package exceptions;

public class AccountException extends QException {

	public AccountException() {
		super("Account does not exist.");
	}
	
	public AccountException(int val) {
		super("Account does not exist.");
		if (val > 0) {
			this.message = "Account balance is not 0.";
		}
		else if (val == 0) {
			this.message = "Account name and number do not match.";
		}
	}
	
}
