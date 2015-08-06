/**
 * 
 */
package com.quikj.ace.web.client.view;

import com.quikj.ace.web.client.presenter.LogoutPresenter;

/**
 * @author beckie
 * 
 */
public interface LogoutPanel {

	public void setPresenter(LogoutPresenter presenter);

	public void logOut();
}
