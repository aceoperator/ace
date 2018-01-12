/**
 * 
 */
package com.quikj.ace.web.client.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.presenter.MessageBoxPresenter;

/**
 * @author amit
 *
 *         Meant to be a base class for rendering to various devices
 *         (desktop/mobile/tablet), hence the protected methods. At this point,
 *         it is not needed to extend it
 */
public class FormRenderer {
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

	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String MINLEN = "minlength";
	private static final String MAXLEN = "maxlength";
	private static final String REQUIRED = "required";

	public interface FormListener {
		boolean formSubmitted(long formId, Map<String, String> result);
	}

	public class FormHandler implements ClickHandler {
		private Map<Widget, Map<String, String>> attributes = new HashMap<Widget, Map<String, String>>();
		private long formId;
		private FormListener listener;
		private Button submitButton;
		private FocusWidget firstWidget;

		public FormHandler(long formId, FormListener listener) {
			this.formId = formId;
			this.listener = listener;
		}

		public void addAttribute(FocusWidget widget, String key, String value) {
			Map<String, String> widgetAttributes = attributes.get(widget);
			if (widgetAttributes == null) {
				widgetAttributes = new HashMap<String, String>();
				attributes.put(widget, widgetAttributes);
			}

			widgetAttributes.put(key, value);
			if (firstWidget == null) {
				firstWidget = widget;
			}
		}

		private void focus() {
			if (firstWidget != null) {
				firstWidget.setFocus(true);
			}
		}

		private void reset() {
			for (Widget widget : attributes.keySet()) {
				if (widget instanceof TextBoxBase) {
					((TextBoxBase) widget).setValue("");
				}
			}
		}

		private void submit() {
			Map<String, String> result = new HashMap<>();

			for (Entry<Widget, Map<String, String>> e : attributes.entrySet()) {
				Widget widget = e.getKey();
				Map<String, String> attributes = e.getValue();

				String value;
				if (widget instanceof TextBoxBase) {
					value = ((TextBoxBase) widget).getValue();
				} else if (widget instanceof ListBox) {
					value = ((ListBox) widget).getSelectedValue();
				} else {
					// Should not happen
					value = "";
				}

				value = value.trim();

				if (!validate(attributes, value)) {
					return;
				}

				result.put(attributes.get(NAME), value);
			}

			if (listener.formSubmitted(formId, result)) {
				submitButton.setEnabled(false);
			}
		}

		@Override
		public void onClick(ClickEvent event) {
			if (event.getSource() == submitButton) {
				submit();
			} else {
				// Reset button
				reset();
			}
		}

