package exceptions;

public class AccountNameException extends QException {

	public AccountNameException() {
		super("Account name is invalid.");
	}

	public AccountNameException(String name) {
		super("Account name invalid.");
		if (name.length() > 15) {
			this.message = "Account name is too long.";
		}
	}

}
