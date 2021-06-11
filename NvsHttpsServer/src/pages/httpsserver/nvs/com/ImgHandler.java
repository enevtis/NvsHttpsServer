package pages.httpsserver.nvs.com;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;

import javax.net.ssl.SSLSocket;

public class ImgHandler extends NvsHttpsServerHandlerTemplate {

	public ImgHandler(Object gData, Hashtable settings) {
		super(gData, settings);
	}

	public void getResponse(SSLSocket socket, String paramsString) {

		try {

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			String pathToFile = "/resources" + paramsString;

			Class c = Class.forName((String) settings.get("mainClass"));

			InputStream in = c.getResourceAsStream(pathToFile);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;

			byte[] data = new byte[1024];

			while ((nRead = in.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
			byte[] byteArray = buffer.toByteArray();

			out.writeBytes("HTTP/1.0 200 Document Follows\r\n");

			String ext = pathToFile.split("\\.")[1];
			out.writeBytes("Content-Type: image/" + ext + "\r\n");

			out.writeBytes("Content-Length: " + byteArray.length + "\r\n");
			out.writeBytes("\r\n");

			out.write(byteArray, 0, byteArray.length);

			socket.close();

		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.severe(errors.toString());
		}

	}

}
