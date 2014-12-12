import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import exceptions.AccountException;
import exceptions.AccountNameException;
import exceptions.AccountNumberException;
import exceptions.AgentAmountException;
import exceptions.AmountException;
import exceptions.LoginStatusException;
import exceptions.QException;
import exceptions.RetailAmountException;

/**
 * Main
 * Intention: To act as a console based ATM.
 * Input files: master_accounts_file.txt.
 *     This file is assumed to be at a given location, however if specified as the first parameter when calling the jar
 *     it will changed to said parameter.
 * Output files: endOfDay.txt
 *     Daily transaction summary file.
 * How to run: User is prompted for login.  Once logged in the user may choose how to interact with accounts based on 
 *             permissions of said user.  Once the user is complete, they must logout.  At that time, the output file will be printed.
 * @author Jake Alsemgeest & Leif Raptis-Firth
 * 
 *
 */
public class Main {

	// Holds the transactions for the end of day
	static ArrayList<TransactionSummary> trans = new ArrayList<TransactionSummary>();
	static ArrayList<Account> accountsMaster = new ArrayList<Account>();
	static ArrayList<Account> accountsLocal = new ArrayList<Account>();
	// static ArrayList<Record> records = new ArrayList<Record>();

	final static int INVALID = -1;

	// Final variables for states
	final static int LOGOUT = 0;
	final static int CREATE = 1;
	final static int DELETE = 2;
	final static int DEPOSIT = 3;
	final static int WITHDRAW = 4;
	final static int TRANSFER = 5;
	final static int LOGIN = 6;

	// Final variables for login status
	final static int AGENT = 7;
	final static int RETAIL = 8;
	final static int LOGGED_OUT = 9;

	// Variable that tracks the users login status
	private static int LOGGED_IN_AS = LOGGED_OUT;

	// Variable that tracks the state the user is in at any given time
	private static int STATE = INVALID;

	// Constants account numbers
	final static int ACCOUNT_NUMBER_UPPER_LIMIT = 999999;
	final static int PROTECTED_ACCOUNT_NUMBER = 000000;

	// Constant for amounts
	final static int RETAIL_AMOUNT_LIMIT = 100000;
	final static int AGENT_AMOUNT_LIMIT = 99999999;

	// Constants for account name
	final static int ACCOUNT_NAME_MAX_LENGTH = 15;

	private final static PrintStream s = System.out;
	
	static String inputFile = "Z:/master_accounts_file.txt";
	private static PrintWriter out;

	public static void main(String args[]) {

		if (args.length != 0) {
			inputFile = args[0];
		}
		readInputFile();
		
		Scanner in = new Scanner(System.in);
		enterCommand();
		String line;
		while ((line = in.nextLine()) != null) {
			line = line.toLowerCase();
			int val = getCommand(line);
			if (val == LOGIN) {
				if (notLoggedInt()) {
					STATE = LOGIN;
					getLoginUser(in);
				}
			} else if (val == LOGOUT && LOGGED_IN_AS != LOGGED_OUT) {
				STATE = LOGOUT;
				logoutUser();
				break;
			} else if (val == CREATE) {
				if (createDelete()) {
					STATE = CREATE;
					createAccount(in);
				}
			} else if (val == DELETE) {
				if (createDelete()) {
					STATE = DELETE;
					deleteAccount(in);
				}
			} else if (val == DEPOSIT) {
				if (depositWithdrawTransfer()) {
					STATE = DEPOSIT;
					deposit(in);
				}
			} else if (val == WITHDRAW) {
				if (depositWithdrawTransfer()) {
					STATE = WITHDRAW;
					withdraw(in);
				}
			} else if (val == TRANSFER) {
				if (depositWithdrawTransfer()) {
					STATE = TRANSFER;
					transfer(in);
				}
			} else {
				throwException(new QException(), "Invalid input.");
			}
			enterCommand();
		}
	}

