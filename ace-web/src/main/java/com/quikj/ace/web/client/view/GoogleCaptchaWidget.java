/**
 * 
 */
package com.quikj.ace.web.client.view;

import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.common.client.salesfront.recaptcha.RecaptchaWidget;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;

/**
 * @author amit
 *
 */
public class GoogleCaptchaWidget implements CaptchaWidget {

	private String recaptcha2Key =  ClientProperties.getInstance().getStringValue(ClientProperties.RECAPTCHA2_SITE_KEY,
			null);
	private RecaptchaWidget recaptcha;

	@Override
	public Widget render(CaptchaListener listener) {
		initLanguage();
		recaptcha = new RecaptchaWidget(recaptcha2Key);
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

		String lang = ApplicationController.getInstance().getLocale();
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
