package pages.httpsserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLSocket;

import simplecrypto.nvs.com.SimpleCrypto;


public class FreeRequestHandler extends NvsHttpsServerHandlerTemplate {

	String test = "";

//	public List<GraphJsObject> jsObjects = new ArrayList<GraphJsObject>();


	public FreeRequestHandler(Object gData,Hashtable settings) {
		super(gData, settings);

	}
	
	public void getResponse(SSLSocket socket, String paramsString) {

		test = paramsString;

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
		String action=params.get("action");
		
		switch(action) {
		
		case "get_guid":
			out = UUID.randomUUID().toString();
			break;
		case "get_hash":
			out = getHashFromPassword(params.get("pass"));
			break;
		default:
			break;			
		}
		
		
		return out;
	}
	public String getHashFromPassword(String password) {

		String out = "";
		try {
			out = SimpleCrypto.encrypt((String)settings.get("SecretKey"), password);
		} catch (Exception e1) {

			StringWriter errors = new StringWriter();
			e1.printStackTrace(new PrintWriter(errors));
			logger.severe(errors.toString());

		}
		return out;
	}
}
