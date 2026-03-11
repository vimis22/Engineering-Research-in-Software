/**
 * 
 */
package de.potsdam.evaluateJavascript;

import java.util.List;
import java.util.StringTokenizer;

import de.potsdam.SmaliContent.SmaliContent;
import de.potsdam.constants.GenericConstants;
import de.potsdam.db.LoadUrlDB;
import de.potsdam.loadurl.LoadURLAnalyzer;
import de.potsdam.loadurl.StringOptimizer;
import de.potsdam.main.ApplicationAnalysis;

/**
 * @author abhishektiwari
 *
 */
public class EvaluateJavaScriptHandler {
	
	public static void checkEvaluateJavaScript(ApplicationAnalysis appAnalyzer){
		
		SmaliContent smaliData = appAnalyzer.getSmaliData();
		String className = null;
		int index;
		
		for (List<String> fileContentInSmaliFormat : smaliData.classContent){
			
			index =0;
			String[] fileContentInArray =  fileContentInSmaliFormat.toArray(new String[fileContentInSmaliFormat.size()]);
			
			className = LoadURLAnalyzer.classNameExtractor(fileContentInArray[0]);
			
			for(String line: fileContentInArray){		
				
			if(line.contains(GenericConstants.EVALUATE_JS)){
				//System.out.println(line);
				String rawJsString = findJavaSript(fileContentInArray, index);	
				LoadUrlDB.storeIntentDetails(appAnalyzer, className, rawJsString);
			}
			index++;
			}
		}
	}
	
	public static String findJavaSript(String[] fileContentInArray, int index){
		
		String[] inputTempReg = new String[5];
		int i =0;
		String rawJavaScript = "";
		
		StringTokenizer st = new StringTokenizer(fileContentInArray[index], "{,}");
		
		while(st.hasMoreTokens()){
			inputTempReg[i] = st.nextToken();
			i++;
		}
		
		int tempIndex = index;
		//Extract JavaScript
		while(!fileContentInArray[tempIndex].contains(".method")){
			tempIndex--;
			//Handle javascript: and normal const-strings
			if(fileContentInArray[tempIndex].contains(inputTempReg[2]) && fileContentInArray[tempIndex].contains("const-string")){
				//System.out.println(fileContentInArray[tempIndex]);
				rawJavaScript = fileContentInArray[tempIndex];
				st = new StringTokenizer(rawJavaScript,",");
				st.nextToken();
				rawJavaScript = "";
				while(st.hasMoreTokens()){
					rawJavaScript += st.nextToken();
				}
				rawJavaScript = rawJavaScript.trim();
				return rawJavaScript;
			}
			//Handle iget-object, Need to provide a better implementation
			else if(fileContentInArray[index].contains(inputTempReg[2]) && fileContentInArray[index].contains("iget-object")){
				rawJavaScript += fileContentInArray[index];
				//System.out.println("rawJsString iget-object is " + rawJavaScript);
				return rawJavaScript;
			}
			//Handle move-result-object
			else if(fileContentInArray[index].contains(inputTempReg[2]) && fileContentInArray[index].contains("move-result-object")){			
				if(fileContentInArray[index-2].contains(GenericConstants.STRINGBUILDER_TOSTRING)){
				//	System.out.println("Handling move-result-object");
					rawJavaScript += StringOptimizer.string_builder(fileContentInArray, index-2);
				}
				else{
					rawJavaScript += "move-result-object need to handle it";
					System.out.println("move-result-object need to handle it");
				}
				return rawJavaScript;
			}
			//Handle sget-object
			else if(fileContentInArray[index].contains(inputTempReg[2]) && fileContentInArray[index].contains("sget-object")){
				System.out.println("sget-object need to handle it");
				rawJavaScript += "sget-object need to handle it";
				return rawJavaScript;
			}
			//Handle inter-procedural
			else if(inputTempReg[2].contains("p")){
				System.out.println("We do not handle inter-procedural strings as of now");
				rawJavaScript += "We do not handle inter-procedural strings as of now";
				return rawJavaScript;
			}			
			
		}
		
		
		return rawJavaScript;
	}

}
