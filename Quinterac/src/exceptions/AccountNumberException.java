package exceptions;

public class AccountNumberException extends QException {

	public AccountNumberException(int number) {
		super("Account number invalid.");
		if (number > 999999) {
			this.message = "Account number is too long.";
		}
		if (number < 0) {
			this.message = "Account number cannot be negative.";
		}
		if (number == 000000) {
			this.message = "000000 is a protected account number.";
		}
		
	}
	
	public AccountNumberException(boolean found) {
		super("Account with that number already exists.");
	}
	
	public AccountNumberException() {
		super("Account number invalid.");
	}
	
}
