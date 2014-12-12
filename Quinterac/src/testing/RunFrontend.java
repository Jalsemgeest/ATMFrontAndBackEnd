package testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

public class RunFrontend {

	static PrintWriter out;
	
	static PrintStream c = System.out;
	static BufferedWriter writer;
	static BufferedReader reader;
	
	private static final String FRONT_END_PATH = "Z:/327/Quinterac.jar";
	
	private static final String[] FRONT_END_START = {"java", "-jar", FRONT_END_PATH};
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		// MANY TEST CASES
		try {
			
			Process p = Runtime.getRuntime().exec(FRONT_END_START);

			OutputStream stdin = p.getOutputStream();
			InputStream stdout = p.getInputStream();
		
			writer = new BufferedWriter(new OutputStreamWriter(stdin));
			reader = new BufferedReader(new InputStreamReader(stdout));
			
			ArrayList<String> commands = new ArrayList<String>();
			commands = parseCSVFile(new File(args[0]));
			if (commands.equals(null)) {
				System.out.println("Fatal error.");
				System.exit(1);
			}
			for (String command : commands) {
				runCommand(command.trim());
			}
			
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		
	}
	
	/**
	 * runCommand
	 * This will push out the command for the front end to the front end process.
	 * @param command
	 */
	private static void runCommand(String command) {
		try {
			System.out.println("Command: " + command);
			writer.write(command + "\n");
			writer.flush();
		} catch (IOException e) {
			System.out.println("Failed to write to process: " + e.getLocalizedMessage());
		}
		
	}
	
	/**
	 * parseCSVFile
	 * This will take in a CSV file and parse it's values.
	 * @param file - CSV file from the standard input.
	 * @return ArrayList<String> that contains the inputs for the front end.
	 */
	private static ArrayList<String> parseCSVFile(File file) {
		ArrayList<String> commands = new ArrayList<String>();
		try {
			CSVReader csvReader = new CSVReader(new FileReader(file));
			int count = 0;
			String[] line;
			while ((line = csvReader.readNext()) != null) {
				if (count > 1) {
					int length = Integer.parseInt(line[4]);
					String temp = line[1];
					while (length > 1) {
						commands.add(temp.substring(0, temp.indexOf(' ')));
						temp = temp.substring(temp.indexOf(' ') + 1);
						length--;
					}
					commands.add(temp);
				} else {
					count++;
				}
			}
			
		} catch (IOException e) {
			System.out.println("Failed to parse CSV file: " + e.getLocalizedMessage());
			return null;
		}
		return commands;
	}
	
	
//	/**
//	 * openFrontEnd
//	 * This will open the front end on the command line by entering the Quinterac jar and entering the basic login commands provided
//	 * by the CSV file.
//	 * @param line - the first line of the CSV file that contains the login information.
//	 */
//	private static void openFrontEnd(String[] line) {
//		try
//		{	
////			setClipboardContents(FRONT_END_START);
//			paste();
//		    pressEnter();
//		    
//		    Thread.sleep(1000);
//		    
//			String[] login = parseLogin(line);
//			setClipboardContents(login[0]);
//		    paste();
//		    pressEnter();
//		    
//		    Thread.sleep(1000);
//		    
//		    setClipboardContents(login[1]);
//		    paste();
//		    pressEnter();
//		    
//		    Thread.sleep(1000);
//		}
//		catch(Exception e) { System.out.println("Could not open front end: " + e.getLocalizedMessage()); }
//	}
//	
//	
//	/**
//	 * parseLogin
//	 * This will parse the first line of the program to get the login and either the Agent or Retail.
//	 * @param line - The input from the csv file.
//	 * @return String[] where the [0] contains the 'login' and the [1] contains 'Agent' or 'Retail'
//	 */
//	private static String[] parseLogin(String[] line) {
//		String[] returnVal =  {line[1].substring(0, line[1].indexOf(' ')), line[1].substring(line[1].indexOf(' ')+1)};
//		return returnVal;
//	}
//	
//	
//	/**
//	 * setClipboardContents
//	 * This is setting the clipboard contents on the system allowing the program to give input more easily.
//	 * @param aString - The string will be what the clipboard is set to.
//	 * void
//	 */
//	private static void setClipboardContents(String aString){
//	    StringSelection stringSelection = new StringSelection(aString);
//	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//	    clipboard.setContents(stringSelection, null);
//	}
//	
//	private static void closeFrontEnd() {
//		setClipboardContents(LOGOUT);
//		paste();
//		pressEnter();
//	}
//	
//	private static void doTransaction(String[] line) {
//		
//	}
//	
//	/**
//	 * paste
//	 * This method will tell the robot to paste.
//	 * NOTE: This is meant ONLY for the command line.  This will not necessarily work in other areas.
//	 * void
//	 */
//	private static void paste() {
//		rightMouseClick();
//		
//		robot.keyPress(KeyEvent.VK_DOWN);
//		robot.keyRelease(KeyEvent.VK_DOWN);
//		robot.keyPress(KeyEvent.VK_DOWN);
//		robot.keyRelease(KeyEvent.VK_DOWN);
//		robot.keyPress(KeyEvent.VK_DOWN);
//		robot.keyRelease(KeyEvent.VK_DOWN);
//		
//		pressEnter();
//	}
//	
//	/**
//	 * rightMouseClick
//	 * This method will tell the robot to simulate a right mouse click.
//	 * void
//	 */
//	private static void rightMouseClick() {
//		robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
//		robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
//	}
//	
//	/**
//	 * pressEnter
//	 * This method will tell the robot to simulate a enter pressed event.
//	 * void
//	 */
//	private static void pressEnter() {
//		robot.keyPress(KeyEvent.VK_ENTER);
//	    robot.keyRelease(KeyEvent.VK_ENTER);
//	}
	
}
