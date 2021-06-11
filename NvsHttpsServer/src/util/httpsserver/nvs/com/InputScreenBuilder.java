package util.httpsserver.nvs.com;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class InputScreenBuilder {
	public Hashtable settings = null;
	protected SqlRequest sqlReq = null;
	
	public InputScreenBuilder(Hashtable settings) {
		this.settings = settings;
		sqlReq = new SqlRequest(settings);
	}
	
	public String buildScreen(String screen) {
		String out = "";

		String SQL = "SELECT * FROM DDIS_divs " + " WHERE screen='" + screen + "' ORDER BY id";
		
		List<Map<String, String>> records = sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {

			out += "<div ";
			out += " id='" + rec.get("divId") + "'";
			out += " style='position:absolute;";
			out += " top:" + rec.get("top") + "px;";
			out += " left:" + rec.get("left") + "px;";
			out += rec.get("style") + "'";
			out += ">";
			out += "<fieldset> ";
			out += "<legend>" + rec.get("labelText") + "</legend>";

			String SQL2 = "SELECT * FROM DDIS_fields " + " WHERE screen='" + screen + "'" + " AND divId = '"
					+ rec.get("divId") + "' AND active='X'";

			List<Map<String, String>> records2 = sqlReq.getSelect(SQL2);

			for (Map<String, String> rec2 : records2) {

				String inputType = rec2.get("tagType") + "_" + rec2.get("tagType2");

				inputType = inputType.toUpperCase();
				
				switch (inputType) {

				case "INPUT_BUTTON":

					out += "<label  for='" + rec2.get("elemId") + "'";
					out += " style='display:inline-block;width:" + rec.get("labelWidth") + "px;'";
					out += ">";
					out += "&nbsp;";
					out += "</label>";
					out += "<input style='float: right;' type='" + rec2.get("tagType2") + "'";
					out += " id='" + rec2.get("elemId") + "'";
					out += " value='" + rec2.get("labelText") + "' ";
					out += addFunctionsNameToElement(screen, rec2.get("divId"), rec2.get("elemId"));
					out += ">";

					break;

				case "INPUT_TEXT":
				case "INPUT_NUMBER":
				case "INPUT_DATETIME-LOCAL":
					
					out += "<label  for='" + rec2.get("elemId") + "'";
					;
					out += " style='display:inline-block;width:" + rec.get("labelWidth") + "px;'";
					out += ">";
					out += rec2.get("labelText");
					out += "</label>";

					out += "<input type='" + rec2.get("tagType2") + "'";
					out += " id='" + rec2.get("elemId") + "' ";
					out += " name='" + rec2.get("elemId") + "' ";
					out += rec2.get("params");

					out += ">";

					break;

				case "TEXTAREA_":
				case "TEXTAREA_TEXTAREA":
		
				 if(!rec2.get("labelText").isEmpty()) {
					
					out += "<label  for='" + rec2.get("elemId") + "'";
					out += " style='vertical-align:top;display:inline-block;width:" + rec.get("labelWidth") + "px;'";
					out += ">";
					out += rec2.get("labelText");
					out += "</label>";
				 }
					out += "<textarea ";
					out += " id='" + rec2.get("elemId") + "' ";
					out += " name='" + rec2.get("elemId") + "' ";
					out += rec2.get("params");

					out += ">";
					out += "</textarea>";					
				
					
	
				default:
					break;
					
					
				}

				int downSteps = Integer.valueOf(rec2.get("downSteps"));

				for (int i = 0; i < downSteps; i++) {
					out += "<br>";
				}

			}

			out += "</fieldset> ";
			out += "</div>";
			out += addElementHandlerFunctionsToPage(screen, rec.get("divId"));
			out += addCommonFunctionsToPage(screen);
			out += addGlobalFunctionsToPage();
		}

		return out;
	}

	public String addFunctionsNameToElement(String screen, String divId, String elemId) {
		String out = "";

		String SQL = "SELECT elemEvent from DDIS_functions " + " WHERE screen='" + screen + "' AND divId='" + divId
				+ "' AND elemId='" + elemId + "' " + " GROUP BY elemEvent ORDER BY id ";

		String functionText = "";

		List<Map<String, String>> records = sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {
			out += " on" + rec.get("elemEvent") + "=on" + rec.get("elemEvent") + "_" + divId + "_";
			out += elemId + "();";

		}

		return out;
	}

	public String addElementHandlerFunctionsToPage(String screen, String divId) {
		String out = "";

		out += "<script>";

		String SQL = "SELECT elemId,elemEvent FROM DDIS_functions" + " WHERE screen='" + screen + "' AND divId='"
				+ divId + "'" + " GROUP BY elemId,elemEvent ORDER BY id ";

		List<Map<String, String>> records = sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {
			out += "function on" + rec.get("elemEvent") + "_" + divId + "_";
			out += rec.get("elemId") + "(){ \n";

			String SQL2 = "SELECT text from DDIS_functions " + " WHERE screen='" + screen + "' AND divId='" + divId
					+ "'" + " AND elemId='" + rec.get("elemId") + "' AND elemEvent='" + rec.get("elemEvent")
					+ "' ORDER BY id";

			List<Map<String, String>> records2 = sqlReq.getSelect(SQL2);
			for (Map<String, String> rec2 : records2) {
				out += rec2.get("text") + "\n";

			}

			out += "} \n";
		}

		out += "</script>";
		return out;
	}

	public String addCommonFunctionsToPage(String screen) {
		String out = "";
		out += "<script>";
		String SQL = "SELECT text FROM DDIS_functions " + " WHERE screen='" + screen + 
				"' AND elemId=''  ORDER BY id";

		List<Map<String, String>> records = sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {
			out += rec.get("text") + "\n";

		}

		out += "</script>";
		return out;
	}

	public String addGlobalFunctionsToPage() {
		String out = "";		
		out += "<script>";
		String SQL = "SELECT text FROM DDGL_functions  WHERE active='X' " 
				+ " ORDER BY id";
		
		List<Map<String, String>> records = sqlReq.getSelect(SQL);
		
		for (Map<String, String> rec : records) {
			out += rec.get("text") + "\n";

		}		
		
		
		out += "</script>";		
		return out;		
	}
	
	
	
}
