/**
 * 
 */
package de.potsdam.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.potsdam.ApplicationDetails.ApplicationDetails;
import de.potsdam.evaluateJavascript.EvaluateJavaScriptHandler;
import de.potsdam.Logging.IIFALogger;
import de.potsdam.ManifestParser.ManifestParser;
import de.potsdam.SmaliContent.SmaliContent;
import de.potsdam.constants.GenericConstants;
import de.potsdam.db.LoadUrlDB;
import de.potsdam.extract.ApkToolHandler;
import de.potsdam.extract.CollectClasses;
import de.potsdam.extract.InputApkFileContainer;
import de.potsdam.loadurl.LoadURLAnalyzer;
import de.potsdam.slicer.Slicer;
import jsdownloader.JSDownloader;

/**
 * @author abhishektiwari
 *
 */
public class ApplicationAnalysis {
	
	private InputApkFileContainer fileContainer;
	private ApplicationDetails appDetails;
	private ApkToolHandler apkToolHandler;
	private SmaliContent smaliData;
	private ManifestParser manifestParser;
	private IIFALogger logger;
	private String logDirectory;
	public static int appCounter;
	
	static{
		appCounter = 0;
	}
	
	public ApplicationAnalysis(){
		this.appDetails = new ApplicationDetails();
		this.apkToolHandler = new ApkToolHandler();
		this.smaliData = new SmaliContent();
		this.manifestParser = new ManifestParser();
		this.logger = new IIFALogger();
		this.logDirectory = normalizeLogDirectory(GenericConstants.DEFAULT_LOG_DIRECTORY);
	}
	
	public ApplicationAnalysis(File inputDirectory){
		this();
		this.fileContainer = new InputApkFileContainer(inputDirectory);
	}
	
	public ApplicationAnalysis(File inputDirectory, String logDirectory){
        this();
        this.fileContainer = new InputApkFileContainer(inputDirectory);
        this.setLogDirectory(logDirectory);
    }

