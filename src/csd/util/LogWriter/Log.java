package csd.util.LogWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;

import csd.Config;

/**
 * @author fangyixiang
 * @date Jul 31, 2015
 * Log file: automatic appending the logs
 */
public class Log {
	private static String fileName = Config.logFilePath;
	
	public static void log(String msg) {
		try {
			Date date = new Date();
			String time = date.toLocaleString();
			
			//true means that it will append the output
			BufferedWriter stdout = new BufferedWriter(new FileWriter(fileName, true));
			stdout.write(time);//output the log time for tracking
			stdout.write("\t");
			stdout.write(msg);
			stdout.newLine();
			
			stdout.flush();
			stdout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		Log.log("I love you");
	}
}
