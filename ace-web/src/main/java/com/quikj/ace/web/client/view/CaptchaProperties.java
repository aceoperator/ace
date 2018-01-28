/**
 * 
 */
package com.quikj.ace.web.client.view;

import java.util.HashMap;

import com.quikj.ace.common.client.captcha.CaptchaConstants;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;

/**
 * @author amit
 *
 */
public class CaptchaProperties extends HashMap<String, Object> {

	private static final long serialVersionUID = 5644798187366702710L;

	private static CaptchaProperties instance;
	
	private CaptchaProperties() {
		put(CaptchaConstants.IMAGE_CAPTCHA_HELP_LABEL,
				ApplicationController.getMessages().DesktopUserBusyEmailPanel_capchaHelp());
		put(CaptchaConstants.IMAGE_CAPTCHA_TYPE_CHAR_LABEL,
				ApplicationController.getMessages().DesktopUserBusyEmailPanel_typeChars());
		put(CaptchaConstants.IMAGE_CAPTCHA_REGEN_BUTTON_LABEL,
				ApplicationController.getMessages().DesktopUserBusyEmailPanel_regenPicture());
		put(CaptchaConstants.IMAGE_CAPTCHA_REGEN_INSTRUCTIONS_LABEL,
				ApplicationController.getMessages().DesktopUserBusyEmailPanel_instructionsToRegen());
		put(CaptchaConstants.IMAGE_CAPTCHA_WHATSTHIS_LABEL,
				ApplicationController.getMessages().DesktopUserBusyEmailPanel_whatIsThis());

		put(CaptchaConstants.RECAPTCHA2_SITE_KEY,
				ClientProperties.getInstance().getStringValue(CaptchaConstants.RECAPTCHA2_SITE_KEY, null));
		put(CaptchaConstants.RECAPTCHA2_LANGUAGE, ApplicationController.getInstance().getLocale());
	}
	
	public static CaptchaProperties properties() {
		if (instance == null) {
			instance = new CaptchaProperties();
		}
		return instance;
	}
}
