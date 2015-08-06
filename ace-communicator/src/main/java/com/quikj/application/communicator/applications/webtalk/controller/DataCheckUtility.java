/*
 * DataCheckUtility.java
 *
 * Created on September 28, 2003, 10:37 AM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

/**
 * 
 * @author amit
 */
public class DataCheckUtility {
	public static boolean followsTableIdRules(String name) {
		char[] namea = name.toCharArray();

		for (int i = 0; i < namea.length; i++) {
			if ((namea[i] == '\'') || (namea[i] == '\"') || (namea[i] == '&')
					|| (namea[i] == '<') || (namea[i] == '>')
					|| (namea[i] == '=') || (namea[i] == '\t')
					|| (namea[i] == ';')) {
				return false;
			}
		}

		return true;
	}
	
	public static boolean followsBlankSpaceRules(String name) {
		char[] namea = name.toCharArray();

		for (int i = 0; i < namea.length; i++) {
			if (Character.isSpaceChar(namea[i])) {
				return false;
			}
		}
		
		return true;
	}
}
