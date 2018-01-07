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

	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String MINLENGTH = "minlength";
	private static final String MAXLENGTH = "maxlength";
	private static final String REQUIRED = "required";

	public interface FormListener {
		boolean formSubmitted(long formId, Map<String, String> result);
	}

	public class SubmitHandler implements ClickHandler {
		private Map<Widget, Map<String, String>> attributes = new HashMap<Widget, Map<String, String>>();
		private long formId;
		private FormListener listener;
		private Button submitButton;
		private FocusWidget firstWidget;

		public SubmitHandler(long formId, FormListener listener) {
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
				v = attributes.get(MINLENGTH);
				if (v != null && value.length() < Integer.parseInt(v)) {
					MessageBoxPresenter.getInstance().show(ApplicationController.getMessages().FormRenderer_formError(),
							ApplicationController.getMessages().FormRenderer_minLength(attributes.get(NAME), v),
							MessageBoxPresenter.Severity.SEVERE, true);
					return false;
				}

				// Validate for maximum length
				v = attributes.get(MAXLENGTH);
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
						String dollars = value;
						String cents = "00";
						if (decimalIndex == 0) {
							dollars = "0";
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

	public Widget renderForm(String from, long timeStamp, long formId, String formDef, String me, boolean smallSpace,
			FormListener listener) {
		Panel panel = renderPanel();

		SubmitHandler submitHandler = new SubmitHandler(formId, listener);

		String[] lines = formDef.split("\\r?\\n");
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}

			String[] columns = line.split("\\|");
			if (columns[0].equalsIgnoreCase("label")) {
				renderLabel(columns, panel);
			} else if (columns[0].equalsIgnoreCase("text")) {
				renderTextField(columns, panel, false, submitHandler);
			} else if (columns[0].equalsIgnoreCase("password")) {
				renderTextField(columns, panel, true, submitHandler);
			} else if (columns[0].equalsIgnoreCase("textarea")) {
				renderTextArea(columns, panel, true, submitHandler);
			} else if (columns[0].equalsIgnoreCase("dropdown")) {
				renderDropdown(columns, panel, submitHandler);
			} else if (columns[0].equalsIgnoreCase("buttons")) {
				renderButtons(columns, panel, submitHandler);
				// This is the last line. Will ignore any more lines
				break;
			}
		}

		submitHandler.focus();
		return panel;
	}

	protected void renderButtons(String[] columns, Panel panel, SubmitHandler submitter) {
		// column 1 = submit button label
		// column 2 = reset button label
		HorizontalPanel buttonPanel = new HorizontalPanel();
		panel.add(buttonPanel);

		Button submit = new Button(getColumn(columns, 1, "Submit"));
		submitter.setSubmitButton(submit);
		submit.addClickHandler(submitter);
		buttonPanel.add(submit);

		if (columns.length >= 3) {
			Button reset = new Button(getColumn(columns, 2, "Reset"));
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
		// column 1 = label
		String label = getColumn(columns, 1, "");

		if (!label.isEmpty()) {
			panel.add(new HTML(label));
		}
	}

	private String getColumn(String[] columns, int index, String defaultValue) {
		String value = defaultValue;

		if (columns.length > index) {
			value = columns[index].trim();
		}

		return value;
	}

	protected void renderTextField(String[] columns, Panel panel, boolean password, SubmitHandler submitter) {
		// column 1 = label
		// column 2 = name
		// column 3 = required (true/false)
		// column 4 = type (text/numeric/date/currency)
		// column 5 = maximum length
		// column 6 = minimum length

		renderLabel(columns, panel);

		TextBox textBox;

		if (password) {
			textBox = new PasswordTextBox();
		} else {
			textBox = new TextBox();
		}
		panel.add(textBox);

		String size = getColumn(columns, 5, null);
		if (size != null) {
			textBox.setVisibleLength(Integer.parseInt(size));
		} else {
			textBox.setWidth("100%");
		}

		submitter.addAttribute(textBox, NAME, getColumn(columns, 2, "text"));
		String column = getColumn(columns, 3, null);
		if (column != null) {
			submitter.addAttribute(textBox, REQUIRED, column);
		}

		submitter.addAttribute(textBox, TYPE, getColumn(columns, 4, "text"));

		column = getColumn(columns, 5, null);
		if (column != null) {
			submitter.addAttribute(textBox, MAXLENGTH, column);
		}

		column = getColumn(columns, 6, null);
		if (column != null) {
			submitter.addAttribute(textBox, MINLENGTH, column);
		}
	}

	protected void renderTextArea(String[] columns, Panel panel, boolean password, SubmitHandler submitter) {
		// column 1 = label
		// column 2 = name
		// column 3 = required (true/false)
		// column 4 = minimum length
		// column 5 = rows
		// column 6 = maximum length

		renderLabel(columns, panel);

		TextArea textArea = new TextArea();
		panel.add(textArea);

		String rows = getColumn(columns, 5, null);
		if (rows != null) {
			textArea.setVisibleLines(Integer.parseInt(rows));
		}

		textArea.setWidth("100%");

		submitter.addAttribute(textArea, NAME, getColumn(columns, 2, "textarea"));

		String column = getColumn(columns, 3, "false");
		if (column != null) {
			submitter.addAttribute(textArea, REQUIRED, column);
		}
		column = getColumn(columns, 4, null);
		if (column != null) {
			submitter.addAttribute(textArea, MINLENGTH, column);
		}
		column = getColumn(columns, 6, null);
		if (column != null) {
			submitter.addAttribute(textArea, MAXLENGTH, column);
		}
	}

	protected void renderDropdown(String[] columns, Panel panel, SubmitHandler submitter) {
		// column 1 = label
		// column 2 = name
		// column 3,4,5.... drop down items
		renderLabel(columns, panel);

		ListBox listBox = new ListBox();
		panel.add(listBox);

		for (int i = 3; i < columns.length; i++) {
			String item = columns[i].trim();
			if (!item.isEmpty()) {
				listBox.addItem(item);
			}
		}

		submitter.addAttribute(listBox, NAME, getColumn(columns, 2, "dropdown"));
	}
}
