import exceptions.AmountException;

/**
 * Account
 * This class contains the functionality required for a bank account with the given ATM program.
 * @author 0jaca
 *
 */
public class Account {

	public String accountName;
	public int accountNumber;
	private int balance;
	private int withdrawn = 0;

	private boolean deleted = false;

	public Account(int accountNumber, int balance, String accountName) {
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.balance = balance;
	}

	public void deposit(int value) {
		this.balance += value;
	}

	public void withdraw(int value) throws AmountException {
		if ((this.balance - value) < 0) {
			throw new AmountException();
		} else {
			this.balance -= value;
		}
	}

	public int checkBalance() {
		return balance;
	}

	public int checkWidthdrawn() {
		return withdrawn;
	}

	public void incrementWithdrawn(int amount) {
		withdrawn += amount;
	}

	public void deleteAccount() {
		deleted = true;
	}

	public boolean deleteStatus() {
		return deleted;
	}

}
