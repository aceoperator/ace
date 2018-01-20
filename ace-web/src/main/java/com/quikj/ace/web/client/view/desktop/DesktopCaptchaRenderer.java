/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.view.CaptchaWidget;
import com.quikj.ace.web.client.view.GoogleCaptchaWidget;
import com.quikj.ace.web.client.view.ImageCaptchaWidget;

/**
 * @author amit
 *
 */
public class DesktopCaptchaRenderer {

	public static CaptchaWidget create() {
		if (ClientProperties.getInstance().getStringValue(ClientProperties.RECAPTCHA2_SITE_KEY,
				null) == null) {
			return new ImageCaptchaWidget();
		} else {
			return new GoogleCaptchaWidget();
		}
	}	
}
