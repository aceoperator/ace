/**
 * 
 */
package com.quikj.ace.web.client.view;

import com.quikj.ace.web.client.ClientProperties;

/**
 * @author amit
 *
 */
public class CaptchaFactory {

	public static CaptchaWidget create() {
		if (ClientProperties.getInstance().getStringValue(ClientProperties.RECAPTCHA2_SITE_KEY,
				null) == null) {
			return new ImageCaptchaWidget();
		} else {
			return new GoogleCaptchaWidget();
		}
	}	
}
