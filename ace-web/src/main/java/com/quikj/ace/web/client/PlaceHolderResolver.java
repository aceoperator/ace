/**
 * 
 */
package com.quikj.ace.web.client;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author amit
 *
 */
public class PlaceHolderResolver {
	private static final String PREFIX_SUFFIX = "__";

	public static String replace(String input, Map<String, String> variables) {
		String ret = input;
		for (Entry<String, String> e : variables.entrySet()) {
			String key = PREFIX_SUFFIX + e.getKey() + PREFIX_SUFFIX;
			ret = ret.replaceAll(key, e.getValue());
		}

		return ret;
	}
}
