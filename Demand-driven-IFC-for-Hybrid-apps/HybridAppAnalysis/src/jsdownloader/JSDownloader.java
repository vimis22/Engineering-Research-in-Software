package jsdownloader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;  
import java.util.StringTokenizer;
import org.jsoup.Jsoup; 
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

import de.potsdam.constants.GenericConstants;

public class JSDownloader {
	
	public static void getJSDetails() {
		
		Connection c = null;
		Statement stmt = null;
		String sql = "select jsdetails.PASS_STRING, webview_prime.intefaceObject, webview_prime.appName from jsdetails, webview_prime where jsdetails.ACTIVITY_NAME = webview_prime.initiatingClass;";
		//ArrayList<String> jsString = new ArrayList<String>();
		String jsString = new String();
		String ifcObj = "";
		String appName = "";

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection(GenericConstants.DB_NAME);
			c.setAutoCommit(false);

			stmt = c.createStatement();
			
		//	System.out.println("sql is " + sql);
			
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				jsString  = rs.getString("PASS_STRING");
				ifcObj = rs.getString("intefaceObject");
				appName = rs.getString("appName");
				extractJS(jsString, ifcObj, appName);
			}
			
			 stmt.close();
			 c.commit();
			 c.close();
			
			
		}catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public static void removeDuplicates() {
		
		Connection c = null;
		Statement stmt = null;
		String sql = "insert into webview_new select distinct * from webview_prime;";
		
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
			 
			 sql = "insert into webview_prime select distinct * from webview_new;";
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
	
	public static void getAltJSDetails() {
	
		Connection c = null;
		Statement stmt = null;
		String sql = "select appName, intefaceObject from webview_prime order by appName;";
		String jsString = new String();
		ArrayList<String> ifcObj = new ArrayList();
		String appName = "";
		String dummy  = "";
		boolean flag = true;
		
		try {
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(GenericConstants.DB_NAME);
		c.setAutoCommit(false);

		stmt = c.createStatement();
		
		ResultSet rs = stmt.executeQuery(sql);
		
		while(rs.next()) {
			
			ifcObj.add(rs.getString("intefaceObject"));
			appName = rs.getString("appName");
			
			if(flag) {
				dummy = appName;
				flag = false;
			}
			if(!dummy.contains(appName)) {
				extractAltJS(ifcObj, dummy);
				ifcObj.clear();
				dummy = appName;
			}	
			
		}
			extractAltJS(ifcObj, dummy);
		
		
		 stmt.close();
		 c.commit();
		 c.close();
		
		
		}catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}
	
	public static void extractAltJS(ArrayList<String> ifcObj, String appName) {
		
		String path = "output/intermediate/";
		Integer counter = 0;
		path = path.concat(appName+"/");
		ArrayList<String> jsFilePath = findJSscript(path, appName);
		String ifcFileName = "";
		String dummyName = "";

		ifcObj = duplicateRemover(ifcObj);
		
		
		for(String ifcobj:ifcObj) {
			ifcFileName = ifcFileName.concat(ifcobj + "#");	
		}
		ifcFileName = ifcFileName.replaceAll("#$", "");
		
	//	System.out.println("Size is " + jsFilePath.size());
		
		for(String jsFile: jsFilePath) {
			dummyName = appName + Integer.toString(counter);
			copyJsFromApp(jsFile, ifcFileName, dummyName);
			counter++;
		}
		
		//counter = 0;
		
		
	}
	
	public static ArrayList<String> duplicateRemover(ArrayList<String> ifcObj){
		
		Set<String> set = new LinkedHashSet<>();
		set.addAll(ifcObj);
		ifcObj.clear();
		ifcObj.addAll(set);
		
		return ifcObj;
	}
	
	public static ArrayList<String> findJSscript(String path, String appName) {

		ArrayList<String> jsFilePath = new ArrayList();
		try {
			//Use apktool to extract the source
			ProcessBuilder pb = new ProcessBuilder("find", path, "-name", "*.js");
			Process p = pb.start();

				BufferedReader reader = 
				         new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				BufferedReader errReader = 
				         new BufferedReader(new InputStreamReader(p.getErrorStream()));
		       
				String line = "";
				while((line = reader.readLine()) != null) {
					jsFilePath.add(line);
				//	System.out.print("File path is " + line + "\n");
		        }
				while((line = errReader.readLine()) != null) {
		            System.out.print(line + "\n");
		        }
				 p.waitFor();
		        
		} catch (IOException | InterruptedException e1) {
	        e1.printStackTrace();
	    }
		return jsFilePath;
	}

	
	public static void extractJS(String jsString, String ifcObj, String appName) {
		
		String path = "output/intermediate/";
		path = path.concat(appName+"/");
		boolean flag = false;
		String script = "";
		if(jsString.contains("asset") && jsString.contains(".html")) {
			//System.out.println("correctly parsed JsString " + jsString);
			StringTokenizer st  = new StringTokenizer(jsString, "///");

			while(st.hasMoreElements()) {
			//	System.out.println("St is " + st.nextToken());
				if(!flag)
					st.nextToken();
				if(flag) {
					path = path.concat(st.nextToken()+"/");
				}
				flag = true;
			}
			path = path.replace("\"/", "");
			if(path.contains("android_asset"))
				path = path.replace("android_asset", "assets");
			else
				path = path.replace("asset", "assets");
		//	System.out.println("Correct path is " + path);
			parseHtml(path, ifcObj, appName);
		}
		
		if(jsString.contains("javascript:")) {
		//	System.out.println("jsString is " + jsString);
			String array[] = jsString.split("javascript:");

			for(String token : array) {
				script = token;
			//	System.out.println("script is " + script);
			}
		/*	StringTokenizer st  = new StringTokenizer(jsString, "javascript:");
			while(st.hasMoreElements()) {
				st.nextToken();
				script = st.nextToken();
				System.out.println("script is " + script);
				
			}*/
			
			
		}
				
	}
	
	public static void parseHtml(String fileName, String ifcObj, String appName) {
		
		try {
		//	Document doc = Jsoup.connect(fileName).get();
			//Document doc = Jsoup.parse(new File("e:\\register.html"),"utf-8");
		//	String absoluteAssetPath = fileName.replace()
			
			Document doc = Jsoup.parse(new File(fileName),"utf-8");
			//String title =  doc.attr("script");
			//System.out.println("title is: " + title);  
			
			Elements loginform = doc.getElementsByTag("script");  
			
			for (Element inputElement : loginform) {  
				String type = inputElement.attr("type");
				
				if(type.contains("text/javascript")) {
					System.out.println("type is  " +  type);
					String value = inputElement.attr("src");
					
					if(value.contains(".js")) {
						//System.out.println("JS is  " +  value);
						StringTokenizer st  = new StringTokenizer(value, "/");
						while(st.hasMoreElements())
							value = st.nextToken();
						String jsFilePath = findAbsolutePath(value, appName);
						copyJsFromApp(jsFilePath, ifcObj, appName);
						
					}
				}
			
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String findAbsolutePath(String js, String appName) {
		
		String path = "output/intermediate/";
		path = path.concat(appName+"/");
		String jsFilePath = "";
		try {
			//Use apktool to extract the source
			ProcessBuilder pb = new ProcessBuilder("find", path, "-name", js);
			Process p = pb.start();

				BufferedReader reader = 
				         new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				BufferedReader errReader = 
				         new BufferedReader(new InputStreamReader(p.getErrorStream()));
		       
				String line = "";
				while((line = reader.readLine()) != null) {
					jsFilePath = line;
					System.out.print("File path is " + line + "\n");
		        }
				while((line = errReader.readLine()) != null) {
		            System.out.print(line + "\n");
		        }
				 p.waitFor();
		        
		} catch (IOException | InterruptedException e1) {
	        e1.printStackTrace();
	    }
		return jsFilePath;
		
	}
	
	public static void copyJsFromApp(String jsFilePath, String ifcObj, String appName) {
		String destination = "JSCode/" + appName + "#" + ifcObj + ".js";  
		//System.out.print("source is " + jsFilePath + "\n");
		//System.out.print("destination is " + destination + "\n");
		try {
			//Use apktool to extract the source
			ProcessBuilder pb = new ProcessBuilder("cp", jsFilePath, destination);
			Process p = pb.start();

				BufferedReader reader = 
				         new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				BufferedReader errReader = 
				         new BufferedReader(new InputStreamReader(p.getErrorStream()));
		       
				String line = "";
				while((line = reader.readLine()) != null) {
					//jsFilePath = line;
					System.out.print("File path is " + line + "\n");
		        }
				while((line = errReader.readLine()) != null) {
		            System.out.print(line + "\n");
		        }
				 p.waitFor();
		        
		} catch (IOException | InterruptedException e1) {
	        e1.printStackTrace();
	    }
		
		
	}
	

}
