/**
 * 
 */
package com.quikj.ace.common.client.captcha;

import java.util.Map;

import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.common.client.salesfront.recaptcha.RecaptchaWidget;

/**
 * @author amit
 *
 */
public class Recaptcha2Widget implements CaptchaWidget {

	private RecaptchaWidget recaptcha;

	private Map<String, Object> properties;

	public Recaptcha2Widget(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public Widget render(CaptchaListener listener) {
		initLanguage();
		recaptcha = new RecaptchaWidget((String) properties.get(CaptchaConstants.RECAPTCHA2_SITE_KEY));
		return recaptcha;
	}

	@Override
	public void focus() {
	}

	@Override
	public void reset() {
		recaptcha.reset();
	}

	@Override
	public String getCaptcha() {
		return recaptcha.getResponse();
	}

	private void initLanguage() {
		if (RecaptchaWidget.getLanguage() != null) {
			return;
		}

		String lang = (String) properties.getOrDefault(CaptchaConstants.RECAPTCHA2_LANGUAGE, "en_US");
		int index = lang.indexOf("_");
		if (index > 0) {
			lang = lang.substring(0, index);
		}
		RecaptchaWidget.setLanguage(lang);
	}

	@Override
	public String getType() {
		return "RECAPTCHA";
	}

	@Override
	public Widget getWidget() {
		return recaptcha;
	}
}
