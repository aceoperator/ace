package com.quikj.server.framework;

import java.io.File;

public class AceConfigFileHelper {

	public static String getAcePath(String dir) {
		return getAcePath(dir, "");
	}

	public static String getAcePath(String dir, String file) {
		String aceRoot = System.getProperty("ace.root");
		String aceHome = null;
		if (aceRoot != null) {
			aceHome = aceRoot + File.separator + ".ace";
		} else {
			aceHome = System.getProperty("user.home") + File.separator + ".ace";
			if (!new File(aceHome).isDirectory()) {
				File[] root = File.listRoots();
				if (root == null) {
					// Should not come here
					root = new File[1];
					root[0] = new File("/");
				}

				for (File rootElement : root) {
					aceHome = rootElement.getAbsolutePath() + "/usr/share/aceoperator/.ace";
					if (new File(aceHome).isDirectory()) {
						break;
					}
				}
			}
		}

		if (!new File(aceHome).isDirectory()) {
			throw new AceRuntimeException("Aceoperator home could not be determined");
		}

		String path = null;
		if (file.length() > 0) {
			path = aceHome + File.separator + dir + File.separator + file;
		} else {
			path = aceHome + File.separator + dir;
		}
		return path;
	}
}
