/**
 * 
 */
package com.quikj.server.framework;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author amit
 *
 */
public class FormUtil {

	public static String[] tokenizeFormDef(String formDef) {
		Matcher matcher = Pattern.compile("^#form\\|(.*)\\r?\\n((?s:.)*)", Pattern.CASE_INSENSITIVE).matcher(formDef);
		if (!matcher.matches()) {
			return null;
		}
	
		return new String[] { matcher.group(1), matcher.group(2) };
	}

	public static void processFormSubmission(String formDef, Map<String,String> result) {
		// #form|action(email at the moment)|to|subject|cc
		String[] tokens = tokenizeFormDef(formDef);
		if (tokens != null) {
			String[] splits = tokens[0].split("\\|");
			String action = null;
			if (splits.length >= 1) {
				action = splits[0];
			}
			
			if (action.equals("email")) {
				String[] toList = splits[1].split(",");
				String subject = "Form Response";
				String[] ccList = new String[0];
				if (splits.length > 2) {
					subject = splits[2];
				}
				
				if (splits.length > 3) {
					ccList = splits[3].split(",");
				}
				
				AceMailMessage mail = new AceMailMessage();
				mail.setSubject(subject);
				for (String to: toList) {
					mail.addTo(to);
				}
				
				for(String cc: ccList) {
					mail.addCc(cc);
				}
				
				mail.setSubType("html");
				mail.setBody(formatBody(result));
				
				if (!AceMailService.getInstance().addToMailQueue(mail)) {
					AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
							+ "- TalkEndoint.processFormSubmission() -- Failed to add form submission to the mail queue");
				}
			} // else, quietly ignore it
		}
	}
	
	private static String formatBody (Map<String, String> result) {
		List<String> list = new ArrayList<>();

		for (Entry<String, String> e : result.entrySet()) {
			StringBuilder b = new StringBuilder("<li><b>");
			b.append(e.getKey());
			b.append("</b>: ");
			b.append(e.getValue());
			b.append("</li>");
			list.add(b.toString());
		}

		list.sort(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		});

		// TODO internationalize
		StringBuilder builder = new StringBuilder("<ul>");
		for (String e : list) {
			builder.append(e);
		}
		builder.append("</ul>");
		return builder.toString();
	}
}