	/**
	 * deleteAccount This will take in the users input and delete the account if
	 * the input is valid.
	 * 
	 * @param in
	 *            - Scanner
	 */
	private static void deleteAccount(Scanner in) {
		String num = in.nextLine();
		String name = in.nextLine();
		boolean foundAccount = false;
		if (validateAccountNumber(num) && validateAccountName(name)) {
			name = formatAccountName(name);
			int accountNum = Integer.parseInt(num);

			// Check Master accounts and/or file
			// If it's there, then delete it. otherwise, do not.
			// If it's deleted, we need to MARK IT.
			for (Account acc : accountsMaster) {
				if (accountNum == acc.accountNumber) { // Correct Log in Name
														// and Number
					if (name.equals(acc.accountName.replace('_', ' '))) {
						if (acc.checkBalance() == 0) {
							acc.deleteAccount();
							trans.add(new TransactionSummary(accountNum, name));
							s.println("Account <" + name.trim() + "> with <"
									+ num + "> has been successfully deleted.");
						} else {
							throwException(new AccountException(),
									acc.checkBalance());
						}
					} else {
						throwException(new AccountNameException(), name);
					}
					foundAccount = true;
				}
			}

			if (!foundAccount) {
				if (!(findAccountInFile(num) == null)) { // If it doesn't
															// equal null then
															// we found it in
															// the file
					int count = accountsMaster.size();
					accountsMaster.add(getAccountFromLine(num)); // Import it
																	// and add
																	// it to the
																	// arraylist

					foundAccount = true;
					if (name.equals(accountsMaster.get(count).accountName.replace('_', ' '))) {
						if (accountsMaster.get(count).checkBalance() == 0) {
							accountsMaster.get(count).deleteAccount();
							trans.add(new TransactionSummary(accountNum, name));
							s.println("Account <" + name.trim() + "> with <"
									+ num + "> has been successfully deleted.");
						} else {
							throwException(new AccountException(), accountsMaster
									.get(count).checkBalance());
						}
					} else {
						throwException(new AccountException(), INVALID + 1);
					}
				}
			}

			if (!foundAccount) {
				throwException(new AccountException(), INVALID);
			}

		}

		STATE = INVALID;
	}

	/**
	 * createAccount This will take the next two input values from the user and
	 * attempt to create a new account.
	 * 
	 * @param in
	 *            - Scanner
	 */
	private static void createAccount(Scanner in) {
		String num = in.nextLine();
		String name = in.nextLine().trim();
		boolean foundAccount = false;
		if (validateAccountNumber(num) && validateAccountName(name)) {
			int accountNum = Integer.parseInt(num);

			for (Account acc : accountsMaster) {
				if (acc.accountNumber == accountNum) {
					foundAccount = true;
					break;
				}
			}

			if (!foundAccount
					&& !(findAccountInFile(accountNum + "") == null)) { // Does
																				// not
																				// exist
																				// within
																				// the
																				// master
																				// accounts
																				// file.
				foundAccount = true;
			}

			if (!foundAccount) {
				for (Account acc : accountsLocal) {
					if (acc.accountNumber == accountNum) {
						foundAccount = true;
						break;
					}
				}
			}

			if (!foundAccount) {
				accountsLocal.add(new Account(accountNum, 0, name));
				trans.add(new TransactionSummary(accountNum, name));
				s.println("Account <" + name + "> with <" + num
						+ "> has been successfully created.");
			} else {
				throwException(new AccountNumberException(), foundAccount);
			}
		}

		STATE = INVALID; // Create Complete.
	}

	/**
	 * deposit This will deposit an amount into the given accounts.
	 * 
	 * @param in
	 *            - Scanner
	 */
	private static void deposit(Scanner in) {
		String account = in.nextLine();
		String amount = in.nextLine();
		if (validateAccountNumber(account) && validateAmount(amount)) {
			int accountNum = Integer.parseInt(account);
			int amountNum = Integer.parseInt(amount);

			if (amountNum > RETAIL_AMOUNT_LIMIT && LOGGED_IN_AS == RETAIL) {
				throwException(new RetailAmountException(), amountNum);
			} else if (amountNum > AGENT_AMOUNT_LIMIT && LOGGED_IN_AS == AGENT) {
				throwException(new AgentAmountException(), amountNum);
			}

			else if (validateDeposit(accountNum, amountNum)) {
				for (Account acc : accountsMaster) {
					if (acc.accountNumber == accountNum) {
						if (!acc.deleteStatus()) {
							if ((acc.checkBalance() + amountNum) > 99999999) {
								throwException(new AmountException(), (acc.checkBalance() + amountNum));
							} else {
								acc.deposit(amountNum);
								System.out.println("Deposit Successful.");
								trans.add(new TransactionSummary(accountNum, amountNum));
							}
						} else {
							throwException(new AccountNumberException(), INVALID);
						}
					}
				}
				
			}
		}
		STATE = INVALID;
	}

