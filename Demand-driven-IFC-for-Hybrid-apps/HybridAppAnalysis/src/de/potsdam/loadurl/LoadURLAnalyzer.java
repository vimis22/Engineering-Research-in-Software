/**
 * 
 */
package de.potsdam.loadurl;

import java.util.List;
import java.util.StringTokenizer;

import de.potsdam.db.LoadUrlDB;
import de.potsdam.main.ApplicationAnalysis;
import de.potsdam.SmaliContent.SmaliContent;
import de.potsdam.constants.GenericConstants;

/**
 * @author abhishektiwari
 *
 */
public class LoadURLAnalyzer {
	
	public static int fileCounter = 0;
	
	public static void checkLoadUrlType( ApplicationAnalysis appAnalyzer){
		
		SmaliContent smaliData = appAnalyzer.getSmaliData();
		String className = null;
		int index;
		boolean addJsflag = false;
		try{
		for (List<String> fileContentInSmaliFormat : smaliData.classContent){
			fileCounter++;
			index =0;
			String[] fileContentInArray =  fileContentInSmaliFormat.toArray(new String[fileContentInSmaliFormat.size()]);
			addJsflag = false;
			//className = classNameExtractor(fileContentInArray[0]);
			for(String line: fileContentInArray){
				if(line.contains(GenericConstants.ADDJSInterface)){
					addJsflag = true;
				}
			}
			
			if(!addJsflag) {
				continue;
			}
			
			
			className = classNameExtractorAlt(fileContentInArray[0]);
			for(String line: fileContentInArray){
				if(line.contains(GenericConstants.LOAD_URL)){
				//	System.out.println("Line is " + line + " Class is " + className);
					String rawJsString =javaScriptTagChecker(fileContentInArray, index);
					LoadUrlDB.storeIntentDetails(appAnalyzer, className, rawJsString);
				//	System.out.println("Raw String is " + rawJsString);
				}
				if(line.contains("loadUrl")){
				//	System.out.println("Below Line is " + line + " Class is " + className);
				}
				index++;
			}
		}
		System.out.println("Total number of files " + fileCounter);
		}catch(Exception e){
			System.out.println("here in checkLoadUrlType");
			System.out.println(e.getStackTrace());
		}
	}

	public static String classNameExtractor(String classHeader){
		String name = "dummyClass";
		StringTokenizer st = new StringTokenizer(classHeader, "/;");
		while(st.hasMoreTokens()){
			name = st.nextToken();
		}
		return name;
	}
	
	public static String classNameExtractorAlt(String classHeader){
		String name = "dummyClass";
		StringTokenizer st = new StringTokenizer(classHeader, " ");
		while(st.hasMoreTokens()){
			name = st.nextToken();
		}
		return name;
	}
	
	
	public static String javaScriptTagChecker(String[] fileContentInArray, int index){
		//System.out.println("index is " + index);
		
		String rawJsString = "";
		
		//System.out.println("LoadURL is" + fileContentInArray[index]);
		String[] inputStringRegister = new String[4];
		int counter =0;
		StringTokenizer st = new StringTokenizer(fileContentInArray[index], "{,}");
		while(st.hasMoreTokens()){
			inputStringRegister[counter] = st.nextToken();
			counter++;
		}
	//	System.out.println("Register is "+ inputStringRegister[2]);
		while(!fileContentInArray[index].contains(".method")){
			index--;
		//	System.out.println("backward slice is " + fileContentInArray[index]);
			//Handle javascript: and normal const-strings
			if(fileContentInArray[index].contains(inputStringRegister[2]) && fileContentInArray[index].contains("const-string")){
				if(fileContentInArray[index].contains("javascript:")){
					rawJsString += fileContentInArray[index];
					System.out.println("rawJsString is " + rawJsString);
					break;
				}
				else{
					rawJsString += fileContentInArray[index];
					System.out.println("rawJsString down is " + rawJsString);
					break;
				}
			}
			//Handle iget-object, Need to provide a better implementation
			else if(fileContentInArray[index].contains(inputStringRegister[2]) && fileContentInArray[index].contains("iget-object")){
				rawJsString += fileContentInArray[index];
				System.out.println("rawJsString iget-object is " + rawJsString);
				break;
			}
			//Handle move-result-object
			else if(fileContentInArray[index].contains(inputStringRegister[2]) && fileContentInArray[index].contains("move-result-object")){			
				if(fileContentInArray[index-2].contains(GenericConstants.STRINGBUILDER_TOSTRING)){
					System.out.println("Handling move-result-object");
					rawJsString += StringOptimizer.string_builder(fileContentInArray, index-2);
				}
				else{
					rawJsString += "move-result-object need to handle it";
				//	System.out.println("move-result-object need to handle it");
				}
				break;
			}
			//Handle sget-object
			else if(fileContentInArray[index].contains(inputStringRegister[2]) && fileContentInArray[index].contains("sget-object")){
			//	System.out.println("sget-object need to handle it");
				rawJsString += "sget-object need to handle it";
				break;
			}
			else if(inputStringRegister[2].contains("p")){
			//	System.out.println("We do not handle inter-procedural as of now");
				rawJsString += "We do not handle inter-procedural as of now";
				break;
			}
		}
		return rawJsString;
		
	}
}
