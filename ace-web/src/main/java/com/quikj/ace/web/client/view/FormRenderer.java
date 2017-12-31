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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.ResetButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.presenter.MessageBoxPresenter;


//TODO add internationalization

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
	private static final String REQUIRED = "required";

	public interface FormListener {
		boolean formSubmitted(String formId, Map<String, String> result);
	}

	public class SubmitHandler implements ClickHandler {
		Map<Widget, Map<String, String>> validators = new HashMap<Widget, Map<String, String>>();
		private String formId;
		private FormListener listener;

		public SubmitHandler(String formId, FormListener listener) {
			this.formId = formId;
			this.listener = listener;
		}

		public void addAttribute(Widget widget, String key, String value) {
			Map<String, String> widgetValidators = validators.get(widget);
			if (widgetValidators == null) {
				widgetValidators = new HashMap<String, String>();
				validators.put(widget, widgetValidators);
			}

			widgetValidators.put(key, value);
		}

		@Override
		public void onClick(ClickEvent event) {
			Map<String, String> result = new HashMap<>();

			for (Entry<Widget, Map<String, String>> e : validators.entrySet()) {
				Widget widget = e.getKey();
				Map<String, String> attributes = e.getValue();

				String value;
				if (widget instanceof TextBox) {
					value = ((TextArea) widget).getValue();
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
				((Button) event.getSource()).setEnabled(false);
			}
		}

		private boolean validate(Map<String, String> attributes, String value) {
			// Validate for required
			String v = attributes.get(REQUIRED);
			if (v != null && v.equalsIgnoreCase("true") && value.isEmpty()) {
				MessageBoxPresenter.getInstance().show("Form Error!", attributes.get(NAME) + " is required",
						MessageBoxPresenter.Severity.SEVERE, true);
				return false;
			}

			// Validate for minimum length
			v = attributes.get(MINLENGTH);
			int intv = Integer.parseInt(v);
			if (v != null && value.length() < intv) {
				MessageBoxPresenter.getInstance().show("Form Error!",
						attributes.get(NAME) + " must have at least " + v + " characters",
						MessageBoxPresenter.Severity.SEVERE, true);
				return false;
			}

			// Validate for type
			v = attributes.get(TYPE);
			if (v != null) {
				// (text/numeric/date[format]/currency)
				if (v.equalsIgnoreCase("numeric")) {
					try {
						Integer.parseInt(value);
					} catch (NumberFormatException ex) {
						MessageBoxPresenter.getInstance().show("Form Error!", attributes.get(NAME) + " must be numeric",
								MessageBoxPresenter.Severity.SEVERE, true);
						return false;
					}
				} else if (v.equalsIgnoreCase("currency")) {
					int decimalIndex = value.indexOf(
							ClientProperties.getInstance().getStringValue(ClientProperties.CURRENCY_DELIMITER, "."));
					String dollars = value;
					String cents = "0";
					if (decimalIndex == 0) {
						dollars = "0";
						cents = value;
					} else if (decimalIndex > 0) {
						dollars = value.substring(0, decimalIndex);
						cents = value.substring(decimalIndex + 1);
					}

					try {
						Integer.parseInt(dollars);
						Integer.parseInt(cents);
					} catch (NumberFormatException ex) {
						MessageBoxPresenter.getInstance().show("Form Error!",
								attributes.get(NAME) + " must be in DD"
										+ ClientProperties.getInstance()
												.getStringValue(ClientProperties.CURRENCY_DELIMITER, ".")
										+ " format",
								MessageBoxPresenter.Severity.SEVERE, true);
						return false;
					}
				} else if (v.equalsIgnoreCase("date")) {
					String pattern = ClientProperties.getInstance().getStringValue(ClientProperties.DATE_FORMAT,
							ClientProperties.DEFAULT_DATE_FORMAT);
					try {
						DateTimeFormat.getFormat(pattern).parse(value);
					} catch (IllegalArgumentException ex) {
						MessageBoxPresenter.getInstance().show("Form Error!",
								attributes.get(NAME) + " must be in "
										+ ClientProperties.getInstance().getStringValue(ClientProperties.DATE_FORMAT,
												ClientProperties.DEFAULT_DATE_FORMAT)
										+ " format",
								MessageBoxPresenter.Severity.SEVERE, true);
						return false;
					}
				}
			}
			return true;
		}
	}

	public Widget renderForm(String from, long timeStamp, String formId, String formDef, String me, boolean smallSpace,
			FormListener listener) {
		Panel panel = renderPanel();

		SubmitHandler validator = new SubmitHandler(formId, listener);

		String[] lines = formDef.split("(\\r\\n|\\r|\\n)");
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}

			String[] columns = line.split("\\|");
			if (columns[0].equalsIgnoreCase("label")) {
				renderLabel(columns, panel);
			} else if (columns[0].equalsIgnoreCase("text")) {
				renderTextField(columns, panel, false, validator);
			} else if (columns[0].equalsIgnoreCase("password")) {
				renderTextField(columns, panel, true, validator);
			} else if (columns[0].equalsIgnoreCase("textarea")) {
				renderTextArea(columns, panel, true, validator);
			} else if (columns[0].equalsIgnoreCase("dropdown")) {
				renderDropdown(columns, panel, validator);
			} else if (columns[0].equalsIgnoreCase("buttons")) {
				renderButtons(columns, panel, validator);
				// This is the last line. Will ignore any more lines
				break;
			}
		}

		return panel;
	}

	protected void renderButtons(String[] columns, Panel panel, SubmitHandler submitter) {
		// column 1 = submit button label
		// column 2 = reset button label
		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.setWidth("100%");
		panel.add(buttonPanel);

		Button submit = new Button(getColumn(columns, 1, "Submit"));
		submit.addClickHandler(submitter);
		buttonPanel.add(submit);

		if (columns.length >= 3) {
			ResetButton reset = new ResetButton(getColumn(columns, 2, "Reset"));
			buttonPanel.add(reset);
		}
	}

	protected Panel renderPanel() {
		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(5);
		panel.setWidth("100%");
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
		// column 5 = size

		renderLabel(columns, panel);

		TextBox textBox;

		if (password) {
			textBox = new PasswordTextBox();
		} else {
			textBox = new TextBox();
		}
		panel.add(textBox);

		String ssize = getColumn(columns, 5, "");

		if (!ssize.isEmpty()) {
			textBox.setVisibleLength(Integer.parseInt(ssize));
		} else {
			textBox.setWidth("100%");
		}

		submitter.addAttribute(textBox, NAME, getColumn(columns, 2, "text"));
		submitter.addAttribute(textBox, REQUIRED, getColumn(columns, 3, "false"));
		submitter.addAttribute(textBox, TYPE, getColumn(columns, 4, "text"));
	}

	protected void renderTextArea(String[] columns, Panel panel, boolean password, SubmitHandler submitter) {
		// column 1 = label
		// column 2 = name
		// column 3 = required (true/false)
		// column 4 = minimum length
		// column 5 = rows

		renderLabel(columns, panel);

		TextArea textArea = new TextArea();
		panel.add(textArea);

		String srows = getColumn(columns, 5, "");
		if (!srows.isEmpty()) {
			textArea.setVisibleLines(Integer.parseInt(srows));
		}

		textArea.setWidth("100%");

		submitter.addAttribute(textArea, NAME, getColumn(columns, 2, "textarea"));
		submitter.addAttribute(textArea, REQUIRED, getColumn(columns, 3, "false"));
		submitter.addAttribute(textArea, MINLENGTH, getColumn(columns, 4, "0"));
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

		listBox.setWidth("100%");
		submitter.addAttribute(listBox, NAME, getColumn(columns, 2, "dropdown"));
	}
}
