/**
 * 
 */
package com.quikj.ace.web.client.view;

import com.quikj.ace.web.client.presenter.ConfirmationDialogPresenter;

/**
 * @author amit
 * 
 */
public interface ConfirmationDialog {

	public void show(String title, String message, String icon,
			boolean showCancel);

	public void hide();

	public void setPresenter(ConfirmationDialogPresenter presenter);
}
