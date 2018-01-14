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
	private static final int IDX_FORM_ADD_TO_TRANSCRIPT = 1;
	private static final int IDX_FORM_EMAIL_TO = 2;
	private static final int IDX_FORM_EMAIL_SUBJECT = 3;
	private static final int IDX_FORM_EMAIL_CC = 4;

	private static final int IDX_WIDGET = 0;
	@SuppressWarnings("unused")
	private static final int IDX_DROPDOWN_LABEL = 1;
	private static final int IDX_DROPDOWN_NAME = 2;
	private static final int IDX_DROPDOWN_ITEMS = 3;

	@SuppressWarnings("unused")
	private static final int IDX_TEXTAREA_LABEL = 1;
	private static final int IDX_TEXTAREA_NAME = 2;
	private static final int IDX_TEXTAREA_REQUIRED = 3;
	private static final int IDX_TEXTAREA_MINLEN = 4;
	private static final int IDX_TEXTAREA_MAXLEN = 5;
	private static final int IDX_TEXTAREA_ROWS = 6;

	@SuppressWarnings("unused")
	private static final int IDX_TEXTBOX_LABEL = 1;
	private static final int IDX_TEXTBOX_NAME = 2;
	private static final int IDX_TEXTBOX_REQUIRED = 3;
	private static final int IDX_TEXTBOX_TYPE = 4;
	private static final int IDX_TEXTBOX_MINLEN = 5;
	private static final int IDX_TEXTBOX_MAXLEN = 6;

	private static final int IDX_LABEL_LABEL = 1;

	@SuppressWarnings("unused")
	private static final int IDX_SUBMIT_LABEL = 1;
	@SuppressWarnings("unused")
	private static final int IDX_RESET_LABEL = 2;

	private static final String SEGMENT_DELIMITER = "\\r?\\n";
	private static final String COLUMN_DELIMITER = "\\|";
	private static final String EMAIL_SEPARATOR = ",";
	private static final String EMAIL_PATTERN = "^[a-zA-Z0-9_\\-\\.]*@[a-zA-Z0-9_\\-\\.]*\\.[a-zA-Z]{2,3}$";

	public enum Action {
		EMAIL("email"), NONE("none");

		String value;

		Action(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}

		public static Action getByValue(String value) {
			for (Action action : values()) {
				if (action.value.equalsIgnoreCase(value)) {
					return action;
				}
			}

			throw new IllegalArgumentException("action not found");
		}

		public static String valuesToString() {
			StringBuilder builder = new StringBuilder();
			for (Action item : values()) {
				if (builder.length() > 0) {
					builder.append("/");
				}
				builder.append(item.value());
			}

			return builder.toString();
		}
	}

	public enum Widget {
		LABEL("label"), TEXT("text"), PASSWORD("password"), TEXTAREA("textarea"), DROPDOWN("dropdown"), BUTTONS(
				"buttons");

		String value;

		Widget(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}

		public static Widget getByValue(String value) {
			for (Widget widget : values()) {
				if (widget.value.equalsIgnoreCase(value)) {
					return widget;
				}
			}

			throw new IllegalArgumentException("widget not found");
		}

		public static String valuesToString() {
			StringBuilder builder = new StringBuilder();
			for (Widget item : values()) {
				if (builder.length() > 0) {
					builder.append("/");
				}
				builder.append(item.value());
			}

			return builder.toString();
		}
	}

	public enum TextType {
		TEXT("text"), NUMERIC("numeric"), CURRENCY("currency"), DATE("date"), EMAIL("email");

		String value;

		TextType(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}

		public static TextType getByValue(String value) {
			for (TextType type : values()) {
				if (type.value.equalsIgnoreCase(value)) {
					return type;
				}
			}

			throw new IllegalArgumentException("action not found");
		}

		public static String valuesToString() {
			StringBuilder builder = new StringBuilder();
			for (TextType item : values()) {
				if (builder.length() > 0) {
					builder.append("/");
				}
				builder.append(item.value());
			}

			return builder.toString();
		}
	}

	private static String formatErrorMessage(int lineNum, String message) {
		return "Line " + lineNum + ": " + message;
	}

	public static String validateForm(String formDef) {
		String[] tokens = tokenizeFormDef(formDef);
		if (tokens == null) {
			return null;
		}

		int lineNum = 1;
		String result = validateFormSegment(tokens[0].split(COLUMN_DELIMITER), lineNum);
		if (!result.isEmpty()) {
			return result;
		}

		String[] lines = tokens[1].split(SEGMENT_DELIMITER);
		for (String line : lines) {
			line = line.trim();
			lineNum++;
			if (line.isEmpty()) {
				continue;
			}

			String[] columns = line.split(COLUMN_DELIMITER);
			Widget widget = null;
			try {
				widget = Widget.getByValue(columns[IDX_WIDGET].trim());
			} catch (IllegalArgumentException e) {
				return formatErrorMessage(lineNum,
						"Unknown object " + columns[IDX_WIDGET] + ". The valid values are " + Widget.valuesToString());
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
				return formatErrorMessage(lineNum, "Unknown form element " + columns[IDX_WIDGET]);
			}

			if (!result.isEmpty()) {
				return result;
			}
		}

		return "";
	}

	private static String validateDropDownSegment(String[] columns, int lineNum) {
		if (columns[IDX_DROPDOWN_NAME].trim().isEmpty()) {
			return formatErrorMessage(lineNum, "The name field must not be empty");
		}
		
		int itemCount = 0;
		for (int i = IDX_DROPDOWN_ITEMS; i < columns.length; i++) {
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
		if (columns.length <= IDX_TEXTBOX_NAME || columns[IDX_TEXTBOX_NAME].trim().isEmpty()) {
			return formatErrorMessage(lineNum, "The name field must be specified");
		}

		if (columns.length > IDX_TEXTBOX_REQUIRED && !validBooleanValue(columns[IDX_TEXTBOX_REQUIRED])) {
			return formatErrorMessage(lineNum, "The required field must have a value of true or false");
		}

		if (columns.length > IDX_TEXTBOX_TYPE) {
			String type = columns[IDX_TEXTBOX_TYPE].trim();
			try {
				TextType.getByValue(type);
			} catch (IllegalArgumentException e) {
				return formatErrorMessage(lineNum,
						"The type field has invalid values. The valid values are " + TextType.valuesToString());
			}
		}

		if (columns.length > IDX_TEXTBOX_MINLEN) {
			String minLength = columns[IDX_TEXTBOX_MINLEN].trim();
			if (!minLength.isEmpty()) {
				try {
					Integer.parseInt(minLength);
				} catch (NumberFormatException e) {
					return formatErrorMessage(lineNum, "The minimum length field must be numeric");
				}
			}
		}

		if (columns.length > IDX_TEXTBOX_MAXLEN) {
			String maxLength = columns[IDX_TEXTBOX_MAXLEN].trim();
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
		if (columns.length <= IDX_TEXTAREA_NAME || columns[IDX_TEXTAREA_NAME].trim().isEmpty()) {
			return formatErrorMessage(lineNum, "The name field must be specified");
		}

		if (columns.length > IDX_TEXTAREA_REQUIRED && !validBooleanValue(columns[IDX_TEXTAREA_REQUIRED])) {
			return formatErrorMessage(lineNum, "The required field must have a value of true or false");
		}

		if (columns.length > IDX_TEXTAREA_MINLEN) {
			String minLength = columns[IDX_TEXTAREA_MINLEN].trim();
			if (!minLength.isEmpty()) {
				try {
					Integer.parseInt(minLength);
				} catch (NumberFormatException e) {
					return formatErrorMessage(lineNum, "The minimum length field must be numeric");
				}
			}
		}

		if (columns.length > IDX_TEXTAREA_MAXLEN) {
			String maxLength = columns[IDX_TEXTAREA_MAXLEN].trim();
			if (!maxLength.isEmpty()) {
				try {
					Integer.parseInt(maxLength);
				} catch (NumberFormatException e) {
					return formatErrorMessage(lineNum, "The maximum length field must be numeric");
				}
			}
		}

		if (columns.length > IDX_TEXTAREA_ROWS) {
			String rows = columns[IDX_TEXTAREA_ROWS].trim();
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
		if (columns.length <= IDX_LABEL_LABEL || columns[IDX_LABEL_LABEL].trim().isEmpty()) {
			return formatErrorMessage(lineNum, "A label field must be specified");
		}
		return "";
	}

	private static String validateFormSegment(String[] columns, int lineNum) {
		if (columns.length == 0) {
			return formatErrorMessage(lineNum, "The action field must not be blank");
		}

		Action action = null;
		try {
			action = Action.getByValue(columns[IDX_FORM_ACTION].trim());
		} catch (IllegalArgumentException e) {
			return formatErrorMessage(lineNum,
					"The action field is invalid. The possible values are " + Action.valuesToString());
		}

		if (columns.length > IDX_FORM_ADD_TO_TRANSCRIPT) {
			String add = columns[IDX_FORM_ADD_TO_TRANSCRIPT].trim();
			if (!add.isEmpty() && !validBooleanValue(add)) {
				return formatErrorMessage(lineNum, "The transcript field has invalid value - must be true or false");
			}
		}

		switch (action) {
		case EMAIL:
			if (columns.length <= IDX_FORM_EMAIL_TO || columns[IDX_FORM_EMAIL_TO].trim().isEmpty()) {
				return formatErrorMessage(lineNum, "The email to field must be specified");
			}

			if (!validEmailList(columns[IDX_FORM_EMAIL_TO].trim())) {
				return formatErrorMessage(lineNum,
						"The email to field must have valid email addresses seperated by comma");
			}

			if (columns.length > IDX_FORM_EMAIL_CC) {
				String cc = columns[IDX_FORM_EMAIL_CC].trim();
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
		String[] tokens = value.split(EMAIL_SEPARATOR);
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
			String[] splits = tokens[0].split(COLUMN_DELIMITER);
			String action = null;
			if (splits.length > IDX_FORM_ACTION) {
				action = splits[IDX_FORM_ACTION];
			}

			if (splits.length > IDX_FORM_ADD_TO_TRANSCRIPT) {
				String sendToTranscript = splits[IDX_FORM_ADD_TO_TRANSCRIPT];
				if (sendToTranscript.equalsIgnoreCase("false")) {
					toTranscript = false;
				}
			}

			Action actionEnum = Action.getByValue(action);
			switch (actionEnum) {
			case EMAIL:
				String[] toList = splits[IDX_FORM_EMAIL_TO].split(EMAIL_SEPARATOR);

				String subject = "Form Response";
				String[] ccList = new String[0];

				if (splits.length > IDX_FORM_EMAIL_SUBJECT) {
					subject = splits[IDX_FORM_EMAIL_SUBJECT];
				}

				if (splits.length > IDX_FORM_EMAIL_CC) {
					ccList = splits[IDX_FORM_EMAIL_CC].split(EMAIL_SEPARATOR);
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