		private boolean validate(Map<String, String> attributes, String value) {
			// Validate for required
			String v = attributes.get(REQUIRED);
			if (v != null && v.equalsIgnoreCase("true") && value.isEmpty()) {
				MessageBoxPresenter.getInstance().show(ApplicationController.getMessages().FormRenderer_formError(),
						ApplicationController.getMessages().FormRenderer_required(attributes.get(NAME)),
						MessageBoxPresenter.Severity.SEVERE, true);
				return false;
			}

			if (!value.isEmpty()) {
				// Validate for minimum length
				v = attributes.get(MINLEN);
				if (v != null && value.length() < Integer.parseInt(v)) {
					MessageBoxPresenter.getInstance().show(ApplicationController.getMessages().FormRenderer_formError(),
							ApplicationController.getMessages().FormRenderer_minLength(attributes.get(NAME), v),
							MessageBoxPresenter.Severity.SEVERE, true);
					return false;
				}

				// Validate for maximum length
				v = attributes.get(MAXLEN);
				if (v != null && value.length() > Integer.parseInt(v)) {
					MessageBoxPresenter.getInstance().show(ApplicationController.getMessages().FormRenderer_formError(),
							ApplicationController.getMessages().FormRenderer_maxLength(attributes.get(NAME), v),
							MessageBoxPresenter.Severity.SEVERE, true);
					return false;
				}

				// Validate for type
				v = attributes.get(TYPE);
				if (v != null) {
					if (v.equalsIgnoreCase("numeric")) {
						try {
							Integer.parseInt(value);
						} catch (NumberFormatException ex) {
							MessageBoxPresenter.getInstance().show(
									ApplicationController.getMessages().FormRenderer_formError(),
									ApplicationController.getMessages().FormRenderer_numeric(attributes.get(NAME)),
									MessageBoxPresenter.Severity.SEVERE, true);
							return false;
						}
					} else if (v.equalsIgnoreCase("currency")) {
						int decimalIndex = value.indexOf(ClientProperties.getInstance()
								.getStringValue(ClientProperties.CURRENCY_DELIMITER, "."));
						String dollars = "0";
						String cents = "00";
						if (decimalIndex <= 0) {
							cents = value;
						} else if (decimalIndex > 0) {
							dollars = value.substring(0, decimalIndex);
							cents = value.substring(decimalIndex + 1);
						}

						if (cents.length() > 2) {
							MessageBoxPresenter.getInstance()
									.show(ApplicationController.getMessages().FormRenderer_formError(),
											ApplicationController.getMessages().FormRenderer_currency(
													attributes.get(NAME), "$$",
													ClientProperties.getInstance().getStringValue(
															ClientProperties.CURRENCY_DELIMITER, "."),
													"cc"),
											MessageBoxPresenter.Severity.SEVERE, true);
							return false;
						}

						try {
							Integer.parseInt(dollars);
							Integer.parseInt(cents);
						} catch (NumberFormatException ex) {
							MessageBoxPresenter.getInstance()
									.show(ApplicationController.getMessages().FormRenderer_formError(),
											ApplicationController.getMessages().FormRenderer_currency(
													attributes.get(NAME), "$$",
													ClientProperties.getInstance().getStringValue(
															ClientProperties.CURRENCY_DELIMITER, "."),
													"cc"),
											MessageBoxPresenter.Severity.SEVERE, true);
							return false;
						}
					} else if (v.equalsIgnoreCase("date")) {
						String pattern = ClientProperties.getInstance().getStringValue(ClientProperties.DATE_FORMAT,
								ClientProperties.DEFAULT_DATE_FORMAT);
						try {
							DateTimeFormat.getFormat(pattern).parseStrict(value);
						} catch (IllegalArgumentException ex) {
							MessageBoxPresenter.getInstance().show(
									ApplicationController.getMessages().FormRenderer_formError(),
									ApplicationController.getMessages().FormRenderer_date(attributes.get(NAME),
											ClientProperties.getInstance().getStringValue(ClientProperties.DATE_FORMAT,
													ClientProperties.DEFAULT_DATE_FORMAT)),
									MessageBoxPresenter.Severity.SEVERE, true);
							return false;
						}
					} else if (v.equalsIgnoreCase("email")) {
						if (!value.matches("^[a-zA-Z0-9_\\-\\.]*@[a-zA-Z0-9_\\-\\.]*\\.[a-zA-Z]{2,3}$")) {
							MessageBoxPresenter.getInstance().show(
									ApplicationController.getMessages().FormRenderer_formError(),
									ApplicationController.getMessages().FormRenderer_email(attributes.get(NAME)),
									MessageBoxPresenter.Severity.SEVERE, true);
							return false;
						}
					}
				} // else value is of type text, nothing to do
			}
			return true;
		}

		public void setSubmitButton(Button submitButton) {
			this.submitButton = submitButton;
		}
	}

	public Widget renderForm(long formId, String formDef, FormListener listener) {
		try {
			Panel panel = renderPanel();

			FormHandler submitHandler = new FormHandler(formId, listener);

			String[] lines = formDef.split("\\r?\\n");
			for (String line : lines) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}

				String[] columns = line.split("\\|");
				if (columns[IDX_WIDGET].equalsIgnoreCase("label")) {
					renderLabel(columns, panel);
				} else if (columns[IDX_WIDGET].equalsIgnoreCase("text")) {
					renderTextField(columns, panel, false, submitHandler);
				} else if (columns[IDX_WIDGET].equalsIgnoreCase("password")) {
					renderTextField(columns, panel, true, submitHandler);
				} else if (columns[IDX_WIDGET].equalsIgnoreCase("textarea")) {
					renderTextArea(columns, panel, true, submitHandler);
				} else if (columns[IDX_WIDGET].equalsIgnoreCase("dropdown")) {
					renderDropdown(columns, panel, submitHandler);
				} else if (columns[IDX_WIDGET].equalsIgnoreCase("buttons")) {
					renderButtons(columns, panel, submitHandler);
					// This is the last line. Will ignore any more lines
					break;
				}
			}

