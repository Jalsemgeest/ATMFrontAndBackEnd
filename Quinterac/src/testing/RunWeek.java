package testing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class RunWeek {

	private static final String DAYS = "Z:/327/Days";
	
	private static final String RUN_FRONT_END = "Z:/327/RunFrontEnd.jar";
	private static final String RUN_BACK_END = "Z:/327/RunBackEnd.jar";
	
	public static void main(String[] args) throws IOException {
		
		File days = new File(DAYS);
		File[] arr = days.listFiles();
		Arrays.sort(arr);
		for (File fileDir : arr) {
			try {
				for (File fileName : fileDir.listFiles()) {
					// Run the Front End for all of these.
					Process p = Runtime.getRuntime().exec("java -jar " + RUN_FRONT_END + " " + fileName.getAbsoluteFile().toString().replace(" ", ""));
					Thread.sleep(2000);
				}
//				 Run the back end.
				Process p = Runtime.getRuntime().exec("java -jar " + RUN_BACK_END);
				Thread.sleep(2000);
				
			} catch (Exception e) {
				System.out.println("Failed running: " + e.getLocalizedMessage());
			}
		}
		
	}
	
}
