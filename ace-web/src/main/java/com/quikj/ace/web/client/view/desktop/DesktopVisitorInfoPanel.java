/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.presenter.VisitorInfoPresenter;
import com.quikj.ace.web.client.view.VisitorInfoPanel;

/**
 * @author amit
 * 
 */
public class DesktopVisitorInfoPanel extends LayoutPanel implements
		VisitorInfoPanel {

	private TextBox name;
	private TextArea message;
	private HTML caption;
	private VisitorInfoPresenter presenter;
	private TextBox email;

	public DesktopVisitorInfoPanel() {
		super();
		setSize("100%", "100%");

		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setTouchScrollingDisabled(false);
		scrollPanel.setAlwaysShowScrollBars(true);
		add(scrollPanel);
		scrollPanel.setSize("100%", "100%");
		setWidgetVerticalPosition(scrollPanel, Layout.Alignment.BEGIN);
		setWidgetLeftRight(scrollPanel, 0, Style.Unit.PCT, 0, Style.Unit.PCT);

		VerticalPanel infoPanel = new VerticalPanel();
		infoPanel.setSpacing(5);
		scrollPanel.setWidget(infoPanel);

		String captionDefault = ApplicationController.getMessages()
				.DesktopVisitorInfoPanel_enterAdditionalInfo() + ".";
		String captionText = ClientProperties.getInstance().getStringValue(
				ClientProperties.VISITOR_FORM_CAPTION, captionDefault);
		caption = new HTML("<p>" + captionText);
		infoPanel.add(caption);

		Label nameLabel = new Label(ApplicationController.getMessages()
				.DesktopVisitorInfoPanel_name());
		infoPanel.add(nameLabel);

		name = new TextBox();
		infoPanel.add(name);
		name.setVisibleLength(30);
		name.setMaxLength(60);

		name.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					if (email != null) {
						email.setFocus(true);
					} else {
						message.setFocus(true);
					}
				}
			}
		});
		
		infoPanel.add(new HTML("<p>"));

		boolean hideEmail = ClientProperties.getInstance().getBooleanValue(
				ClientProperties.VISITOR_EMAIL_HIDDEN, false);
		if (!hideEmail) {
			Label emailLabel = new Label(ApplicationController.getMessages()
					.DesktopVisitorInfoPanel_email());
			infoPanel.add(emailLabel);

			email = new TextBox();
			infoPanel.add(email);
			email.setVisibleLength(30);
			email.setMaxLength(255);

			email.addKeyUpHandler(new KeyUpHandler() {

				@Override
				public void onKeyUp(KeyUpEvent event) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
						message.setFocus(true);
					}
				}
			});

			infoPanel.add(new HTML("<p>"));
		}

		Label messageLabel = new Label(ApplicationController.getMessages()
				.DesktopVisitorInfoPanel_additionalInfo());
		infoPanel.add(messageLabel);

		message = new TextArea();
		infoPanel.add(message);
		message.setCharacterWidth(65);
		message.setVisibleLines(5);

		HorizontalPanel buttonPanel = new HorizontalPanel();
		infoPanel.add(buttonPanel);
		buttonPanel.setSpacing(5);
		buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		Button submitButton = new Button(ApplicationController.getMessages()
				.DesktopVisitorInfoPanel_submit());
		buttonPanel.add(submitButton);
		submitButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.infoSubmitted(name.getText(), email == null ? null
						: email.getText(),
						message.getText());
			}
		});

		Button resetButton = new Button(ApplicationController.getMessages()
				.DesktopVisitorInfoPanel_reset());
		buttonPanel.add(resetButton);
		resetButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				reset();
			}
		});
	}

	public void reset() {
		name.setText("");

		if (email != null) {
			email.setText("");
		}

		message.setText("");
	}

	@Override
	public void setPresenter(VisitorInfoPresenter presenter) {
		this.presenter = presenter;
	}

}
