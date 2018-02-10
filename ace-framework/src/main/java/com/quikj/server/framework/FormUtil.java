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

import com.quikj.ace.common.client.form.FormConstants;

/**
 * @author amit
 *
 */
public class FormUtil {
	public static final String EMAIL_PATTERN = "^[a-zA-Z0-9_\\-\\.]*@[a-zA-Z0-9_\\-\\.]*\\.[a-zA-Z]{2,3}$";

	private static String formatErrorMessage(int lineNum, String message) {
		return "Line " + lineNum + ": " + message;
	}

	public static String validateForm(String formDef) {
		String[] tokens = tokenizeFormDef(formDef);
		if (tokens == null) {
			return null;
		}

		int lineNum = 1;
		String result = validateFormSegment(tokens[0].split(FormConstants.COLUMN_DELIMITER), lineNum);
		if (!result.isEmpty()) {
			return result;
		}

		String[] lines = tokens[1].split(FormConstants.SEGMENT_DELIMITER);
		for (String line : lines) {
			line = line.trim();
			lineNum++;
			if (line.isEmpty()) {
				continue;
			}

			String[] columns = line.split(FormConstants.COLUMN_DELIMITER);
			FormConstants.Widget widget = null;
			try {
				widget = FormConstants.Widget.getByValue(columns[FormConstants.IDX_WIDGET].trim());
			} catch (IllegalArgumentException e) {
				return formatErrorMessage(lineNum,
						"Unknown object " + columns[FormConstants.IDX_WIDGET] + ". The valid values are " + FormConstants.Widget.valuesToString());
			}

			switch (widget) {
			case LABEL:
				result = validateLabelSegment(columns, lineNum);
				break;
			case TEXT:
				result = validateTextFieldSegment(columns, lineNum);
				break;
			case PASSWORD:
				result = validateTextFieldSegment(columns, lineNum);
				break;
			case TEXTAREA:
				result = validateTextAreaSegment(columns, lineNum);
				break;
			case DROPDOWN:
				result = validateDropDownSegment(columns, lineNum);
				break;
			case BUTTONS:
				break;
			default:
				// Should not happen, exception is going to be thrown before
				// this
				return formatErrorMessage(lineNum, "Unknown form element " + columns[FormConstants.IDX_WIDGET]);
			}

			if (!result.isEmpty()) {
				return result;
			}
		}

		return "";
	}

	private static String validateDropDownSegment(String[] columns, int lineNum) {
		if (columns[FormConstants.IDX_DROPDOWN_NAME].trim().isEmpty()) {
			return formatErrorMessage(lineNum, "The name field must not be empty");
		}
		
		int itemCount = 0;
		for (int i = FormConstants.IDX_DROPDOWN_ITEMS; i < columns.length; i++) {
			if (!columns[i].trim().isEmpty()) {
				itemCount++;
			}
		}
		
		if (itemCount == 0) {
			return formatErrorMessage(lineNum, "The dropdown must have at least one item");
		}
		return "";
	}

	private static String validateTextFieldSegment(String[] columns, int lineNum) {
		if (columns.length <= FormConstants.IDX_TEXTBOX_NAME || columns[FormConstants.IDX_TEXTBOX_NAME].trim().isEmpty()) {
			return formatErrorMessage(lineNum, "The name field must be specified");
		}

		if (columns.length > FormConstants.IDX_TEXTBOX_REQUIRED && !validBooleanValue(columns[FormConstants.IDX_TEXTBOX_REQUIRED])) {
			return formatErrorMessage(lineNum, "The required field must have a value of true or false");
		}

		if (columns.length > FormConstants.IDX_TEXTBOX_TYPE) {
			String type = columns[FormConstants.IDX_TEXTBOX_TYPE].trim();
			try {
				FormConstants.TextType.getByValue(type);
			} catch (IllegalArgumentException e) {
				return formatErrorMessage(lineNum,
						"The type field has invalid values. The valid values are " + FormConstants.TextType.valuesToString());
			}
		}

		if (columns.length > FormConstants.IDX_TEXTBOX_MINLEN) {
			String minLength = columns[FormConstants.IDX_TEXTBOX_MINLEN].trim();
			if (!minLength.isEmpty()) {
				try {
					Integer.parseInt(minLength);
				} catch (NumberFormatException e) {
					return formatErrorMessage(lineNum, "The minimum length field must be numeric");
				}
			}
		}

		if (columns.length > FormConstants.IDX_TEXTBOX_MAXLEN) {
			String maxLength = columns[FormConstants.IDX_TEXTBOX_MAXLEN].trim();
			if (!maxLength.isEmpty()) {
				try {
					Integer.parseInt(maxLength);
				} catch (NumberFormatException e) {
					return formatErrorMessage(lineNum, "The maximum length field must be numeric");
				}
			}
		}

		return "";
	}

