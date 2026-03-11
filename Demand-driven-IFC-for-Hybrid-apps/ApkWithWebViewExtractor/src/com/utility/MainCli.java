package com.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;


public class MainCli {
	
	public static List<File> collectFiles(File directory, String[] extensions) {
		ArrayList<File> files = new ArrayList<File>();

		System.out.println("Directoy is " + directory);
		if (directory.isDirectory()) {
		    try {

		         boolean recursive = true;
		         
		         Collection<File> foundApks = FileUtils.listFiles(directory, extensions, recursive);
		         System.out.println("Directoy is " + foundApks.size());
		         for (Iterator<File> iterator = foundApks.iterator(); iterator.hasNext();) {
		             files.add(iterator.next());
		         }   
		     } catch (Exception e) {
		         e.printStackTrace();
		     }   
		}

		return files;
	}
	
	
	public static int checkaddJSInterface(String destination) {
		int counter = 0;
		try {
			//Use apktool to extract the source
			ProcessBuilder pb = new ProcessBuilder("grep", "-ir", "Landroid/webkit/WebView;->addJavascriptInterface" , destination);
			Process p = pb.start();

				BufferedReader reader = 
				         new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				BufferedReader errReader = 
				         new BufferedReader(new InputStreamReader(p.getErrorStream()));
		       
				String line = "";
				while((line = reader.readLine()) != null) {
					//jsFilePath = line;
				//	System.out.print("File path is " + line + "\n");
					counter++;
		        }
				while((line = errReader.readLine()) != null) {
		            System.out.print(line + "\n");
		        }
				 p.waitFor();
		        
		} catch (IOException | InterruptedException e1) {
	        e1.printStackTrace();
	    }
		//System.out.println("Counter is " + counter);
		return counter;
		
	}

	public static void main(String[] args) {
		ArrayList<File> inputApplicationFiles = new ArrayList<File>();;
		
		inputApplicationFiles.addAll(collectFiles(new File(args[0]), new String[]{"apk"}));
		
		for(File f: inputApplicationFiles) {
			
		//	System.out.println(f.getAbsolutePath());
			dissembeApk(f.getAbsolutePath());
			if(checkaddJSInterface("output/apk/") != 0) {
				System.out.println("Found addJavascript Interface");
				moveApk(f.getAbsolutePath());
			}
			removeApk();
			
		}
		
		
	}
	
	public static void dissembeApk(String apkPath) {
		
		try {
			//Use apktool to extract the source
			ProcessBuilder pb = new ProcessBuilder("/usr/local/bin/apktool","d","-o", "output/apk/", apkPath);
			Process p = pb.start();

				BufferedReader reader = 
				         new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				BufferedReader errReader = 
				         new BufferedReader(new InputStreamReader(p.getErrorStream()));
		       
				String line = "";
				while((line = reader.readLine()) != null) {
		            
		        }
				while((line = errReader.readLine()) != null) {
					System.out.println(line + "\n");
		        }
				 p.waitFor();
		        
		} catch (IOException | InterruptedException e1) {
	        e1.printStackTrace();
	    }

	}
	
	public static void removeApk() {
		try {
			//Use apktool to extract the source
			ProcessBuilder pb = new ProcessBuilder("rm","-rf", "output/apk/");
			Process p = pb.start();

				BufferedReader reader = 
				         new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				BufferedReader errReader = 
				         new BufferedReader(new InputStreamReader(p.getErrorStream()));
		       
				String line = "";
				while((line = reader.readLine()) != null) {
		            
		        }
				while((line = errReader.readLine()) != null) {
					System.out.println(line + "\n");
		        }
				 p.waitFor();
		        
		} catch (IOException | InterruptedException e1) {
	        e1.printStackTrace();
	    }
		
	}
	
	public static void moveApk(String apkPath) {
		try {
			//Use apktool to extract the source
			ProcessBuilder pb = new ProcessBuilder("mv",apkPath, "relevantapk/");
			Process p = pb.start();

				BufferedReader reader = 
				         new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				BufferedReader errReader = 
				         new BufferedReader(new InputStreamReader(p.getErrorStream()));
		       
				String line = "";
				while((line = reader.readLine()) != null) {
		            
		        }
				while((line = errReader.readLine()) != null) {
					System.out.println(line + "\n");
		        }
				 p.waitFor();
		        
		} catch (IOException | InterruptedException e1) {
	        e1.printStackTrace();
	    }
		
	}

}
