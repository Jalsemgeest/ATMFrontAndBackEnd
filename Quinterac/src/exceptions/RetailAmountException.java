package exceptions;

public class RetailAmountException extends QException {
	
	public RetailAmountException() {
		super("Invalid amount entered.");
	}
	
	public RetailAmountException(int number) {
		super("Invalid amount entered.");
		if (number > 100000) {
			this.message = "Cannot exchange more than 100000 in Retail mode in one session.";
		}
	}
}
