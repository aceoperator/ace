package com.quikj.server.framework;

import java.io.File;

public class AceConfigFileHelper {

	public static String getAcePath(String dir) {
		return getAcePath(dir, "");
	}

	public static String getAcePath(String dir, String file) {
		String aceHome = getAceRoot();	

		String path = null;
		if (file.isEmpty()) {
			path = aceHome +  File.separator + ".ace" + File.separator + dir;			
		} else {
			path = aceHome + File.separator + ".ace" + File.separator + dir + File.separator + file;
		}
		return path;
	}

	public static String getAceRoot() {
		String aceHome = System.getProperty("ace.root.dir");		
		if (aceHome == null || !new File(aceHome + "/.ace").isDirectory()) {
			throw new AceRuntimeException("The ace.root.dir property is not pointing to the right location");
		}
		
		return aceHome;
	}
}
