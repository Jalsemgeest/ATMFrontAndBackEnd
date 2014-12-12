package testing;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
import java.util.ArrayList;


public class TestCommandLine {

	static PrintStream c = System.out;
	static BufferedWriter writer;
	static BufferedReader reader;
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
//	
//		if (args.length != 0) {
//			System.out.println("We would be testing: " + args[0]);
//		} else {
//			System.out.println("No information detected.");
//		}
		
		String[] temp = {"java", "-jar", "Z:/327/TestCases/Quinterac.jar"};
//		ProcessBuilder builder = new ProcessBuilder(temp);
//		builder.redirectErrorStream(true);
//		Process p = builder.start();
		Process p = Runtime.getRuntime().exec(temp);
		
		OutputStream stdin = p.getOutputStream();
		InputStream stdout = p.getInputStream();
	
		writer = new BufferedWriter(new OutputStreamWriter(stdin));
		reader = new BufferedReader(new InputStreamReader(stdout));
		
		ArrayList<String> input = parseTestInputs(new File(args[0]));
		
		writer.flush();
//		reader.readLine();
		for (String x : input) {
//			System.out.println(x.trim());
			writeToProcess(x.trim());
		}
		
		writer.close();
		
//		System.out.println(reader.readLine());
//		writer.flush();
//		writer.write("login\n");
//		writer.flush();
////		System.out.println("login");
//		writer.write("agent\n");
//		writer.flush();
//		
//		writeToProcess("create");
//		writeToProcess("179346");
//		writeToProcess("327 Rulez");
//		
////		System.out.println("retail");
//		writer.write("logout\n");
//		writer.flush();
//		System.out.println(reader.readLine());
//		System.out.println("logout");
//		writer.close();
//		System.out.println(reader.readLine());
		
		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.contains("Enter a command:")) {
				System.out.println(line);
			}
		}
		
		p.destroyForcibly();
		
	}
	
	/**
	 * This method is used in testing automation.
	 * @param testFile The incoming test file containing the commands.
	 * @return Extracted commands
	 */
	private static ArrayList<String> parseTestInputs(File testFile){
		ArrayList<String> inputs = new ArrayList<String>();
		String line = "";
		try {
			BufferedReader read = new BufferedReader(new FileReader(testFile));
			//Skip to input lines
			for (int i = 0; i < 4; i++)
				read.readLine();
			int count = 0;
			while (!((line = read.readLine()).equals("\t-*"))){
				inputs.add(line);
			}
			read.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inputs;
	}

	
	private static void writeToProcess(String line) throws IOException {
		writer.write(line + "\n");
		writer.flush();
	}
	
	private static void Login(String[] a) throws IOException {
		attemptLogin(a[0], a[1]);
	}
	
	private static void attemptLogout() throws IOException {
		writer.write("logout\n");
		writer.flush();
	}
	
	private void readProcessOutput() throws IOException {
		System.out.println(reader.readLine());
	}
	
	private static void attemptLogin(String login, String perm) throws IOException {
		writer.write(login+"\n");
		writer.flush();
		
		writer.write(perm+"\n");
		writer.flush();
	}
	
}