	public int checkaddJSInterface(String destination) {
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
	

	public  void extractApplicationDetails(){
		
		LoadUrlDB.initDB();
		
		for(File individualApplication : this.fileContainer.getInputApplicationFiles()){
			int counter = 0;
			try{	
				this.appDetails.setAppName(individualApplication.toString());
				this.logger.initLogging(this.appDetails.getAppName(), this.logDirectory);
				this.logger.getLogger().info("Application name is " + this.appDetails.getAppName());
				this.appDetails.setAppPath(individualApplication.getAbsolutePath());
				this.apkToolHandler.dissembeApk(this.appDetails.getAppName(), this.logger.getLogger(), this.appDetails.getAppPath());
				this.appDetails.setSmaliPath(this.appDetails.getAppName());
				this.manifestParser.parseManifest(GenericConstants.APKTOOL_OUTPUT_DIRECTORY+this.appDetails.getAppName(), this.appDetails, this.logger.getLogger());

			    if(this.checkaddJSInterface(GenericConstants.APKTOOL_OUTPUT_DIRECTORY+this.appDetails.getAppName()) == 0) {
					System.out.println("No addJavascriptInterface in " + this.appDetails.getAppName());
					System.out.println("Moving on to the next app");
					this.reInitialize();
					continue;
				}
			    
				//remove duplicate paths
				this.removeDuplicate(this.appDetails.getActivityPath());
				//Store each class file content (in smali format) to the SmaliContent classContent
				String directoryA = GenericConstants.APKTOOL_OUTPUT_DIRECTORY + this.getAppDetails().getSmaliPath();
				directoryA = directoryA.replace("smali/", "");
				System.out.println("directory is " + directoryA);
				CollectClasses.listAllSmali(directoryA, this.smaliData, this.logger.getLogger());
				
				
		/*		for(String directory : this.appDetails.getActivityPath()){
					directory = GenericConstants.APKTOOL_OUTPUT_DIRECTORY + this.getAppDetails().getSmaliPath();
					directory = directory.replace("smali/", ""); 

					CollectClasses.listAllSmali(directory, this.smaliData, this.logger.getLogger());

				}			*/

				
				/* Stop the analysis if the app does not contain addJavascriptInterface api call
				 * i.e., there is no hybrid data transfer
				*/
			//temp abort	
			//	System.exit(0);
				
				for (List<String> fileContentInSmaliFormat : this.smaliData.classContent){
					String[] fileContentInArray =  fileContentInSmaliFormat.toArray(new String[fileContentInSmaliFormat.size()]);
					for (int index =0; index< fileContentInArray.length ; index++){
					if(fileContentInArray[index].contains(GenericConstants.ADDJSInterface)){
						counter++;
					}
					}				
				}
				if(counter == 0) {
					System.out.println("No addJavascriptInterface in " + this.appDetails.getAppName());
					System.out.println("Moving on to the next app");
					this.reInitialize();
					continue;
				}
				

				//Backward slicing on loadURL, find any potential leaks and store it in the DB
				// Requirement number 3

			//	System.out.println("Came Here Abhis");
				//Commented temporarly abhishek
				try {
				new Slicer( this.smaliData.classContent, this.logger.getLogger(), this.appDetails);
				}
				catch(Exception e) {
					this.reInitialize();
					e.printStackTrace(System.out);
				}
			//	System.exit(0);
				
				//System.out.println("came out of loop");
				//Find raw JS passed to loadurl, Store them in a DB
				//Requirement number 2
				LoadURLAnalyzer.checkLoadUrlType(this);

			// Checking for evaluateJavaScript
			//	EvaluateJavaScriptHandler.checkEvaluateJavaScript(this);
				
			//	JSDownloader.getJSDetails();
			//	JSDownloader.getAltJSDetails();
				//re-initialize 
				this.reInitialize();
				//app Analyzed successfully
				appCounter++;
				//this.appDetails.getActivityPath().clear();
				System.out.println("App " + appCounter + " Analyzed");
			} catch(Exception e){
				System.out.println("Here in extractApplicationDetails");
				System.out.println("Exception is " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
		JSDownloader.removeDuplicates();
		//copy remaining JS
		JSDownloader.getAltJSDetails();
	}
	
	public void removeDuplicate(List<String> activity_path){
		
		Set<String> temp = new HashSet<>();
		temp.addAll(activity_path);
		activity_path.clear();
		activity_path.addAll(temp);
	}
	
	public void reInitialize(){
		this.appDetails = new ApplicationDetails();
		this.apkToolHandler = new ApkToolHandler();
		this.smaliData = new SmaliContent();
		this.manifestParser = new ManifestParser();
		this.logger = new IIFALogger();
	}

	/**
	 * @return the fileContainer
	 */
	public InputApkFileContainer getFileContainer() {
		return fileContainer;
	}

	/**
	 * @param fileContainer the fileContainer to set
	 */
	public void setFileContainer(InputApkFileContainer fileContainer) {
		this.fileContainer = fileContainer;
	}

	/**
	 * @return the appDetails
	 */
	public ApplicationDetails getAppDetails() {
		return appDetails;
	}

	/**
	 * @param appDetails the appDetails to set
	 */
	public void setAppDetails(ApplicationDetails appDetails) {
		this.appDetails = appDetails;
	}

	/**
	 * @return the apkToolHandler
	 */
	public ApkToolHandler getApkToolHandler() {
		return apkToolHandler;
	}

	/**
	 * @param apkToolHandler the apkToolHandler to set
	 */
	public void setApkToolHandler(ApkToolHandler apkToolHandler) {
		this.apkToolHandler = apkToolHandler;
	}

	/**
	 * @return the smaliData
	 */
	public SmaliContent getSmaliData() {
		return smaliData;
	}

	/**
	 * @param smaliData the smaliData to set
	 */
	public void setSmaliData(SmaliContent smaliData) {
		this.smaliData = smaliData;
	}

	/**
	 * @return the manifestParser
	 */
	public ManifestParser getManifestParser() {
		return manifestParser;
	}

	/**
	 * @param manifestParser the manifestParser to set
	 */
	public void setManifestParser(ManifestParser manifestParser) {
		this.manifestParser = manifestParser;
	}

	/**
	 * @return the logger
	 */
	public IIFALogger getLogger() {
		return logger;
	}

	/**
	 * @param logger the logger to set
	 */
	public void setLogger(IIFALogger logger) {
		this.logger = logger;
	}

	public void setLogDirectory(String logDirectory) {
        this.logDirectory = normalizeLogDirectory(logDirectory);
    }

	public String getLogDirectory() {
        return logDirectory;
    }

	private static String normalizeLogDirectory(String directory) {
        String candidate = (directory == null || directory.trim().isEmpty())
                ? GenericConstants.DEFAULT_LOG_DIRECTORY
                : directory.trim();
        candidate = candidate.replace("\\", File.separator).replace("/", File.separator);
        if (!candidate.endsWith(File.separator)) {
            candidate = candidate + File.separator;
        }
        return candidate;
    }
}
