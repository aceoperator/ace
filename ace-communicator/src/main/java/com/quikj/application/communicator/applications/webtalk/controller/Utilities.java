/*
 * Utilities.java
 *
 * Created on September 14, 2003, 1:12 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

/**
 * 
 * @author bhm
 */
public class Utilities {
	/* Common utility functions needed by multiple classes */

	private static String ESCAPE_EQUAL = "&eq;";
	private static String ESCAPE_ESCAPE = "&amp;";

	public static String deEscapeEqual(String input) {
		// Replace &eq with =
		// Replace &amp with &

		StringBuffer buf = new StringBuffer(input);

		int index = 0;
		while (index < buf.length()) {
			int replace_at = buf.toString().indexOf(ESCAPE_EQUAL, index);

			if (replace_at == -1) {
				break;
			}

			buf = buf.replace(replace_at, replace_at + ESCAPE_EQUAL.length(),
					"=");

			index = replace_at + 1;
		}

		index = 0;
		while (index < buf.length()) {
			int replace_at = buf.toString().indexOf(ESCAPE_ESCAPE, index);

			if (replace_at == -1) {
				break;
			}

			buf = buf.replace(replace_at, replace_at + ESCAPE_ESCAPE.length(),
					"&");

			index = replace_at + 1;
		}

		return buf.toString();
	}

	public static String escapeEqual(String input) {
		// Replace = with &eq
		// Replace & with &amp

		StringBuffer buf = new StringBuffer(input);

		int index = 0;
		while (index < buf.length()) {
			int replace_at = buf.toString().indexOf('&', index);

			if (replace_at == -1) {
				break;
			}

			buf = buf.replace(replace_at, replace_at + 1, ESCAPE_ESCAPE);

			index = replace_at + ESCAPE_ESCAPE.length();
		}

		index = 0;
		while (index < buf.length()) {
			int replace_at = buf.toString().indexOf('=', index);

			if (replace_at == -1) {
				break;
			}

			buf = buf.replace(replace_at, replace_at + 1, ESCAPE_EQUAL);

			index = replace_at + ESCAPE_EQUAL.length();
		}

		return buf.toString();
	}

}
