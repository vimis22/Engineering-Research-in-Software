/**
 * 
 */
package de.potsdam.loadurl;

import java.util.StringTokenizer;

import de.potsdam.constants.GenericConstants;

/**
 * @author abhishektiwari
 *
 */
public class StringOptimizer {
	
	
	//Create actual string from StringBuilder clas
	public static String string_builder(String[] array, int index){
		
		String ob = "";
		StringBuilder finalString = new StringBuilder("");
		
		if(array[index].contains(GenericConstants.STRINGBUILDER_TOSTRING)){
			StringTokenizer st = new StringTokenizer(array[index],"{,}");
			st.nextToken();
			ob = st.nextToken();
			
			while(!array[index].contains(".method")){
				index--;
				if(array[index].contains(ob) && array[index].contains(GenericConstants.STRINGBUILDER_APPEND)){
					st = new StringTokenizer(array[index],"{,}");
					st.nextToken();
					st.nextToken();
					String tempReg = "";
					tempReg = st.nextToken();
					if(tempReg.contains("p")){
						finalString = finalString.insert(0, "String coming from different method");
						continue;
					}
					int tempIndex = index;
					while(!array[tempIndex].contains(".method")){
						tempIndex--;
						if(array[tempIndex].contains(tempReg) && array[tempIndex].contains("const-string")){
							st = new StringTokenizer(array[tempIndex],"\"");
						//	System.out.println(st.nextToken());
							finalString = finalString.insert(0, st.nextToken());
						}
					}
					
				}
			}
			
			
			}
	//	System.out.println("Final String is " + finalString.toString());
		return finalString.toString();
		
	}
}
