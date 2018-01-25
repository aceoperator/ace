/**
 * 
 */
package com.quikj.ace.common.client.form;

/**
 * @author amit
 *
 */
public class FormConstants {

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
	public static final int IDX_FORM_ACTION = 0;
	public static final int IDX_FORM_ADD_TO_TRANSCRIPT = 1;
	public static final int IDX_FORM_EMAIL_TO = 2;
	public static final int IDX_FORM_EMAIL_SUBJECT = 3;
	public static final int IDX_FORM_EMAIL_CC = 4;
	public static final int IDX_WIDGET = 0;
	public static final int IDX_DROPDOWN_LABEL = 1;
	public static final int IDX_DROPDOWN_NAME = 2;
	public static final int IDX_DROPDOWN_ITEMS = 3;
	public static final int IDX_TEXTAREA_LABEL = 1;
	public static final int IDX_TEXTAREA_NAME = 2;
	public static final int IDX_TEXTAREA_REQUIRED = 3;
	public static final int IDX_TEXTAREA_MINLEN = 4;
	public static final int IDX_TEXTAREA_MAXLEN = 5;
	public static final int IDX_TEXTAREA_ROWS = 6;
	public static final int IDX_TEXTBOX_LABEL = 1;
	public static final int IDX_TEXTBOX_NAME = 2;
	public static final int IDX_TEXTBOX_REQUIRED = 3;
	public static final int IDX_TEXTBOX_TYPE = 4;
	public static final int IDX_TEXTBOX_MINLEN = 5;
	public static final int IDX_TEXTBOX_MAXLEN = 6;
	public static final int IDX_LABEL_LABEL = 1;
	public static final int IDX_SUBMIT_LABEL = 1;
	public static final int IDX_RESET_LABEL = 2;
	public static final String SEGMENT_DELIMITER = "\\r?\\n";
	public static final String COLUMN_DELIMITER = "\\|";
	public static final String EMAIL_SEPARATOR = ",";

}
