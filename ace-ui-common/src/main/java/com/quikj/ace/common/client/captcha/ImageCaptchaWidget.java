/**
 * 
 */
package com.quikj.ace.common.client.captcha;

import java.util.Date;
import java.util.Map;

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

/**
 * @author amit
 *
 */
public class ImageCaptchaWidget implements CaptchaWidget {

	private CaptchaListener listener;
	private Image captchaImg;
	private TextBox captchaText;

	private VerticalPanel panel;

	private Map<String, Object> properties;

	public ImageCaptchaWidget(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public Widget render(CaptchaListener listener) {
		this.listener = listener;

		panel = new VerticalPanel();
		panel.setSpacing(5);

		Label captchaLabel = new Label((String) properties.getOrDefault(CaptchaConstants.IMAGE_CAPTCHA_TYPE_CHAR_LABEL,
				"Please type the characters you see in the image below"));
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
				(String) properties.getOrDefault(CaptchaConstants.IMAGE_CAPTCHA_REGEN_BUTTON_LABEL, "Regenerate"));
		captchaPanel.add(captchaResetButton);
		captchaResetButton.setTitle((String) properties.getOrDefault(
				CaptchaConstants.IMAGE_CAPTCHA_REGEN_INSTRUCTIONS_LABEL,
				"If you are not able to read the characters in the picture, click on this button to generate another picture"));

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

		HTML captchaHelp = new HTML(
				"<smallest>" + properties.getOrDefault(CaptchaConstants.IMAGE_CAPTCHA_WHATSTHIS_LABEL, "What's this?")
						+ "</smallest>");
		captchaPanel.add(captchaHelp);

		captchaPanel.setCellVerticalAlignment(captchaHelp, HasVerticalAlignment.ALIGN_MIDDLE);
		captchaHelp.setTitle((String) properties.getOrDefault(CaptchaConstants.IMAGE_CAPTCHA_HELP_LABEL,
				"By verifying the characters you entered against the characters in the picture, we will be able to differentiate you from a 'BOT'."
				+ " A BOT is a program often used by spammers to send unsolicited mass email messages by exploiting a form similar to this one."));
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