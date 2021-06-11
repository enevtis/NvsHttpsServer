package util.httpsserver.nvs.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Util {

	public static String decode(String url)  
    {  
              try {  
                   String prevURL="";  
                   String decodeURL=url;  
                   while(!prevURL.equals(decodeURL))  
                   {  
                        prevURL=decodeURL;  
                        decodeURL=URLDecoder.decode( decodeURL, "UTF-8" );  
                   }  
                   return decodeURL;  
              } catch (UnsupportedEncodingException e) {  
                   return "Issue while decoding" +e.getMessage();  
              }  
    } 
	public static void SaveToLog(String outText, String fileName, boolean append) {
		
		String fullFileName = System.getProperty("user.dir") + File.separator + "log" +  File.separator + fileName + ".log";
		
		  try {
			File fileOut = new File(fullFileName);
				
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();
			
			

			FileWriter fr = new FileWriter(fileOut, append);
			BufferedWriter br = new BufferedWriter(fr);
			br.write(dateFormat.format(date) + ": " + outText + "\r\n");

			br.close();
			fr.close();
			
				
		    } 
		   catch (Exception e) 
		   {
			System.out.println(e.getMessage());
		   } 

	}
	public static String runOsCommand2(String cmdString, Logger logger) {

		String result = "";

		try {

			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(cmdString);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			// Read the output from the command

			logger.info("command = " + cmdString);

			String s = null;

			while ((s = stdInput.readLine()) != null) {

				result += s;
			}

			// Read any errors from the attempted command

			while ((s = stdError.readLine()) != null) {

				result += s;
			}

			logger.info("output = " + result);

		} catch (Exception e) {

			logger.info("error " + e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			logger.severe(errors.toString());
			e.printStackTrace();
			result = "E";
		}
		return result;

	}

	public static String runSuCommand(String user, String command, Logger logger) {

		String result = "";

		try {

			ProcessBuilder pb = new ProcessBuilder("su", "-", user, "-c", command);
			pb.redirectErrorStream(true);

			Process p = pb.start();

			logger.info("user=" + user + " command = " + command);

			int retCode = p.waitFor();

			logger.info(" command = " + command + "return code = " + retCode);

			Reader reader = new InputStreamReader(p.getInputStream());
			int ch;
			while ((ch = reader.read()) != -1) {
				result += ((char) ch);

			}
			reader.close();

			logger.info(" command = " + command + " output = " + result);

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			logger.severe(errors.toString());
			e.printStackTrace();
			result = "E";
		}

		return result;

	}
	
	public static String getCurrentTime() {

		return new SimpleDateFormat("HH:mm:ss  dd.MM.yyyy").format(new Date());

	}

}
