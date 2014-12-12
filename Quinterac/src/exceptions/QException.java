package exceptions;

public class QException extends Exception {

	public String message;

	public QException() {
		super();
	}
	
	public QException(String message) {
		super(message);
		this.message = message;
	}

	public QException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public void printMessage() {
		System.out.println("Error: " + message);
	}

}
