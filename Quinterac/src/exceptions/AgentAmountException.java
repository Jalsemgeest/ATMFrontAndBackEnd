package exceptions;

public class AgentAmountException extends QException {

	public AgentAmountException() {
		super("Invalid amount entered.");
	}
	
	public AgentAmountException(int number) {
		super("Invalid amount entered.");
		if (number > 99999999) {
			this.message = "Cannot exchange more than 99999999 in one transaction.";
		}
	}
	
}