			submitHandler.focus();
			return panel;
		} catch (Exception e) {
			// Any error processing this form is a result of bad form definition
			// (and bug - should not happen :-)). Just render the text
			return new HTML(new SafeHtmlBuilder().appendEscaped(formDef).toSafeHtml());
		}
	}

	protected void renderButtons(String[] columns, Panel panel, FormHandler submitter) {
		HorizontalPanel buttonPanel = new HorizontalPanel();
		panel.add(buttonPanel);

		Button submit = new Button(getColumn(columns, IDX_SUBMIT_LABEL, "Submit"));
		submitter.setSubmitButton(submit);
		submit.addClickHandler(submitter);
		buttonPanel.add(submit);

		if (columns.length > IDX_RESET_LABEL) {
			Button reset = new Button(getColumn(columns, IDX_RESET_LABEL, "Reset"));
			buttonPanel.add(reset);

			reset.addClickHandler(submitter);
		}
	}

	protected Panel renderPanel() {
		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(5);
		panel.setWidth("90%");
		return panel;
	}

	protected void renderLabel(String[] columns, Panel panel) {
		String label = getColumn(columns, IDX_LABEL_LABEL, "");
		panel.add(new HTML(label));
	}

	private void drawFieldLabel(String[] columns, int index, Panel panel) {
		String label = getColumn(columns, index, "");

		if (!label.isEmpty()) {
			panel.add(new HTML(label));
		}
	}

	private String getColumn(String[] columns, int index, String defaultValue) {
		if (columns.length > index) {
			String value = columns[index].trim();
			if (!value.isEmpty()) {
				return value;
			}
		}

		return defaultValue;
	}

	protected void renderTextField(String[] columns, Panel panel, boolean password, FormHandler submitter) {
		drawFieldLabel(columns, IDX_TEXTBOX_LABEL, panel);

		TextBox textBox;

		if (password) {
			textBox = new PasswordTextBox();
		} else {
			textBox = new TextBox();
		}
		panel.add(textBox);

		submitter.addAttribute(textBox, NAME, getColumn(columns, IDX_TEXTBOX_NAME, "text"));

		String column = getColumn(columns, IDX_TEXTBOX_REQUIRED, null);
		if (column != null) {
			submitter.addAttribute(textBox, REQUIRED, column);
		}

		submitter.addAttribute(textBox, TYPE, getColumn(columns, IDX_TEXTBOX_TYPE, "text"));

		column = getColumn(columns, IDX_TEXTBOX_MINLEN, null);
		if (column != null) {
			submitter.addAttribute(textBox, MINLEN, column);
		}
		
		column = getColumn(columns, IDX_TEXTBOX_MAXLEN, null);
		if (column != null) {
			textBox.setVisibleLength(Integer.parseInt(column));
			submitter.addAttribute(textBox, MAXLEN, column);
		} else {
			textBox.setWidth("100%");
		}
	}

	protected void renderTextArea(String[] columns, Panel panel, boolean password, FormHandler submitter) {
		drawFieldLabel(columns, IDX_TEXTAREA_LABEL, panel);

		TextArea textArea = new TextArea();
		panel.add(textArea);

		submitter.addAttribute(textArea, NAME, getColumn(columns, IDX_TEXTAREA_NAME, "textarea"));

		String column = getColumn(columns, IDX_TEXTAREA_REQUIRED, "false");
		if (column != null) {
			submitter.addAttribute(textArea, REQUIRED, column);
		}

		column = getColumn(columns, IDX_TEXTAREA_MINLEN, null);
		if (column != null) {
			submitter.addAttribute(textArea, MINLEN, column);
		}

		column = getColumn(columns, IDX_TEXTAREA_ROWS, null);
		if (column != null) {
			textArea.setVisibleLines(Integer.parseInt(column));
		}

		textArea.setWidth("100%");

		column = getColumn(columns, IDX_TEXTAREA_MAXLEN, null);
		if (column != null) {
			submitter.addAttribute(textArea, MAXLEN, column);
		}
	}

	protected void renderDropdown(String[] columns, Panel panel, FormHandler submitter) {
		drawFieldLabel(columns, IDX_DROPDOWN_LABEL, panel);

		ListBox listBox = new ListBox();
		panel.add(listBox);

		for (int i = IDX_DROPDOWN_ITEMS; i < columns.length; i++) {
			String item = columns[i].trim();
			if (!item.isEmpty()) {
				listBox.addItem(item);
			}
		}

		submitter.addAttribute(listBox, NAME, getColumn(columns, IDX_DROPDOWN_NAME, "dropdown"));
	}
}
