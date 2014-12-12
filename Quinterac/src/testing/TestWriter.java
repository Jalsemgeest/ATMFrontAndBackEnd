package testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.naming.spi.DirectoryManager;

import au.com.bytecode.opencsv.CSVReader;


public class TestWriter {

	static String test = "Test: ";
	static String purpose = "Purpose: ";
	static String input = "Input: ";
	static String iFiles = "Input files: ";
	static String oFiles = "Output files: ";
	static String output = "Output: ";
	
	public static void main(String[] arg) {
		
		CSVReader reader = null;
		int count = 0;
		
		// MANY TEST CASES
		try {
			reader = new CSVReader(new FileReader(new File("Z:/327/testcase.csv")));
			String[] line;
			
			while ((line = reader.readNext()) != null) {
				if (count > 1) {
					printTestCase(line);
				} else {
					count++;
				}
			}
			
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		
		// ONE LARGE FILE
//		try {
//			PrintWriter out = new PrintWriter(new File("Z:/327/oneFile.txt"));
//			reader = new CSVReader(new FileReader(new File("Z:/327/testcase.csv")));
//			String[] line;
//			
//			while ((line = reader.readNext()) != null) {
//				if (count > 2) {
//					oneFile(line, out);
//				} else {
//					count++;
//				}
//			}
//			
//			out.close();
//			
//		} catch (Exception e) {
//			System.out.println(e.getLocalizedMessage());
//		}
		
		
	}
	
	private static void oneFile(String[] line, PrintWriter out) {
		out.print(test);
		out.println(line[1]);
		out.print(purpose);
		out.println(line[7]);
		out.println(input);
		out.println("\t" + "*-");
		
		if (line[9].equals("1")) {
			out.println("\t" + "login");
			out.println("\t" + "retail");
		} else if (line[9].equals("2")) {
			out.println("\t" + "login");
			out.println("\t" + "agent");
		}
//		System.out.println("Here." + line[1] + line[10]);
		output(line[1], out, Integer.parseInt(line[10]));
//		System.out.println("Here.");
		if (!line[1].contains("logout")) {
			out.println("\t" + "logout");
		}
		
		out.println("\t" + "-*");
		out.print(iFiles);
//		if (line[9].equals("1")) {
//			out.println("Current accounts file, login_agent.not_logged_in.txt");
//		}
		out.println("Current accounts file.");
//		else if(line[9].equals("2")){
//			out.println("Current accounts file, login_retail.not_logged_in.txt");		
//		} else {
//			out.println("Current accounts file.");
//		}
		out.print(oFiles);
		out.println("Transaction summary file.");
		out.print(output);
		out.println(line[8]);
		
		out.println("\n");
		out.println("");
	}
	
	private static void printTestCase(String[] line) throws Exception {
		File file;
		File expectedOutput;
		String fileName = getFileName(line);
		file = new File("Z:/327/TestCases/TestingCorrectFiles/"+fileName+".txt");
		expectedOutput = new File("Z:/327/TestCases/ExpectedOutputs/" + fileName + ".txt" + ".exp");
		System.out.println(fileName);
		
		PrintWriter out = new PrintWriter(file);
		PrintWriter expectedOut = new PrintWriter(expectedOutput);
		
		out.print(test);
		out.println(line[1]);
		out.print(purpose);
		out.println(line[7]);
		out.println(input);
		out.println("\t" + "*-");
		
		if (line[9].equals("1")) {
			out.println("\t" + "login");
			out.println("\t" + "retail");
			
			// For expected output for test records
			expectedOut.println("Successfully logged in as Retail.");
		} else if (line[9].equals("2")) {
			out.println("\t" + "login");
			out.println("\t" + "agent");
			
			// For expected output for test records
			expectedOut.println("Successfully logged in as Agent.");
		}
//		System.out.println("Here." + line[1] + line[10]);
		output(line[1], out, Integer.parseInt(line[10]));
//		System.out.println("Here.");
		if (!line[1].contains("logout")) {
			out.println("\t" + "logout");
		}
		
		out.println("\t" + "-*");
		out.print(iFiles);
//		if (line[9].equals("1")) {
//			out.println("Current accounts file, login_agent.not_logged_in.txt");
//		}
		out.println("Current accounts file.");
//		else if(line[9].equals("2")){
//			out.println("Current accounts file, login_retail.not_logged_in.txt");		
//		} else {
//			out.println("Current accounts file.");
//		}
		out.print(oFiles);
		out.println("Transaction summary file.");
		out.print(output);
		out.println(line[8]);
		
		// Printint for expectedOutputs for the comparison of tests. 
		expectedOut.println(line[8]);
		
		if ((!line[1].contains("logout") && !line[2].equals("not logged in")) || (line[1].contains("login") && (line[1].contains("Agent") || line[1].contains("Retail")))) {
			expectedOut.println("Logout Successful.");
		}
		
		expectedOut.close();
		out.close();
	}
	
	private static void output(String line, PrintWriter out, int num) {
		String temp;
		if (num > 0) {
			if (num == 1) { // Logout
				out.println("\t" + line);
//				System.out.println(line);
			} else {
				temp = line.substring(0, line.indexOf(" "));
				out.println("\t" + temp);
//				System.out.println("here: " + temp);
				if (num > 1) {
					if (num == 2) { // Login
						temp = line.substring(nthOccurrence(line, ' ', 0));
						if (temp.startsWith(" ")) {
							temp = temp.replaceFirst(" ", "");
						}
//						System.out.println(temp);
						out.println("\t" + temp);
					} else { 
						temp = line.substring(nthOccurrence(line, ' ', 0), nthOccurrence(line, ' ', 1));
//						System.out.println(temp);
						if (temp.startsWith(" ")) {
							temp = temp.replaceFirst(" ", "");
						}
						out.println("\t" + temp);
						if (num > 2) {
							if (num == 3) { // Create, Delete, Withdraw, Deposit
								temp = line.substring(nthOccurrence(line, ' ', 1));
//								System.out.println(temp);
								if (temp.startsWith(" ")) {
									temp = temp.replaceFirst(" ", "");
								}
								out.println("\t" + temp);
							} else {
								
								temp = line.substring(nthOccurrence(line, ' ', 1), nthOccurrence(line, ' ', 2));
//								System.out.println(temp);
								if (temp.startsWith(" ")) {
									temp = temp.replaceFirst(" ", "");
								}
								out.println("\t" + temp);
								if (num > 3) {
									temp = line.substring(nthOccurrence(line, ' ', 2));
//									System.out.println(temp);
									if (temp.startsWith(" ")) {
										temp = temp.replaceFirst(" ", "");
									}
									out.println("\t" + temp);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static int nthOccurrence(String str, char c, int n) {
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos+1);
	    return pos;
	}
	
	private static String getFileName(String[] line) {
		String name = line[1] + ".";
		if (!line[2].equals("")) {
			name += line[2];
		} 
		if (!line[3].equals("")) {
			name += "." + line[3];
		}
		if (!line[4].equals("")) {
			name += "." + line[4];
		}
		if (!line[5].equals("")) {
			name += "." + line[5];
		}
		name = name.replaceAll("\\s", "_");
		return name;
	}
	
}