	/**
	 * withdraw This will withdraw an amount from a given account.
	 * 
	 * @param in
	 *            - Scanner
	 */
	private static void withdraw(Scanner in) {
		String account = in.nextLine();
		String amount = in.nextLine();
		if (validateAccountNumber(account) && validateAmount(amount)) {
			int accountNum = Integer.parseInt(account);
			int amountNum = Integer.parseInt(amount);

			boolean temp = false;
			
			// Validate if it can happen. Account exists, withdraw can occur
			// with current balance
			if (validateWithdraw(accountNum, amountNum)) {
				temp = true;
				for (Account acc : accountsMaster) {
					if (acc.accountNumber == accountNum) {
						if (!acc.deleteStatus()) {
							acc.incrementWithdrawn(amountNum);
							try {
								acc.withdraw(amountNum);
							} catch (AmountException e) {
								e.printMessage();
							}
							break;
						} else {
							throwException(new AccountNumberException(), INVALID);
						}
					}
				}
				trans.add(new TransactionSummary(accountNum, amountNum));
				System.out.println("Withdraw Successful.");
			}

		}
		STATE = INVALID;
	}

	/**
	 * transfer
	 * This will take three inputs from the user. (Two valid account numbers and an amount to transfer).
	 * This will transfer the amount from the first account entered to the second account entered.
	 * @param in - Scanner in for the users input.
	 */
	private static void transfer(Scanner in) {
		String account1 = in.nextLine();
		String account2 = in.nextLine();
		String amount = in.nextLine();
		if (validateAccountNumber(account1) && validateAccountNumber(account2)
				&& validateAmount(amount)) {
			int accountNum1 = Integer.parseInt(account1);
			int accountNum2 = Integer.parseInt(account2);
			int amountNum = Integer.parseInt(amount);

			Account acc2 = null;
			boolean withdrawn = false;
			// Try to withdraw money from accountNum1
			if (validateWithdraw(accountNum1, amountNum)) {
				if (validateDeposit(accountNum2, amountNum)) {
					// Check if the number would exceed 99999999 in the depositee's account.
					boolean validDeposit = false;
					for (Account acc: accountsMaster) {
						if (acc.accountNumber == accountNum2) {
							if ((acc.checkBalance() + amountNum) <= 99999999) {
								validDeposit = true;
							}
						}
					}
					if (validDeposit) {
						for (Account acc : accountsMaster) {
							if (acc.accountNumber == accountNum1) {
								try {
									acc.withdraw(amountNum);
									withdrawn = true;
									if (acc2 != null) {
										acc2.deposit(amountNum);
									}
								} catch (AmountException e) {
									e.printMessage();
									break;
								}
							}
							if (acc.accountNumber == accountNum2) {
								acc2 = acc;
								if (withdrawn) {
									acc2.deposit(amountNum);
								}
							}
						}
						trans.add(new TransactionSummary(accountNum1, accountNum2,
								amountNum));
						s.println("Transfer Successful.");
					}
					else {
						throwException(new AmountException(), 100000000);
					}
				}
			}
		}
	}

