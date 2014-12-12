

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import exceptions.AmountException;


public class BackEnd {

	// Basic array lists that hold transactions, accounts and errors.
	static ArrayList<TransactionSummary> trans = new ArrayList<TransactionSummary>();
	static ArrayList<Account> accountsMaster = new ArrayList<Account>();
	static ArrayList<Account> newValid = new ArrayList<Account>();
	static ArrayList<String> errorLog = new ArrayList<String>();
	
	private final static PrintStream s = System.out;
	
	static String masterFile = "Z:/master_accounts_file.txt";
	
	static String mergedTransactionSummaryFile = "Z:/327/TransactionsMerged/transaction_summary_file_merged_2014-11-23T145112.753-0500.txt";
	
	// Constants for transactions.
	final static int CREATE = 01;
	final static int DELETE = 02;
	final static int DEPOSIT = 03;
	final static int WITHDRAW = 04;
	final static int TRANSFER = 05;
	
	// Maximum valid amount for an account.
	final static int ACCOUNT_MAX = 99999999;
	
	public static void main(String[] args) {
		
		// Grab all accounts from Master Accounts File
		try {
			
			// Reading in the master accounts file.
			readMaster(masterFile);

			// Reading in the merged transaction summary file.
			// If transaction summary files are specified, we will read them in here.
			// Note: This is essentially creating the merged transaction summary file at runtime.
			if (args.length > 0) {
				for (String name : args) {
					readEndOfDay(name);
				}
			}
			// Otherwise, we will read in the hard-coded value of the transaction summary file.
			else {
				readEndOfDay(mergedTransactionSummaryFile);
			}
			
		} catch (IOException e) {
			s.println("FATAL ERROR.");
			System.exit(1);
		}
		
		// Apply all the transactions.
		applyTransactions();
		
		// Print out the new master accounts file.
		outputMasterAccountsFile();
		
		// Ouput the error log, if any.
		outputErrorLog();
	}
	
	/**
	 * outputErrorLog
	 * This will output any errors, unless fatal.  It will go to the hardcoded location of: Z:/errorLog.txt
	 * void
	 */
	private static void outputErrorLog() {
		if (errorLog.size() == 0)
			return;
		
		try {
			PrintWriter out = new PrintWriter(new File("Z:/errorLog.txt"));
			for (String line : errorLog) {
				out.println(line + "\n");
			}
			out.close();
		} catch (FileNotFoundException e) {
			s.println("FATAL ERROR.");
			System.exit(1);
		}
	}
	
	/**
	 * applyTransaction
	 * This will iterate through the transactions from the merged transactions summary file and apply them to the appropriate acccounts.
	 * void
	 */
	private static void applyTransactions() {
		for (TransactionSummary transaction : trans) {
			switch (transaction.state) {
			case DEPOSIT:
				deposit(transaction);
				break;
			case WITHDRAW:
				withdraw(transaction);
				break;
			case TRANSFER:
				transfer(transaction);
				break;
			case CREATE:
				create(transaction);
				break;
			case DELETE:
				delete(transaction);
				break;
			}
		}
	}
	
	/**
	 * outputMasterAccountsFile
	 * This will output the new master accounts file to the hardcoded location of: Z:/master_accounts_file2.txt
	 * void
	 */
	private static void outputMasterAccountsFile() {
		// We are naming the output master accounts file as master_accounts_file2.txt as we do not want to overwrite the previous
		// file in case of an error causing it to be empty.  Given our process this should not happen, but better to be safe.
		try {
			PrintWriter out = new PrintWriter(new File("Z:/master_accounts_file2.txt"));
			for (Account acc : accountsMaster) {
				out.println(parseForAccountsMaster(acc));
			}
			out.close();
		} catch (FileNotFoundException e) {
			s.println("FATAL ERROR.");
			System.exit(1);;
		}
	}
	
	/**
	 * parseForAccountsMaster
	 * This will parse an account and return the appropriate string representation for the master accounts file.
	 * @param account - account to be interpreted.
	 * @return String - line for master accounts file.
	 */
	private static String parseForAccountsMaster(Account account) {
		int accountNumLength = (account.accountNumber+"").length();
		int amountLength = (account.checkBalance()+"").length();
		int nameLength = account.accountName.length();
		String accountNum = account.accountNumber+"";
		for (int i = 0; i < (6 - accountNumLength); i++) {
			accountNum = "0" + accountNum;
		}
		String amount = account.checkBalance()+"";
		for (int i = 0; i < (8 - amountLength); i++) {
			amount = "0" + amount;
		}
		String name = account.accountName;
		for (int i = 0; i < (15 - nameLength); i++) {
			name += "_";
		}
		return accountNum + "_" + amount + "_" + name;  
	}
	
	/**
	 * delete
	 * This will delete the given account from the current accountsMaster arraylist.
	 * @param transaction - given account that is in question.
	 * void
	 */
	private static void delete(TransactionSummary transaction) {
		for (Account account : accountsMaster) {
			if (account.accountNumber == transaction.account1) {
				if (account.checkBalance() == 0) {
					if (account.accountName.equals(transaction.accountName)) {
						accountsMaster.remove(account);
						break;
					} else {
						errorLog.add("Error: Deleting " + account.accountNumber + " failed. Account name does not match.");
					}
				} else {
					errorLog.add("Error: Deleting " + account.accountNumber + " failed. Account balance was not 0.");
				}
			}
		}
	}
	
