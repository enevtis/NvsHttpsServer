package pages.httpsserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import com.sun.net.httpserver.HttpExchange;

import util.httpsserver.nvs.com.SqlRequest;



public class NvsHttpsServerHandlerTemplate {

	public Map<String, String> params;
	public String caption = "";
	public final Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
	public Hashtable settings = null;
	public Object gData = null;
	public SqlRequest sqlReq = null;

	public NvsHttpsServerHandlerTemplate(Object gData, Hashtable settings) {
		this.gData = gData;
		this.settings = settings;
		sqlReq = new SqlRequest(this.settings);
		
	}
	public NvsHttpsServerHandlerTemplate() {
	}
	public void getResponse(SSLSocket socket, String paramsString) {

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

		return out;
	}

	public Map<String, String> queryToMap(String query) {

		if (query == null || query.isEmpty())
			return null;

		Map<String, String> result = new HashMap<>();
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				result.put(entry[0], entry[1]);
			} else {
				result.put(entry[0], "");
			}
		}
		return result;
	}

	public void parseParams(String requestString) {

		String strForParse = "";
		String[] parts = requestString.split("\\?+");

		if (parts.length == 2) {
			params = queryToMap(parts[1]);
		} else {
			params = queryToMap(requestString);

		}

	}

	public String header200() {
		String out = "";
		out += "HTTP/1.0 200 OK\r\n";
		out += "Content-Type: text/html; charset=utf-8\r\n";

		return out;
	}

	public String header302(String location) {
		String out = "";
		out += "HTTP/1.0 302 Redirect\r\n";
		out += "Location:" + location + "\r\n";
		out += "Content-Type: text/html; charset=utf-8\r\n";
		return out;
	}

	protected String getBeginPage() {
		String out = "";
		out += "<!DOCTYPE html> \n";
		out += "<html> \n";
		out += "<head> \n";
		out += "<meta charset=\"utf-8\"> \n";
		out += "<link rel=\"icon\" href=\"/img/nvs.png\"> \n";
		out += "<script src=\"/src/greenex.js\"></script> \n";
		out += "<link rel=\"stylesheet\" href=\"/src/style.css\"> \n";
		out += "</head>";
		out += "<body>";
		return out;
	}

	protected String getEndPage() {
		String out = "";
		out += " </body> \n";
		out += " </html>  \n";
		return out;
	}

	public String strTopPanel(String text) {
		String out = "";
		out += strPopupMenu();
		out += "<p class='caption1'>&nbsp;&nbsp;" + text + "</p><br>";

		return out;
	}

	public String strPopupMenu() {
		String out = "";
		String link = "";

		out += "<img id='pic_more' src='/img/menu.png' title='menu'";

		out += " style='cursor:pointer;' ";
		out += " onclick='showMenu(this.id)';\" ";
		out += ">";

		out += "<div class='menu'>  \n";

		String SQL = "select * from global_menu where lang='" + 
		settings.get("lang") + "'" + " order by id";

		List<Map<String, String>> records = sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {

			out += "<div id='menu_" + rec.get("id") + "' class='menu-item' onclick=\"window.open('" + rec.get("link")
					+ "', '_blank');\">" + rec.get("description") + "</div>  \n";

		}

		out += "</div>  \n";

		return out;
	}

}
