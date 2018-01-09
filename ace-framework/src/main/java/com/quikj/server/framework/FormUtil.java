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
	private static final int IDX_FORM_ACTION = 0;
	private static final int IDX_TO_TRANSCRIPT = 1;
	private static final int IDX_FORM_EMAIL_TO = 2;
	private static final int IDX_FORM_EMAIL_SUBJECT = 3;
	private static final int IDX_FORM_EMAIL_CC = 4;
	
	private static final int IDX_WIDGET = 0;
	private static final int IDX_DROPDOWN_LABEL = 1;
	private static final int IDX_DROPDOWN_NAME = 2;
	private static final int IDX_DROPDOWN_ITEMS = 3;
	
	private static final int IDX_TEXTAREA_LABEL = 1;
	private static final int IDX_TEXTAREA_NAME = 2;
	private static final int IDX_TEXTAREA_REQUIRED = 3;
	private static final int IDX_TEXTAREA_MINLEN = 4;
	private static final int IDX_TEXTAREA_MAXLEN = 5;
	private static final int IDX_TEXTAREA_ROWS = 6;	
	
	private static final int IDX_TEXTBOX_LABEL = 1;
	private static final int IDX_TEXTBOX_NAME = 2;
	private static final int IDX_TEXTBOX_REQUIRED = 3;
	private static final int IDX_TEXTBOX_TYPE = 4;
	private static final int IDX_TEXTBOX_MINLEN = 5;
	private static final int IDX_TEXTBOX_MAXLEN = 6;
	
	private static final int IDX_LABEL_LABEL = 1;
	
	private static final int IDX_SUBMIT_LABEL = 1;
	private static final int IDX_RESET_LABEL = 2;

	
	public static String validateForm(String formDef) {
		String[] tokens = tokenizeFormDef(formDef);
		if (tokens == null) {
			return null;
		}
		
		String result = validateFormSegment(tokens[0]);
		if (!result.isEmpty()) {
			return result;
		}
		
		String[] lines = tokens[1].split("\\r?\\n");
		for (String line: lines) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			
			String[] columns = line.split("\\|");
			if (columns[IDX_WIDGET].equalsIgnoreCase("label")) {
				result = validateLabel(columns);
			} else if (columns[IDX_WIDGET].equalsIgnoreCase("text")) {
				result = validateText(columns);
			} else if (columns[IDX_WIDGET].equalsIgnoreCase("password")) {
				result = validateText(columns);
			} else if (columns[IDX_WIDGET].equalsIgnoreCase("textarea")) {
				result = validateTextArea(columns);
			} else if (columns[IDX_WIDGET].equalsIgnoreCase("dropdown")) {
				result = validateDropDown(columns);
			} else if (columns[IDX_WIDGET].equalsIgnoreCase("buttons")) {
				result = validateButtons(columns);
			} else {
				result = "Unknown form element " + columns[IDX_WIDGET];
			}
			
			if (!result.isEmpty()) {
				return result;
			}
		}
		
		return "";
	}
	
	private static String validateButtons(String[] columns) {
		// TODO Auto-generated method stub
		return "";
	}

	private static String validateDropDown(String[] columns) {
		// TODO Auto-generated method stub
		return "";
	}

	private static String validateTextArea(String[] columns) {
		// TODO Auto-generated method stub
		return "";
	}

	private static String validateText(String[] columns) {
		// TODO Auto-generated method stub
		return "";
	}

	private static String validateLabel(String[] columns) {
		// TODO Auto-generated method stub
		return "";
	}

	private static String validateFormSegment(String segment) {
		// TODO
		return "";
	}

	public static String[] tokenizeFormDef(String formDef) {
		Matcher matcher = Pattern.compile("^##form\\|(.*)\\r?\\n((?s:.)*)", Pattern.CASE_INSENSITIVE).matcher(formDef);
		if (!matcher.matches()) {
			return null;
		}
	
		return new String[] { matcher.group(1), matcher.group(2) };
	}

	public static boolean processFormSubmission(String formDef, Map<String,String> result) {
		// #form|action(email at the moment)|to|subject|cc
		boolean toTranscript = true;
		String[] tokens = tokenizeFormDef(formDef);
		if (tokens != null) {
			String[] splits = tokens[0].split("\\|");
			String action = null;
			if (splits.length > IDX_FORM_ACTION) {
				action = splits[IDX_FORM_ACTION];
			}
			
			if (splits.length > IDX_TO_TRANSCRIPT) {
				String sendToTranscript = splits[IDX_TO_TRANSCRIPT];
				if (sendToTranscript.equalsIgnoreCase("false")) {
					toTranscript = false;
				}
			}			
			
			if (action.equals("email")) {
				String[] toList = splits[IDX_FORM_EMAIL_TO].split(",");
				
				String subject = "Form Response";
				String[] ccList = new String[0];
				
				if (splits.length > IDX_FORM_EMAIL_SUBJECT) {
					subject = splits[IDX_FORM_EMAIL_SUBJECT];
				}
				
				if (splits.length > IDX_FORM_EMAIL_CC) {
					ccList = splits[IDX_FORM_EMAIL_CC].split(",");
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
		
		return toTranscript;		
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