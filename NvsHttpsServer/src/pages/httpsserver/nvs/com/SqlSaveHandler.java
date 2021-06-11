package pages.httpsserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;



public class SqlSaveHandler extends NvsHttpsServerHandlerTemplate {

	String postData = "";

	public SqlSaveHandler(Object gData,Hashtable settings) {
		super(gData, settings);

	}

	public void getResponse(SSLSocket socket, String paramsString, String postData) {

		this.postData = postData;
		parseParams(paramsString);
		String resp = getPage();

		
		try {

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			out.write(header200());
			out.write("Content-Length: " + resp.getBytes("UTF-8").length + "\r\n");
			out.write("\r\n");
			out.write(resp);

			out.close();

		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.severe(errors.toString());
		}

	}

	public String getPage() {
		String out = "";

		String parts[] = postData.split(";");
		List sqlList = new ArrayList();

		for (String s : parts) {
			sqlList.add(cleanInvalidCharacters(s));
		}

		sqlReq.saveResult(sqlList);

		return out;
	}

	public String cleanInvalidCharacters(String in) {
		StringBuilder out = new StringBuilder();
		char current;
		if (in == null || ("".equals(in))) {
			return "";
		}
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i);
			if ((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF))
					|| ((current >= 0xE000) && (current <= 0xFFFD))
					|| ((current >= 0x10000) && (current <= 0x10FFFF))) {
				out.append(current);
			}

		}
		return out.toString().replaceAll("\\s", " ");
	}
}