	private static String validateTextAreaSegment(String[] columns, int lineNum) {
		if (columns.length <= FormConstants.IDX_TEXTAREA_NAME || columns[FormConstants.IDX_TEXTAREA_NAME].trim().isEmpty()) {
			return formatErrorMessage(lineNum, "The name field must be specified");
		}

		if (columns.length > FormConstants.IDX_TEXTAREA_REQUIRED && !validBooleanValue(columns[FormConstants.IDX_TEXTAREA_REQUIRED])) {
			return formatErrorMessage(lineNum, "The required field must have a value of true or false");
		}

		if (columns.length > FormConstants.IDX_TEXTAREA_MINLEN) {
			String minLength = columns[FormConstants.IDX_TEXTAREA_MINLEN].trim();
			if (!minLength.isEmpty()) {
				try {
					Integer.parseInt(minLength);
				} catch (NumberFormatException e) {
					return formatErrorMessage(lineNum, "The minimum length field must be numeric");
				}
			}
		}

		if (columns.length > FormConstants.IDX_TEXTAREA_MAXLEN) {
			String maxLength = columns[FormConstants.IDX_TEXTAREA_MAXLEN].trim();
			if (!maxLength.isEmpty()) {
				try {
					Integer.parseInt(maxLength);
				} catch (NumberFormatException e) {
					return formatErrorMessage(lineNum, "The maximum length field must be numeric");
				}
			}
		}

		if (columns.length > FormConstants.IDX_TEXTAREA_ROWS) {
			String rows = columns[FormConstants.IDX_TEXTAREA_ROWS].trim();
			if (!rows.isEmpty()) {
				try {
					Integer.parseInt(rows);
				} catch (NumberFormatException e) {
					return formatErrorMessage(lineNum, "The rows field must be numeric");
				}
			}
		}

		return "";
	}

	private static String validateLabelSegment(String[] columns, int lineNum) {
		if (columns.length <= FormConstants.IDX_LABEL_LABEL || columns[FormConstants.IDX_LABEL_LABEL].trim().isEmpty()) {
			return formatErrorMessage(lineNum, "A label field must be specified");
		}
		return "";
	}

	private static String validateFormSegment(String[] columns, int lineNum) {
		if (columns.length == 0) {
			return formatErrorMessage(lineNum, "The action field must not be blank");
		}

		FormConstants.Action action = null;
		try {
			action = FormConstants.Action.getByValue(columns[FormConstants.IDX_FORM_ACTION].trim());
		} catch (IllegalArgumentException e) {
			return formatErrorMessage(lineNum,
					"The action field is invalid. The possible values are " + FormConstants.Action.valuesToString());
		}

		if (columns.length > FormConstants.IDX_FORM_ADD_TO_TRANSCRIPT) {
			String add = columns[FormConstants.IDX_FORM_ADD_TO_TRANSCRIPT].trim();
			if (!add.isEmpty() && !validBooleanValue(add)) {
				return formatErrorMessage(lineNum, "The transcript field has invalid value - must be true or false");
			}
		}

		switch (action) {
		case EMAIL:
			if (columns.length <= FormConstants.IDX_FORM_EMAIL_TO || columns[FormConstants.IDX_FORM_EMAIL_TO].trim().isEmpty()) {
				return formatErrorMessage(lineNum, "The email to field must be specified");
			}

			if (!validEmailList(columns[FormConstants.IDX_FORM_EMAIL_TO].trim())) {
				return formatErrorMessage(lineNum,
						"The email to field must have valid email addresses seperated by comma");
			}

			if (columns.length > FormConstants.IDX_FORM_EMAIL_CC) {
				String cc = columns[FormConstants.IDX_FORM_EMAIL_CC].trim();
				if (!cc.isEmpty() && !validEmailList(cc)) {
					return formatErrorMessage(lineNum,
							"The email cc field must have valid email addresses seperated by comma");
				}
			}
			break;
		default:
			// FORM_ACTION_NONE at this time
			break;
		}

		return "";
	}