	/**
	 * validateDeposit
	 * This will validate that the given account is valid and the current user is allowed to deposit the given amount.
	 * @param account - Valid account number the user provided.
	 * @param amount - Valid amount the user provided.
	 * @return TRUE - If the user entered a valid account and they are permitted to deposit the amount given.
	 *        FALSE - If it is not a valid deposit case.
	 */
	private static boolean validateDeposit(int account, int amount) {

		// For loop that iterates through the current accountsMaster ArrayList
		for (Account acc : accountsMaster) {
			// If the current account that is being withdrawn from is found
			// within the accountsMaster
			if (acc.accountNumber == account) {
				// Set currentWithdrawn to the accounts withdrawn amount
				if (acc.deleteStatus()) {
					return false;
				}
				return true;
			}
		}

		// The account is not in the accountsMaster
		if (!(findAccountInFile("" + account) == null)) {
			accountsMaster.add(getAccountFromLine("" + account));
			return true;
		} else {
			throwException(new AccountException(), INVALID);
			return false;
		}
	}

	/**
	 * validateWithdraw This will check the accountsMaster ArrayList to see if
	 * the account we are trying to withdraw from has already been imported. If
	 * it has not been imported it will go through the master account file to
	 * try to find one
	 * 
	 * @param account
	 *            - account number the user has entered
	 * @param amount
	 *            - amount the user has entered
	 * @return TRUE - The amount is allowed to be withdrawn from a valid account
	 *         by the given user. FALSE - The account does not exist, or the
	 *         given user cannot withdraw the given amount.
	 */
	private static boolean validateWithdraw(int account, int amount) {

		// Used to track the amount withdrawn for a given account.
		int currentWithdrawn = 0;

		// For loop that iterates through the current accountsMaster ArrayList
		for (Account acc : accountsMaster) {
			// If the current account that is being withdrawn from is found
			// within the accountsMaster
			if (acc.accountNumber == account) {
				// Set currentWithdrawn to the accounts withdrawn amount
				currentWithdrawn = acc.checkWidthdrawn();
				if (acc.deleteStatus()) {
					return false;
				}
				// If currentWithdrawn + amount we are trying to withdraw
				// exceeds the amount allowed for the user return false
				if ((currentWithdrawn + amount) > RETAIL_AMOUNT_LIMIT
						&& LOGGED_IN_AS == RETAIL) {
					throwException(new RetailAmountException(),
							(currentWithdrawn + amount));
					return false;
				}
				// If currentWithdrawn + amount we are trying to withdraw
				// exceeds the amount allowed for the user return false
				else if ((currentWithdrawn + amount) > AGENT_AMOUNT_LIMIT
						&& LOGGED_IN_AS == AGENT) {
					throwException(new AgentAmountException(),
							(currentWithdrawn + amount));
					return false;
				}
				if ((acc.checkBalance() - amount) < 0) {
					throwException(new AmountException(), INVALID - 1);
					return false;
				}
				return true;
			}
		}

		// The account is not in the accountsMaster
		if (!(findAccountInFile("" + account) == null)) {
			int count = accountsMaster.size();
			// Adding the current account from the masters account file to the
			// accountsMaster ArrayList
			accountsMaster.add(getAccountFromLine("" + account));
			// Setting the currentWithdrawn
			currentWithdrawn = accountsMaster.get(count).checkWidthdrawn();

			if ((currentWithdrawn + amount) > RETAIL_AMOUNT_LIMIT
					&& LOGGED_IN_AS == RETAIL) {
				throwException(new RetailAmountException(),
						(currentWithdrawn + amount));
				return false;
			} else if ((currentWithdrawn + amount) > AGENT_AMOUNT_LIMIT
					&& LOGGED_IN_AS == AGENT) {
				throwException(new AgentAmountException(),
						(currentWithdrawn + amount));
				return false;
			}
			if ((accountsMaster.get(count).checkBalance() - amount) < 0) {
				throwException(new AmountException(), INVALID);
				return false;
			}
			return true;

		} else {
			throwException(new AccountException(), INVALID);
			return false;
		}
	}

	/**
	 * getAccountFromLine This will parse the given line and then return a new
	 * account from it.
	 * 
	 * @param account
	 *            - String that will be parsed through.
	 * @return Account - This will return the account.
	 */
	private static Account getAccountFromLine(String account) {
		String[] array = new String[3]; // [0] - Account Number, [1] - Account
										// Name, [2] - Account Balance
		array = parseLine(findAccountInFile(account));
		return new Account(Integer.parseInt(array[0]), Integer.parseInt(array[1]),
				array[2]);
	}