	/**
	 * create
	 * This will create the account in the accounts master array list.
	 * @param transaction - given account in question.
	 * void
	 */
	private static void create(TransactionSummary transaction) {
		if (accountsMaster.size() == 0) {
			accountsMaster.add(new Account(transaction.account1, transaction.amount, transaction.accountName));
			return;
		}
		for (int i = 0; i < accountsMaster.size(); i++) {
			if (accountsMaster.get(i).accountNumber == transaction.account1) {
				errorLog.add("Error: Could not create " + transaction.account1 + ".");
				break;
			}
			if (accountsMaster.get(i).accountNumber > transaction.account1) {
				System.out.println("Getting here: " + accountsMaster.get(i).accountNumber);
				accountsMaster.add(i, new Account(transaction.account1, transaction.amount, transaction.accountName));
				break;
			} else if (i == (accountsMaster.size() -1)) {
				accountsMaster.add(new Account(transaction.account1, transaction.amount, transaction.accountName));
			}
		}
	}
	
	/**
	 * transaction
	 * This will remove a set amount, based on the transaction summary, from one account and deposit the amount into the account2.
	 * We explicitly call withdraw and deposit as the checks required are implemented.
	 * @param transaction - Accounts and amount in question.
	 * void
	 */
	private static void transfer(TransactionSummary transaction) {
		Account payer = null;
		Account payee = null;
		for (Account account : accountsMaster) {
			if (account.accountNumber == transaction.account1) {
				payer = account;
			} else if (account.accountNumber == transaction.account2) {
				payee = account;
			}
		}
		try {
			if ((payee.checkBalance() + transaction.amount) > ACCOUNT_MAX) {
				throw new AmountException();
			}
			payer.withdraw(transaction.amount);
			payee.deposit(transaction.amount);
		} catch (AmountException e) {
			errorLog.add("Error: Transferring " + transaction.amount + " from " + payer.accountNumber + ".");
		}
	}
	
	/**
	 * withdraw
	 * This will withdraw a set amount from the account in question.
	 * @param transaction - Account information, including amount.
	 * void
	 */
	private static void withdraw(TransactionSummary transaction) {
		for (Account account : accountsMaster) {
			if (account.accountNumber == transaction.account1) {
				try {
					account.withdraw(transaction.amount);
				} catch (AmountException e) {
					errorLog.add("Error: Withdrawing " + transaction.amount + " from " + account.accountNumber + ".");
				}
				break;
			}
		}
	}
	
	/**
	 * deposit
	 * This will deposit a set amount to the account in question.
	 * @param transaction - Account information, including amount.
	 * void
	 */
	private static void deposit(TransactionSummary transaction) {
		for (Account account : accountsMaster) {
			if (account.accountNumber == transaction.account1) {
				if ((account.checkBalance() + transaction.amount) <= ACCOUNT_MAX) {
					account.deposit(transaction.amount);
				} else {
					errorLog.add("Error: Depositing " + transaction.amount + " into " + account.accountNumber + ".");
				}
				break;
			}
		}
	}
	
	/**
	 * readEndOfDay
	 * This will take in the endOfDay.txt file from the front end and convert them to transactions and add them into the trans arrayList.
	 * @param fileName - the end of day.
	 * @throws IOException - Exceptions based on reading in the file.  FATAL EXCEPTION.
	 * void
	 */
	private static void readEndOfDay(String fileName) throws IOException {
		
		File file = new File(fileName);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String temp;
		while ((temp = reader.readLine()) != null) {
			if (temp.length() > 3) {
				trans.add(new TransactionSummary(temp));
			}
		}
		reader.close();
		
	}
	
	/**
	 * readMaster
	 * This will read the old master accounts file, parsing the accounts into the accountsMaster arrayList.
	 * String name - the name of the file we want to read.
	 * @throws IOException - This may cause a IOException. FATAL EXCEPTION
	 * void
	 */
	private static void readMaster(String name) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(name)));
		String temp;
		while ((temp = reader.readLine()) != null) {
			if (temp.length() > 3) {
				accountsMaster.add(parseMasterAccount(temp));
			}
		}
		reader.close();
	}
	
	/**
	 * parseMasterAccount
	 * Converts a line of the old master accounts file into an account.
	 * @param line - line from old master accounts file.
	 * @return Account - the new account that is created from the line.
	 */
	private static Account parseMasterAccount(String line) {
		String number = line.substring(0, line.indexOf('_'));
		line = line.substring(line.indexOf('_')+1);
		String balance = line.substring(0, line.indexOf('_'));
		line = line.substring(line.indexOf('_')+1);
		
		return new Account(Integer.parseInt(number), Integer.parseInt(balance), line);
	}
	
	private static class TransactionSummary {
		
		private int account1 = 000000;
		private int account2 = 000000;
		private int amount = 0;
		private String accountName = "_______________";
		private int state = 00;

		/**
		 * Creates a transaction summary based on a line from the mergedTransactionSummaryFile
		 * @param line - transaction summary line.
		 */
		TransactionSummary(String line) {
			this.state = Integer.parseInt(line.substring(0, line.indexOf('_')));
			line = line.substring(line.indexOf('_')+1);
			this.account1 = Integer.parseInt(line.substring(0, line.indexOf('_')));
			line = line.substring(line.indexOf('_')+1);
			this.account2 = Integer.parseInt(line.substring(0, line.indexOf('_')));
			line = line.substring(line.indexOf('_')+1);
			this.amount = Integer.parseInt(line.substring(0, line.indexOf('_')));
			line = line.substring(line.indexOf('_')+1);
			this.accountName = line;
		}
		
	}
	
}