	private static boolean validEmailList(String value) {
		String[] tokens = value.split(FormConstants.EMAIL_SEPARATOR);
		for (String token : tokens) {
			if (!token.matches(EMAIL_PATTERN)) {
				return false;
			}
		}
		return true;
	}

	private static boolean validBooleanValue(String value) {
		return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
	}

	public static String[] tokenizeFormDef(String formDef) {
		Matcher matcher = Pattern.compile("^##form\\|(.*)\\r?\\n((?s:.)*)", Pattern.CASE_INSENSITIVE).matcher(formDef);
		if (!matcher.matches()) {
			return null;
		}

		return new String[] { matcher.group(1), matcher.group(2) };
	}

	public static boolean processFormSubmission(String formDef, Map<String, String> result) {
		// #form|action(email at the moment)|to|subject|cc
		boolean toTranscript = true;
		String[] tokens = tokenizeFormDef(formDef);
		if (tokens != null) {
			String[] splits = tokens[0].split(FormConstants.COLUMN_DELIMITER);
			String action = null;
			if (splits.length > FormConstants.IDX_FORM_ACTION) {
				action = splits[FormConstants.IDX_FORM_ACTION];
			}

			if (splits.length > FormConstants.IDX_FORM_ADD_TO_TRANSCRIPT) {
				String sendToTranscript = splits[FormConstants.IDX_FORM_ADD_TO_TRANSCRIPT];
				if (sendToTranscript.equalsIgnoreCase("false")) {
					toTranscript = false;
				}
			}

			FormConstants.Action actionEnum = FormConstants.Action.getByValue(action);
			switch (actionEnum) {
			case EMAIL:
				String[] toList = splits[FormConstants.IDX_FORM_EMAIL_TO].split(FormConstants.EMAIL_SEPARATOR);

				String subject = "Form Response";
				String[] ccList = new String[0];

				if (splits.length > FormConstants.IDX_FORM_EMAIL_SUBJECT) {
					subject = splits[FormConstants.IDX_FORM_EMAIL_SUBJECT];
				}

				if (splits.length > FormConstants.IDX_FORM_EMAIL_CC) {
					ccList = splits[FormConstants.IDX_FORM_EMAIL_CC].split(FormConstants.EMAIL_SEPARATOR);
				}

				AceMailMessage mail = new AceMailMessage();
				mail.setSubject(subject);
				for (String to : toList) {
					mail.addTo(to);
				}

				for (String cc : ccList) {
					mail.addCc(cc);
				}

				mail.setSubType("html");
				mail.setBody(formatBody(result));

				if (!AceMailService.getInstance().addToMailQueue(mail)) {
					AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
							+ "- TalkEndoint.processFormSubmission() -- Failed to add form submission to the mail queue");
				}
				break;

			default:
				// Ignore. More cases to come
				break;
			}
		}

		return toTranscript;
	}

	private static String formatBody(Map<String, String> result) {
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

		StringBuilder builder = new StringBuilder("<ul>");
		for (String e : list) {
			builder.append(e);
		}
		builder.append("</ul>");
		return builder.toString();
	}
}