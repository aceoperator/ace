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
	void setPresenter(UserBusyEmailPresenter presenter);

	void setEmailSentResult(String error);
}