	/**
	 * findAccountInFile This will search for the account line in the masters
	 * account file.
	 * 
	 * @param account
	 *            - String that the user inputs
	 * @return String - where the string is the line within the Master accounts
	 *         file NULL - If it is not found then it will return null.
	 */
	private static String findAccountInFile(String account) {
		File file = new File(inputFile);
		if (file.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] fileAccount = parseLine(line);
					if (fileAccount[0].equals(account)) {
						return line;
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}

	/**
	 * parseLine This will parse the given line from the Master Accounts file
	 * 
	 * @param line
	 *            - Line from the master accounts file
	 * @return String[] - where [0] - Account Number, [1] - Account Balance, [2]
	 *         - Account Name
	 */
	private static String[] parseLine(String line) {
		String[] array = new String[3];
		array[0] = ""+Integer.parseInt(line.substring(0, line.indexOf("_")));
		String temp = line.substring(line.indexOf("_") + 1);
		array[1] = temp.substring(0, temp.indexOf("_"));
		array[2] = temp.substring(temp.indexOf("_") + 1);
		return array;
	}

	/**
	 * createDelete This will return true if the user has the rights to create
	 * and/or delete an account.
	 * 
	 * @return
	 */
	private static boolean createDelete() {
		if (LOGGED_IN_AS == AGENT) {
			return true;
		} else {
			throwException(new LoginStatusException(), RETAIL);
			return false;
		}
	}

	/**
	 * depositWithdrawTransfer This will return true if the user has the rights
	 * to deposit, withdraw or transfer.
	 * 
	 * @return
	 */
	private static boolean depositWithdrawTransfer() {
		if (LOGGED_IN_AS != LOGGED_OUT) {
			return true;
		} else {
			throwException(new LoginStatusException(), LOGGED_IN_AS);
			return false;
		}
	}

	/**
	 * validateAmount
	 * This will take in the users given amount and ensure that the amount is valid for the current session.
	 * @param amount - String containing the amount provided by the user.
	 * @return TRUE - the user entered a valid amount.
	 *        FALSE - the user entered an amount that is either invalid or they do not have the permissions for.
	 */
	private static boolean validateAmount(String amount) {
		int amountNum = INVALID;
		try {
			amountNum = Integer.parseInt(amount);
		} catch (NumberFormatException e) {
			throwException(new AmountException(), (INVALID + 1));
			return false;
		}
		if (amountNum < 0) {
			throwException(new AmountException(), INVALID - 1);
			return false;
		} else if (amountNum > RETAIL_AMOUNT_LIMIT && LOGGED_IN_AS == RETAIL) {
			throwException(new RetailAmountException(), amountNum);
			return false;
		} else if (amountNum > AGENT_AMOUNT_LIMIT && LOGGED_IN_AS == AGENT) {
			throwException(new AgentAmountException(), amountNum);
			return false;
		}

		return true;
	}

	/**
	 * validateAccountNumber This will take the input from the user and attempt
	 * to validate the account number entered
	 * 
	 * @param num
	 *            - account number user entered as a string
	 * @return TRUE - User entered a valid account number. FALSE - User entered
	 *         invalid account number.
	 */
	private static boolean validateAccountNumber(String num) {
		int accountNum = INVALID;
		try {
			accountNum = Integer.parseInt(num);
			
		} catch (NumberFormatException e) {
			throwException(new AccountNumberException(), INVALID);
			return false;
		}
		if (accountNum > ACCOUNT_NUMBER_UPPER_LIMIT
				|| accountNum <= PROTECTED_ACCOUNT_NUMBER) { // Ensuring the
																// number is not
																// 0, protected
																// or greater
																// than 999999
			throwException(new AccountNumberException(), accountNum);
			return false;
		}

		return true;
	}

	/**
	 * validateAccountName
	 * 
	 * @param name
	 *            - accountName the user enters
	 * @return TRUE - the user enters a valid account name. FALSE - the user
	 *         enters an invalid account name. Ie. It's length is greater than
	 *         15 or equal to 0.
	 */
	private static boolean validateAccountName(String name) {
		if (name.length() > ACCOUNT_NAME_MAX_LENGTH || name.length() == 0) {
			throwException(new AccountNameException(), name);
			return false;
		}
		return true;
	}

	/**
	 * formatAccountName This will take in the users entered account name and
	 * format it correctly so that it is 15 characters long including
	 * whitespace.
	 * 
	 * @param name
	 *            - String the user has entered.
	 * @return String - final account name.
	 */
	private static String formatAccountName(String name) {
		String tempName = name;
		int length = tempName.length();

		for (int i = 0; i < (ACCOUNT_NAME_MAX_LENGTH - length); i++) {
			tempName = tempName + " ";
		}

		return tempName;
	}

	/**
	 * getLoginUser This will get the input from the user after they have
	 * entered the login command. It will throw an exception if the user enters
	 * an invalid user account and provide a useful message to the user. If the
	 * user enters a valid user account then it will return a message informing
	 * them they have successfully logged in.
	 * 
	 * @param in
	 *            - input line, just using the same one.
	 */
	private static void getLoginUser(Scanner in) {
		String line = in.nextLine().toLowerCase();

		if (line.equals("agent")) {
			LOGGED_IN_AS = AGENT;
			s.println("Successfully logged in as Agent.");
		} else if (line.equals("retail")) {
			LOGGED_IN_AS = RETAIL;
			s.println("Successfully logged in as Retail.");
		} else {
			STATE = INVALID;
			throwException(new LoginStatusException(), LOGIN);
		}

	}

	/**
	 * logoutUser This will logout the user if the user is already logged in. If
	 * the user is not logged in then an exception will be thrown providing the
	 * user with a message indicating they are not logged in.
	 */
	private static void logoutUser() {
		if (LOGGED_IN_AS != LOGGED_OUT) {
			LOGGED_IN_AS = LOGGED_OUT;
			STATE = -1;
			File dir = new File("Z:/327/Transactions");
			File endOfDay = new File("Z:/327/Transactions/transaction_summary_file_" + dir.list().length + ".txt");
			try {
				out = new PrintWriter(endOfDay);
				for (TransactionSummary t: trans){
					out.println(t);
				}
				out.println("00");
				out.close();
				System.out.println("Logout Successful.");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}			
		} else {
			try {
				throw new LoginStatusException();
			} catch (LoginStatusException e) {
				e.printMessage();
			}
		}

	}

	/**
	 * notLoggedInt This will check if the user is logged in.
	 * 
	 * @return true - the user is not logged in. false - the user is logged in.
	 */
	private static boolean notLoggedInt() {
		if (LOGGED_IN_AS == LOGGED_OUT) {
			return true;
		} else {
			throwException(new LoginStatusException(), INVALID);
			return false;
		}
	}

	/**
	 * getCommand This will take in the users initial input, providing a return
	 * value of what input parameter the user entered.
	 * 
	 * @param line
	 *            - the line that the user entered through system.in
	 * @return int - based on the state that the user is trying to enter.
	 */
	private static int getCommand(String line) {
		if (line.equals("login")) {
			return LOGIN;
		} else if (line.equals("logout")) {
			return LOGOUT;
		} else if (line.equals("create")) {
			return CREATE;
		} else if (line.equals("delete")) {
			return DELETE;
		} else if (line.equals("deposit")) {
			return DEPOSIT;
		} else if (line.equals("withdraw")) {
			return WITHDRAW;
		} else if (line.equals("transfer")) {
			return TRANSFER;
		}

		return -1;
	}

	/**
	 * readInputFile This function will read the input file for a start of the
	 * day.
	 */
	private static void readInputFile() {
		File file = null;
		try {
			file = new File(inputFile);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		if (file.equals(null)) {
			s.println("Count not find master accounts file.");
		}
	}

	/**
	 * enterCommand This just prints out Enter a Command after a state has
	 * ended.
	 */
	private static void enterCommand() {
		s.println("Enter a command:");
	}

	/**
	 * throwException
	 * This amalgamates the possible exceptions that will be thrown by the program.
	 * This was used to created to give a more clean way to throw exceptions when needed.
	 * @param ex - Exception that extends QException
	 * @param val - Value if needed.
	 */
	private static void throwException(QException ex, boolean val) {
		try {
			if (ex instanceof AccountNumberException) {
				throw new AccountNumberException(val);
			}
		} catch (QException e) {
			e.printMessage();
		}
	}
	
	/**
	 * throwException
	 * This amalgamates the possible exceptions that will be thrown by the program.
	 * This was used to created to give a more clean way to throw exceptions when needed.
	 * @param ex - Exception that extends QException
	 * @param val - Value if needed.
	 */
	private static void throwException(QException ex, String val) {
		try {
			if (ex instanceof AccountNameException) {
				throw new AccountNameException(val);
			}
			else if (ex instanceof QException) {
				throw new QException(val);
			}
		} catch (QException e) {
			e.printMessage();
		}
	}

	/**
	 * throwException This will throw a number of different exceptions provided
	 * to it as parameters.
	 * 
	 * @param ex
	 *            - Exception
	 * @param val
	 *            - Value if the exception requires it. Enter -1 if it's not
	 *            needed.
	 */
	private static void throwException(QException ex, int val) {
		try {
			if (ex instanceof AccountNumberException) {
				if (val == INVALID) {
					throw new AccountNumberException();
				} else {
					throw new AccountNumberException(val);
				}
			} else if (ex instanceof RetailAmountException) {
				throw new RetailAmountException(val);
			} else if (ex instanceof AgentAmountException) {
				throw new AgentAmountException(val);
			} else if (ex instanceof AmountException) {
				if (val == INVALID) {
					throw new AmountException();
				} else {
					throw new AmountException(val);
				}
			} else if (ex instanceof LoginStatusException) {
				if (val == INVALID) {
					throw new LoginStatusException();
				} else {
					throw new LoginStatusException(val);
				}
			} else if (ex instanceof AccountException) {
				if (val == INVALID) {
					throw new AccountException();
				} else {
					throw new AccountException(val);
				}
			}

		} catch (QException e) {
			e.printMessage();
		}

	}

	/**
	 * Assumes that state always properly correlates to the appropriate
	 * parameters.
	 * 
	 * @author 11flr1
	 *
	 */
	private static class TransactionSummary {

		private int account1 = 000000;
		private int account2 = 000000;
		private int amount = 0;
		private String accountName = "_______________";
		private int state = STATE;

		/**
		 * create delete
		 * 
		 * @state
		 * @param account1
		 * @param accountName
		 * @return
		 */
		TransactionSummary(int account1, String accountName) {
			this.account1 = account1;
			this.accountName = convertAccountName(formatAccountName(accountName));
		}

		/**
		 * deposit withdraw
		 * 
		 * @state
		 * @param account1
		 * @param amount
		 * @return
		 */
		TransactionSummary(int account, int amount) {
			this.account1 = account;
			this.amount = amount;
		}

		/**
		 * transfer
		 * 
		 * @state
		 * @param account1
		 * @param account2
		 * @param amount
		 * @return
		 */
		TransactionSummary(int account1, int account2, int amount) {
			this.account1 = account1;
			this.account2 = account2;
			this.amount = amount;
		}

		public String toString() {
			String acc1 = Integer.toString(account1);
			String acc2 = Integer.toString(account2);
			String amt = Integer.toString(amount);
			int ac1 = acc1.length();
			int ac2 = acc2.length();
			int amount = amt.length();
			for (int i = 0; i < 6 - ac1; i++)	{ acc1 = "0".concat(acc1); }
			for (int i = 0; i < 6 - ac2; i++)	{ acc2 = "0".concat(acc2); }
			for (int i = 0; i < 8 - amount; i++) { amt = "0".concat(amt);	}		
			return new String ("0" + state + "_" + acc1 + "_" + acc2 + "_" + amt + "_" + accountName);

		}

		/**
		 * incoming string is 15 spaces long has gone through accountNameFormat
		 * in main converts all spaces to underscores
		 * 
		 */
		private String convertAccountName(String accountName) {
			char[] charName = accountName.toCharArray();
			for (int i = 0; i < charName.length; i++) {
				if (charName[i] == ' ') {
					charName[i] = '_';
				}
			}
			return new String(charName);
		}
	}

}
