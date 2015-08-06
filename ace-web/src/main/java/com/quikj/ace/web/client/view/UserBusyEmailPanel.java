/**
 * 
 */
package com.quikj.ace.web.client.view;

import com.quikj.ace.web.client.presenter.UserBusyEmailPresenter;

/**
 * @author amit
 * 
 */
public interface UserBusyEmailPanel {

	public void setPresenter(UserBusyEmailPresenter presenter);

	public void setEmailSentResult(String error);
	
	public String getCaptchaType();
}
