/**
 * 
 */
package com.quikj.ace.web.client.view;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.web.client.ApplicationController;

/**
 * @author amit
 *
 */
public class ImageCaptchaWidget implements CaptchaWidget {

	private static final String CAPTCHA_HELP = ApplicationController.getMessages()
			.DesktopUserBusyEmailPanel_capchaHelp();

	private CaptchaListener listener;
	private Image captchaImg;
	private TextBox captchaText;

	private VerticalPanel panel;

	@Override
	public Widget render(CaptchaListener listener) {
		this.listener = listener;

		panel = new VerticalPanel();
		panel.setSpacing(5);

		Label captchaLabel = new Label(ApplicationController.getMessages().DesktopUserBusyEmailPanel_typeChars());
		panel.add(captchaLabel);

		captchaImg = new Image(GWT.getModuleBaseURL() + "../jcaptcha?nocache=" + new Date().getTime());
		panel.add(captchaImg);

		HorizontalPanel captchaPanel = new HorizontalPanel();
		panel.add(captchaPanel);

		captchaText = new TextBox();
		captchaPanel.add(captchaText);
		captchaText.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					ImageCaptchaWidget.this.listener.captchaEntered(captchaText.getText(), getType());
				}
			}
		});

		HTML sp = new HTML("&nbsp;");
		captchaPanel.add(sp);

		Button captchaResetButton = new Button(
				ApplicationController.getMessages().DesktopUserBusyEmailPanel_regenPicture());
		captchaPanel.add(captchaResetButton);
		captchaResetButton
				.setTitle(ApplicationController.getMessages().DesktopUserBusyEmailPanel_instructionsToRegen());

		captchaResetButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				captchaText.setText("");
				captchaText.setFocus(true);
				captchaImg.setUrl(GWT.getModuleBaseURL() + "../jcaptcha?nocache=" + new Date().getTime());
			}
		});

		sp = new HTML("&nbsp;");
		captchaPanel.add(sp);

		HTML captchaHelp = new HTML("<smallest>"
				+ ApplicationController.getMessages().DesktopUserBusyEmailPanel_whatIsThis() + "</smallest>");
		captchaPanel.add(captchaHelp);

		captchaPanel.setCellVerticalAlignment(captchaHelp, HasVerticalAlignment.ALIGN_MIDDLE);
		captchaHelp.setTitle(CAPTCHA_HELP);

		return panel;
	}

	@Override
	public void focus() {
		captchaText.setFocus(true);
	}

	@Override
	public void reset() {
		captchaText.setText("");
		captchaImg.setUrl(GWT.getModuleBaseURL() + "../jcaptcha?nocache=" + new Date().getTime());
	}

	@Override
	public String getCaptcha() {
		return captchaText.getText().trim();
	}

	@Override
	public String getType() {
		return "IMAGE";
	}

	@Override
	public Widget getWidget() {
		return panel;
	}
}