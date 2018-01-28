/**
 * 
 */
package com.quikj.ace.common.client.captcha;

import java.util.Map;


/**
 * @author amit
 *
 */
public class CaptchaFactory {

	public static CaptchaWidget create(Map<String, Object> properties) {		
		if (properties.getOrDefault(CaptchaConstants.RECAPTCHA2_SITE_KEY, null) == null) {
			return new ImageCaptchaWidget(properties);
		} else {
			return new GoogleCaptchaWidget(properties);
		}
	}
}
