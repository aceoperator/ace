/**
 * 
 */
package com.quikj.ace.common.client.captcha;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author amit
 *
 */
public interface CaptchaWidget {
	public interface CaptchaListener {
		void captchaEntered(String captcha, String captchaType);
	}
	
	Widget render(CaptchaListener listener);
	void focus();
	void reset();
	String getCaptcha();
	String getType();
	Widget getWidget();
}
