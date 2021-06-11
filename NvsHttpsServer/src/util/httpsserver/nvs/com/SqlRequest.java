package util.httpsserver.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;




public class SqlRequest {
	public final Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
	public Hashtable settings = null;
	String connectionString = "";
	String user = "";
	String password = "";
	
	public SqlRequest(Hashtable settings) {
		this.settings = settings;
	}


	
	public  List<Map<String , String>> getSelect(String SQL){

		List<Map<String , String>> resordsMap  = new ArrayList<Map<String,String>>();
		
			if(SQL == null || SQL.isEmpty()) {		
				return resordsMap;
			}
		Connection conn = null ;
		
		
		
		try {
		      
			
			conn = DriverManager.getConnection( 
					(String)settings.get("db_connectionString"),
					(String)settings.get("db_user"),
					(String)settings.get("db_password"));
		      
		      	Statement stmt = conn.createStatement();
				
				ResultSet rs = stmt.executeQuery(SQL);

		        ResultSetMetaData rsmd = rs.getMetaData();
	            int cols = rsmd.getColumnCount();
				
	            
	        	String colName = "";
	            String colType = "";
	            String colLabel = "";
	            
	            
	            while (rs.next()) {


					Map<String,String> record = new HashMap<String, String>();
					
					
					for (int i=1;i<=cols;i++) {
		                
		            	colName = rsmd.getColumnName(i);
		                colType = rsmd.getColumnTypeName(i);
		                colLabel = rsmd.getColumnLabel(i);

		                

		                
		                if(colType.toUpperCase().contains("CHAR")) {
		                	
		                	record.put(colLabel, rs.getString(colLabel));
		                	
		                }else if (colType.toUpperCase().contains("INT")){
		                	
		                	record.put(colLabel, String.valueOf(rs.getInt(colLabel)));
			            
//		                	this.logger.info("colLabel = " + colLabel + " = " + rs.getInt(colLabel) );                	
		                	

		                }else if (colType.toUpperCase().contains("FLOAT")){
			                	
			                record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));
			                
		                }else if (colType.toUpperCase().contains("DATETIME")){
		                	
		                	record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));				                	

		                }else if (colType.toUpperCase().contains("TIMESTAMP")){
		                	
		                	record.put(colLabel, String.valueOf(rs.getTimestamp(colLabel)));	                

		                }else if (colType.toUpperCase().contains("DOUBLE")){
	                	
		                	record.put(colLabel, String.valueOf(rs.getFloat(colLabel)));	                
		                }else {
		                
		                	
		                	record.put(colLabel, colType);
		                	
		                	logger.info(colType.toUpperCase() + " is unknown colName=" + colName + " colType=" + colType + " colLabel=" + colLabel );
		                	
		                }
		                 
		            }							

					
					resordsMap.add(record);

				}

				
//	            System.out.println("resordsMap " + resordsMap.size());
	            conn.close();
				
		
		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			logger.severe(SQL+ " \n " + errors.toString());

		} finally {	
			try {
				conn.close();
			} catch (SQLException e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				logger.severe(SQL+ " \n " + errors.toString());
			}
		}	
		
		return resordsMap;
	}	
	

public void saveResult (List<String> sqlList) {
	
	
	if (sqlList == null) return;
	
	if (sqlList.size() == 0) return;
	
	Connection conn = null ;
			
			try {
				
					conn = DriverManager.getConnection( 
							(String)settings.get("db_connectionString"),
							(String)settings.get("db_user"),
							(String)settings.get("db_password"));			      	
			      
			      
			      for(String s: sqlList) {
			      		Statement stmt = conn.createStatement();
					    stmt.executeUpdate(s);
					}


					conn.close();
					
			
			} catch (Exception e) {

				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				logger.severe(errors.toString());

			} finally {	
				try {
					conn.close();
				} catch (SQLException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					logger.severe(errors.toString());
				}
			}
	
}			
public void saveResult (String sql) {
	
	
	if (sql == null) return;
	if (sql.isEmpty()) return;	

	
	Connection conn = null ;
			
			try {
				
					conn = DriverManager.getConnection( 
							(String)settings.get("db_connectionString"),
							(String)settings.get("db_user"),
							(String)settings.get("db_password"));			      	
			      
			      

			      		Statement stmt = conn.createStatement();
					    stmt.executeUpdate(sql);



					conn.close();
					
			
			} catch (Exception e) {

				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				logger.severe(errors.toString());

			} finally {	
				try {
					conn.close();
				} catch (SQLException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					logger.severe(errors.toString());
				}
			}
	
}	
}
