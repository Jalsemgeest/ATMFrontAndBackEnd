package testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class RunBackend {

	private static final String TRANSACTIONS = "Z:/327/Transactions";
	
	private static String MERGED_TRANSACTION_SUMMARY_FILE = "Z:/327/TransactionsMerged/transaction_summary_file_merged_";
	
	private static String BACK_END_JAR = "Z:/327/BackEnd.jar";
	
	static BufferedReader reader;
	static PrintWriter writer;
	
	public static void main(String[] args) throws InterruptedException {
		
		File transDir = new File(TRANSACTIONS);
		
		String dateInfo = (new Date().toInstant().atZone(ZoneId.of("America/Toronto"))).toString().replace("[America/Toronto]", "").replace(":","");
	
		MERGED_TRANSACTION_SUMMARY_FILE += dateInfo + ".txt";

		try {
			
			writer = new PrintWriter(new File(MERGED_TRANSACTION_SUMMARY_FILE));

			for (String fileName : transDir.list()) {
				// If the file does not contain merged. ie. The file will not be the merged transaction summary file from a previous run.
				if (!fileName.contains("merged")) {
					File summaryFile = new File(TRANSACTIONS + "/" + fileName);
					reader = new BufferedReader(new FileReader(summaryFile));
					String line;
					while ((line = reader.readLine()) != null) {
						if (!line.equals("00")) {
							writer.println(line);
						}
					}
					reader.close();
					summaryFile.delete();
				}
			}
			writer.println("00");
			writer.close();
			
		} catch (IOException e) {
			System.out.println("Failed to read/write to file: " + e.getLocalizedMessage());
		}
		
		try {
			Process p = Runtime.getRuntime().exec("java -jar " + BACK_END_JAR + " " + MERGED_TRANSACTION_SUMMARY_FILE);
			
			Thread.sleep(2000);
			
			File newMaster = new File("Z:/master_accounts_file2.txt");
			if (newMaster.exists()) {
				new File("Z:/master_accounts_file.txt").renameTo(new File("Z:/master_accounts_file_" + dateInfo + ".txt"));
				newMaster.renameTo(new File("Z:/master_accounts_file.txt"));
			}
			
		} catch (IOException e) {
			System.out.println("Running back end failed: " + e.getLocalizedMessage());
		}
		
		
		
		
	}
	
}
