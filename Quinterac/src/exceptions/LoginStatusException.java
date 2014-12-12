package exceptions;

public class LoginStatusException extends QException {

	// Final variables for login status
	final static int AGENT = 7;
	final static int RETAIL = 8;
	final static int LOGGED_OUT = 9;

	public LoginStatusException() {
		super("Already logged in.");

	}

//	public LoginStatusException(int login) {
//		super("You do not have permission to perform that action.");
//		if (login == LOGGED_OUT) {
//			this.message = "Must be logged in as Agent.";
//		}
//		else if (login == RETAIL) {
//			this.message = "Must be logged in.";
//		}
//		else {
//			this.message = "Invalid input.";
//		}
//	}
	
	public LoginStatusException(int login) {
		super("You do not have permission to perform that action.");
		if (login == LOGGED_OUT) {
			this.message = "Must be logged in.";
		}
		else if (login == RETAIL) {
			this.message = "Must be logged in as Agent.";
		}
		else {
			this.message = "Invalid input.";
		}
	}
}
