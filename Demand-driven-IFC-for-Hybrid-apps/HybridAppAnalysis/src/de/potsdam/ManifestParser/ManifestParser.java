/**
 * 
 */
package de.potsdam.ManifestParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import de.potsdam.ApplicationDetails.ApplicationDetails;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 * @author abhishektiwari
 *
 */
public class ManifestParser {
	
	public void parseManifest(String applicationPath, ApplicationDetails appDetails, Logger logger) {
		
		logger.info("Inside parseManifest");
		String targetAction = null;
		String activityName = null;
		String scheme  = null;
		String host = null;
		String port = null;
		String url = null;
		String mime_type = null;
		List<String> action_list = new ArrayList();
		//System.out.println("Manifest file path is "+ applicationPath);
		//remove apk name from the application absolute path
		applicationPath = applicationPath.replace(".apk", "");
		try {
			InputStream file = new FileInputStream(applicationPath+"/AndroidManifest.xml");
			logger.info("Path of Manifest file : " + applicationPath+"/AndroidManifest.xml");
			
			XmlPullParserFactory xmlFactoryObject;
			xmlFactoryObject = XmlPullParserFactory.newInstance();
			XmlPullParser myParser = xmlFactoryObject.newPullParser();
			myParser.setInput(file, null);
			
			int event = myParser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT)  {
			   String name=myParser.getName();

			   switch (event){
			      case XmlPullParser.START_TAG:
			    	  
			    	  if(name.equals("manifest")){
			    		  appDetails.setPackageName(myParser.getAttributeValue(null,"package"));
			    		  logger.info("Package Name" + appDetails.getPackageName());
			    	  }
			    	  if(name.equals("uses-permission")){
			    		//  appDetails.permission = myParser.getAttributeValue(null,"android:name");
			    		 // appDetails.logger.info("App Permissions" + appDetails.packageName);
			    	  }
			    	  if(name.equals("activity") || name.equals("service") || name.equals("receiver") || name.equals("provider")){
			    		  String activityWithPackageName = myParser.getAttributeValue(null,"android:name");
			    		
			    		  if(activityWithPackageName!=null)
			    			  activityName = removePackageFromActivity(activityWithPackageName);
			    		  else {
			    			  activityWithPackageName = myParser.getAttributeValue(null,"name");
			    			//  System.out.println("activityWithPackageName here is " + activityWithPackageName);
			    			  activityName = removePackageFromActivity(activityWithPackageName);
			    		  }
			    		  
			    		 logger.info("App actvity/service/receiver/provider/" + activityName);
			    		  
			    		  extractActivityFolderPath(activityWithPackageName, appDetails.getActivityPath());	
			    	  }
					    if(name.equals("action")){
					    	  targetAction = myParser.getAttributeValue(null,"android:name");
					    	  logger.info("Target Action for this activity: " + targetAction);
					    	  action_list.add(targetAction);
					    }
					    if(name.equals("data")){
					    	scheme = myParser.getAttributeValue(null,"android:scheme");
					    	host = myParser.getAttributeValue(null,"android:host");
					    	port = myParser.getAttributeValue(null,"android:port");
					    	if(scheme != null && host != null && port != null){
					    	url = scheme + "://" + host + ":" + port;
					    	action_list.add(url);  
					    	}
					    	mime_type = myParser.getAttributeValue(null,"android:mimeType");
					    	if(mime_type != null)
					    		action_list.add(mime_type);
					    }
					
			      break;
			      
			      case XmlPullParser.END_TAG:
			    	  if(name.equals("activity") || name.equals("service") || name.equals("receiver") || name.equals("provider")){
			    		  List<String> action_list1 = new ArrayList<String>(action_list);
			    		  
			    		  appDetails.getHmap().put(activityName, action_list1);
			    		  action_list.clear();
			    	  }
			     break;
			   }		 
			   event = myParser.next(); 					
			}
		} catch (XmlPullParserException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String removePackageFromActivity(String activity){
		
		StringTokenizer st = new StringTokenizer(activity,".");
		while(st.hasMoreElements()){
			activity = st.nextToken();
		}
		return activity;
	}
	
	public void extractActivityFolderPath(String activity, List<String> activity_path){

		StringTokenizer st = new StringTokenizer(activity,".");
		activity = "";
		int activityRemovalCounter = st.countTokens();
		
		while(st.hasMoreElements() && activityRemovalCounter > 1){
			activity += st.nextToken();
			activity += "/";
			activityRemovalCounter--;
		}
	//	System.out.println("activity is "+ activity);
		activity_path.add(activity);
	 }
}
