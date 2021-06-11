package pages.httpsserver.nvs.com;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.List;

import javax.net.ssl.SSLSocket;

public class LogReaderHandler extends NvsHttpsServerHandlerTemplate {

	private String SQL = "";

	public LogReaderHandler(Object gData, Hashtable settings) {
		super(gData, settings);

	}
	public String getPage() {
		String out = "";
		int lines = 1000;
		out += getBeginPage();
		out += strTopPanel("Last " + lines + " lines of log:");
		String fileName = System.getProperty("user.dir") + File.separator + "log" + File.separator + "logger.trc";

		out += getLastNLogLines(new File(fileName), lines);

		out += getEndPage();

		return out;
	}

	public String getLastNLogLines(File file, int nLines) {
		StringBuilder s = new StringBuilder();
		try {
			Process p = Runtime.getRuntime().exec("tail -" + nLines + " " + file);
			java.io.BufferedReader input = new java.io.BufferedReader(
					new java.io.InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				s.append(line + "<br>");
			}
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		return s.toString();
	}

}
