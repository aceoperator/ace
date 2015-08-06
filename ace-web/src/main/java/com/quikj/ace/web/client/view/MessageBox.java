/**
 * 
 */
package com.quikj.ace.web.client.view;

import com.quikj.ace.web.client.presenter.MessageBoxPresenter;

/**
 * @author amit
 *
 */
public interface MessageBox {
	
	void show(String title, String message, String icon, boolean showClose);
	void hide();
	void setPresenter(MessageBoxPresenter presenter);
}
