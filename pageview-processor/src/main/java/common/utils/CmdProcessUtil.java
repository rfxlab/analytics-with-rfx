package common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.exception.ExceptionUtils;

import rfx.core.util.LogUtil;
import rfx.core.util.StringPool;

public class CmdProcessUtil {

	static class Worker extends Thread {
		private final Process process;
		private Integer exit;

		private Worker(Process process) {
			this.process = process;
		}

		@Override
		public void run() {			
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}						
		}
	}
	

	public static String executePython3Script(String cmd, int timeout) {
		//System.out.println(cmd);
		Process process = null;
		Worker worker = null;
		String rs = StringPool.BLANK;
		try {			
			Runtime runtime = Runtime.getRuntime();
			process = runtime.exec(cmd);
			worker = new Worker(process);
			worker.start();
			worker.join(timeout);
			if (worker.exit != null) {
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				// System.out.printf("Output of running %s is:", Arrays.toString(args));
				StringBuilder s = new StringBuilder();
				while ((line = br.readLine()) != null) {
					s.append(line).append("\n");
				}
				rs = s.toString();
			} else {
				System.err.println("timeout");
			}
		} catch (Exception ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			if( ! (ex instanceof InterruptedException) ){
				String er = ExceptionUtils.getStackTrace(ex);				
				LogUtil.e("CmdProcessUtil.stack", er);
			}
		} finally {
			if(process != null){
				process.destroy();
			}
		}
		return rs;
	}

	public static void main(String args[]) throws IOException,
			InterruptedException {

		String scriptpath = "python3 multilang/resources/hello.py abc xyz 111";
		String scriptpath1 = "python3 scripts/fraud-click-filter/clickprocess.py 127.0.0.1 1300000 b90d94578d1a527500b3c0a2970f0a6515fcc70d 1321321 86";

		String rs = executePython3Script(scriptpath1, 2000);
		System.out.println("rs: " + rs);

	}
}
