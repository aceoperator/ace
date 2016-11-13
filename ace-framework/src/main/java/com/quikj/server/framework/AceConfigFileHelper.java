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
		String aceHome = System.getProperty("ace.root");		
		if (aceHome == null) {
			aceHome = System.getProperty("user.home");
			if (!new File(aceHome + "/.ace").isDirectory()) {
				File[] root = File.listRoots();
				if (root == null) {
					// Should not come here
					root = new File[1];
					root[0] = new File("/");
				}

				boolean found = false;
				for (File rootElement : root) {
					aceHome = rootElement.getAbsolutePath() + "/usr/share/aceoperator";
					if (new File(aceHome + "/.ace").isDirectory()) {
						found = true;
						break;
					}
				}
				
				if (!found) {
					throw new AceRuntimeException("The root directory could not be determined");
				}
			}
		} else if (!new File(aceHome + "/.ace").isDirectory()) {
			throw new AceRuntimeException("The ace.root property is not right");
		}
		
		return aceHome;
	}
}
