package com.satoshiinoue.anydownloader.utilities;

public class ContentDispositionParser {
	public static String getValue(final String contentDisposition, final String key) {
		
		if (!contentDisposition.contains(key)) {
			return null;
		} else {
			String ret;
			String temp[] = contentDisposition.split(key);
			//temp[0]: attachment;...filename
			//temp[1]: ="......" .....
			
			if (temp.length <2)
				return null;
			
			//remove '='
			ret = temp[1].substring(1);
			
			//remove any following strings after "filename.ext" 
			String temp2[] = ret.split("\\s");
			ret = temp2[0].replaceAll("^\"|\"$", "");
			return ret;
		}
		
	}
}
