/**
 * 
 */
package com.quikj.application.web.talk.plugin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author amit
 *
 */
public class PlaceHolderResolver {
	private static final String PATTERN = "__(.*?)__";

	private static Pattern compiled = Pattern.compile(PATTERN);

	public static String replace(String input, Map<String, String> variables) {
		Matcher m = compiled.matcher(input);
		StringBuffer buffer = new StringBuffer();
		while(m.find()) {
			String text = m.group();
			text = text.substring(2, text.length() - 2);
			String value = variables.get(text);
			if (value == null) {
				value = text;
			}
			try {
				m.appendReplacement(buffer, URLEncoder.encode(value, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// Should not happen
				m.appendReplacement(buffer, "UNKNOWN");
			}
		}
		m.appendTail(buffer);
		return buffer.toString();
	}
	
	public static boolean placeHoldersExist(String input) {
		return compiled.matcher(input).find();
	}
	
//	public static void main(String[] args) {
//		String input = "http://quik-j.com/message.html?cookie=__cookie__&name=__name__";
//		HashMap<String, String> variables = new HashMap<String, String>();
//		variables.put("cookie", "123");
//		variables.put("name", "Amit Chatterjee");
//		String replacement = PlaceHolderResolver.replace(input, variables);
//		System.out.println(replacement);
//		
//		input = "http://quik-j.com/message.html";
//		boolean exists = PlaceHolderResolver.placeHoldersExist(input);
//		System.out.println(exists);
//		input = "http://quik-j.com/message.html?cookie=__cookie__&name=__name__";
//		exists = PlaceHolderResolver.placeHoldersExist(input);
//		System.out.println(exists);
//	}
}
