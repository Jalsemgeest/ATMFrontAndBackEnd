package exceptions;

public class AmountException extends QException {

	private final int INVALID = -1;
	
	public AmountException() {
		super("Insufficient funds.");
	}

	public AmountException(int value) {
		super("Invalid amount.");
		if (value < 0) {
			this.message = "Amount must be positive.";
		}
		if (value > 99999999) {
			this.message = "Deposit would exceed account limit.";
		}
	}

}
