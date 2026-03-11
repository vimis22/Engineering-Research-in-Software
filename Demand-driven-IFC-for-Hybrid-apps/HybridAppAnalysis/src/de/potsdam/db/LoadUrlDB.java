/**
 * 
 */
package de.potsdam.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import de.potsdam.constants.GenericConstants;
import de.potsdam.main.ApplicationAnalysis;

/**
 * @author abhishektiwari
 *
 */
public class LoadUrlDB {

	
	public static void storeIntentDetails(ApplicationAnalysis appAnalyzer, String className, String rawJsString){
		
		appAnalyzer.getLogger().getLogger().info("Inside storeIntentDetails");
		if(rawJsString.contains("'")){
			rawJsString = rawJsString.replace("'", "''");
			System.out.println(rawJsString);
			
		}
		
		Connection c = null;
		Statement stmt = null;
		String sql = "INSERT INTO jsdetails (PACKAGE_NAME,ACTIVITY_NAME,PASS_STRING) VALUES (";
		
		
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection(GenericConstants.DB_NAME);
			c.setAutoCommit(false);

			stmt = c.createStatement();
			
			sql = sql + "'" + appAnalyzer.getAppDetails().getPackageName() + "'" + ", ";
			sql = sql + "'" + className +  "'" + ", ";
			sql = sql + "'" + rawJsString +  "'" + " );";
			appAnalyzer.getLogger().getLogger().info(sql);
			//System.out.println("sql is " + sql);
			stmt.executeUpdate(sql);
			
			 stmt.close();
			 c.commit();
			 c.close();
			
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void initDB(){
		
		Connection c = null;
		Statement stmt = null;
		String sql = "delete from jsdetails;";
		
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection(GenericConstants.DB_NAME);
			c.setAutoCommit(false);

			stmt = c.createStatement();
		//	System.out.println("sql is " + sql);
			stmt.executeUpdate(sql);
			
			 stmt.close();
			 c.commit();
			 
			 
			 sql = "delete from webview_prime;";
			 stmt = c.createStatement();
			 stmt.executeUpdate(sql);
			 stmt.close();
			 c.commit();
			 
			 sql = "delete from webview_new;";
			 stmt = c.createStatement();
			 stmt.executeUpdate(sql);
			 stmt.close();
			 c.commit();
			 
			 c.close();
			
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
