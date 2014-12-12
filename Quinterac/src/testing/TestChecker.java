package testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;


public class TestChecker {

	
	public static void main(String[] args){
		
		File actualFolder = new File("Z:/327/TestCases/TestOutputs");
		File expectedFolder = new File("Z:/327/TestCases/ExpectedOutputs");
		compareAllFiles(expectedFolder, actualFolder);	
		
		// Successfully logged in as Retail. *** Successfully logged in as Retail.
		
//		String a = "Successfully logged in as Retail.";
//		String b = "Successfully logged in as Retail.";
//		System.out.println(a == b);
//		System.out.println(a.equals(b));
		
//		System.out.println("hello".equals(null));
		
	}
	
	/**
	 * Assumes that files are passed in in correlating order.
	 * @param expectedFolder
	 * @param actualFolder
	 */
	private static void compareAllFiles(File expectedFolder, File actualFolder){
		
		File[] expectedFiles = expectedFolder.listFiles();
		Arrays.sort(expectedFiles);
		File[] actualFiles = actualFolder.listFiles();
		Arrays.sort(actualFiles);
		if (expectedFiles.length != actualFiles.length){
			System.out.println("Unequal file lengths, exiting comparison.");
			System.out.println(expectedFiles.length);
			System.out.println(actualFiles.length);
			return;
		}
		int fileCount = expectedFiles.length;
		
		File errorLog = new File("Z:/327/ErrorLog.txt");
		try {
			PrintWriter out = new PrintWriter(errorLog);

			for (int i = 0; i < fileCount; i++){				
				for (String line : compareFiles(expectedFiles[i], actualFiles[i]))
					out.println(line);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * This method compares the expected and actual output messages of a unit test.
	 * Inconsistent messages are returned in the array to the user.
	 * 
	 * @param expected Expected output messages.
	 * @param actual Actual output messages.
	 * @return
	 */
	private static ArrayList<String> compareFiles(File expected, File actual){
		String expectedLine = "";
		String actualLine = "";
		
		ArrayList<String> errors = new ArrayList<String>();
		
		try {
			BufferedReader expectedReader = new BufferedReader(new FileReader(expected));
			BufferedReader actualReader = new BufferedReader(new FileReader(actual));
			while (true){
				expectedLine = expectedReader.readLine();
				actualLine = actualReader.readLine();
				if ((expectedLine == null && actualLine == null) ||
						(expectedLine == null && actualLine.equals("Error: Invalid input.")))
					break;
				
				if (actualLine == null || expectedLine == null)
					errors.add(expectedLine + " *** " + actualLine);
				
				else if (!(expectedLine.equals(actualLine)))
					errors.add(expectedLine + " *** " + actualLine);
				
//				System.out.println(expectedLine);
//				System.out.println(actualLine);
				
			}
			System.out.println(expected.getName());
			System.out.println(actual.getName());
			System.out.println();
			if(errors.size() != 0){
				errors.add("^^^^ Test of " + actual.getName() + " produced " + errors.size() + " errors. ^^^^");
				errors.add("\n");
			}
			
			expectedReader.close();
			actualReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return errors;
	}
	
	/**
	 * This method is used in testing automation.
	 * @param testFile The incoming test file containing the commands.
	 * @return Extracted commands
	 */
	private static String[] parseTestInputs(File testFile){
		ArrayList<String> inputs = new ArrayList<String>();
		String line = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(testFile));
			//Skip to input lines
			for (int i = 0; i < 4; i++)
				reader.readLine();
			while (!(line.equals("/t-*"))){
				line = reader.readLine();
				inputs.add(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (String[])(inputs.toArray());
	}
	
}