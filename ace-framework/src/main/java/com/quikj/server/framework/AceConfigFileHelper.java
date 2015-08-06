package com.quikj.server.framework;

import java.io.File;

public class AceConfigFileHelper {

	public static String getAcePath(String dir)
			throws ArrayIndexOutOfBoundsException {
		return getAcePath(dir, "");
	}

	public static String getAcePath(String dir, String file)
			throws ArrayIndexOutOfBoundsException {
		String ace_home = System.getProperty("user.home") + File.separator
				+ ".ace";

		String path = null;
		if (file.length() > 0) {
			path = ace_home + File.separator + dir + File.separator + file;
		} else {
			path = ace_home + File.separator + dir;
		}
		return path;
	}
}
