/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import java.util.Date;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.presenter.UserBusyEmailPresenter;
import com.quikj.ace.web.client.view.UserBusyEmailPanel;

/**
 * @author amit
 * 
 */
public class DesktopUserBusyEmailPanel extends LayoutPanel implements
		UserBusyEmailPanel {

	private static final String CAPTCHA_HELP = ApplicationController
			.getMessages().DesktopUserBusyEmailPanel_capchaHelp();
	private VerticalPanel messageForm;
	private TextBox name;
	private TextArea message;
	private HTML caption;
	private UserBusyEmailPresenter presenter;
	private Label messageLabel;
	private Label nameLabel;
	private HorizontalPanel buttonPanel;
	private Label emailLabel;
	private TextBox email;
	private Image captchaImg;
	private TextBox captchaText;
	private HorizontalPanel captchaPanel;
	private Label captchaLabel;
	private HTML captchaHelp;

	public DesktopUserBusyEmailPanel() {
		super();
		setSize("100%", "100%");

		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setTouchScrollingDisabled(false);
		scrollPanel.setAlwaysShowScrollBars(false);
		add(scrollPanel);
		scrollPanel.setSize("100%", "100%");
		setWidgetVerticalPosition(scrollPanel, Layout.Alignment.BEGIN);
		setWidgetLeftRight(scrollPanel, 0, Style.Unit.PCT, 0, Style.Unit.PCT);

		messageForm = new VerticalPanel();
		messageForm.setSpacing(5);
		scrollPanel.setWidget(messageForm);

		caption = new HTML("<p>"
				+ ApplicationController.getMessages()
						.DesktopUserBusyEmailPanel_allOpsBusyLeaveMessage());
		messageForm.add(caption);

		nameLabel = new Label(ApplicationController.getMessages()
				.DesktopUserBusyEmailPanel_name());
		messageForm.add(nameLabel);

		name = new TextBox();
		messageForm.add(name);
		name.setVisibleLength(30);
		name.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					email.setFocus(true);
				}
			}
		});

		messageForm.add(new HTML("<p>"));

		emailLabel = new Label(ApplicationController.getMessages()
				.DesktopUserBusyEmailPanel_email());
		messageForm.add(emailLabel);

		email = new TextBox();
		messageForm.add(email);
		email.setVisibleLength(30);
		email.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					message.setFocus(true);
				}
			}
		});
		
		messageForm.add(new HTML("<p>"));

		messageLabel = new Label(ApplicationController.getMessages()
				.DesktopUserBusyEmailPanel_message());
		messageForm.add(messageLabel);

		message = new TextArea();
		messageForm.add(message);
		message.setCharacterWidth(65);
		message.setVisibleLines(5);

		messageForm.add(new HTML("<p>"));

		captchaLabel = new Label(ApplicationController.getMessages()
				.DesktopUserBusyEmailPanel_typeChars());
		messageForm.add(captchaLabel);

		captchaImg = new Image(captchaImageUrl());
		messageForm.add(captchaImg);

		captchaPanel = new HorizontalPanel();
		messageForm.add(captchaPanel);

		captchaText = new TextBox();
		captchaPanel.add(captchaText);
		captchaText.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					processSubmit();
				}
			}
		});
		
		HTML sp = new HTML("&nbsp;");
		captchaPanel.add(sp);

		Button captchaResetButton = new Button(ApplicationController
				.getMessages().DesktopUserBusyEmailPanel_regenPicture());
		captchaPanel.add(captchaResetButton);
		captchaResetButton.setTitle(ApplicationController.getMessages()
				.DesktopUserBusyEmailPanel_instructionsToRegen());

		captchaResetButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				captchaImg.setUrl(captchaImageUrl());
			}
		});

		sp = new HTML("&nbsp;");
		captchaPanel.add(sp);

		captchaHelp = new HTML("<smallest>"
				+ ApplicationController.getMessages()
						.DesktopUserBusyEmailPanel_whatIsThis() + "</smallest>");
		captchaPanel.add(captchaHelp);

		captchaPanel.setCellVerticalAlignment(captchaHelp,
				HasVerticalAlignment.ALIGN_MIDDLE);
		captchaHelp.setTitle(CAPTCHA_HELP);

		messageForm.add(new HTML("<p>"));
		buttonPanel = new HorizontalPanel();
		messageForm.add(buttonPanel);
		buttonPanel.setSpacing(5);
		buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		Button submitButton = new Button(ApplicationController.getMessages()
				.DesktopUserBusyEmailPanel_submit());
		buttonPanel.add(submitButton);
		submitButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				processSubmit();
			}
		});

		Button resetButton = new Button(ApplicationController.getMessages()
				.DesktopUserBusyEmailPanel_reset());
		buttonPanel.add(resetButton);
		resetButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				reset();
			}
		});

	}

	public String captchaImageUrl() {
		return GWT.getModuleBaseURL() + "../jcaptcha?nocache="
				+ new Date().getTime()
				+ "&type=large";
	}

	public void reset() {
		name.setText("");
		message.setText("");
		email.setText("");
	}

	@Override
	public void setEmailSentResult(String error) {
		if (error == null) {
			caption.setHTML(ApplicationController.getMessages()
					.DesktopUserBusyEmailPanel_messageDelivered());
		} else {
			caption.setHTML(ApplicationController.getMessages()
					.DesktopUserBusyEmailPanel_messageFailed() + " : " + error);
		}

		messageForm.remove(nameLabel);
		messageForm.remove(messageLabel);
		messageForm.remove(emailLabel);
		messageForm.remove(name);
		messageForm.remove(message);
		messageForm.remove(email);
		messageForm.remove(buttonPanel);
		messageForm.remove(captchaImg);
		messageForm.remove(captchaPanel);
		messageForm.remove(captchaLabel);
		messageForm.remove(captchaHelp);
	}

	@Override
	public void setPresenter(UserBusyEmailPresenter presenter) {
		this.presenter = presenter;
	}

	public void processSubmit() {
		presenter.informationSubmitted(name.getText(), email.getText(),
				message.getText(), captchaText.getText());
	}

	@Override
	public String getCaptchaType() {
		return "large";
	}
}
