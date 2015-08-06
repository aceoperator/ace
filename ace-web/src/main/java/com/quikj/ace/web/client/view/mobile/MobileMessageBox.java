/**
 * 
 */
package com.quikj.ace.web.client.view.mobile;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.presenter.MessageBoxPresenter;
import com.quikj.ace.web.client.view.MessageBox;

/**
 * @author amit
 * 
 */
public class MobileMessageBox extends DialogBox implements MessageBox {

	private HTML label;
	private Button closeButton;
	private MessageBoxPresenter presenter;

	public MobileMessageBox() {
		super(false, true);
		setAnimationEnabled(false);
		setModal(true);
		setTitle(ApplicationController.getMessages()
				.DesktopMessageBox_message());

		VerticalPanel panel = new VerticalPanel();
		panel.setSize((Window.getClientWidth() - 10) + "px", (Window.getClientHeight() - 10) + "px");
		setWidget(panel);
		
		label = new HTML();
		panel.add(label);
		panel.setCellHorizontalAlignment(label, HasHorizontalAlignment.ALIGN_CENTER);
		label.setWidth("100%");

		closeButton = new Button(ApplicationController.getMessages()
				.DesktopMessageBox_close());
		panel.add(closeButton);
		panel.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_CENTER);
		
		closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
				presenter.close();
			}
		});
	}

	@Override
	public void show(String title, String message, String icon,
			boolean enableClose) {
		label.setHTML(message);
		closeButton.setVisible(enableClose);
		show();
//		center();
	}


	@Override
	public void hide() {
		super.hide();
	}

	@Override
	public void setPresenter(MessageBoxPresenter presenter) {
		this.presenter = presenter;
	}
}
