/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.presenter.MessageBoxPresenter;
import com.quikj.ace.web.client.view.MessageBox;

/**
 * @author amit
 * 
 */
public class DesktopMessageBox extends DialogBox implements MessageBox {

	private static final int PAD_SIZE = 60;
	private static final String DEFAULT_ICON = Images.INFO_MEDIUM;
	private Image image = new Image(DEFAULT_ICON);
	private HTML label = new HTML();
	private Button closeButton = new Button(ApplicationController.getMessages()
			.DesktopMessageBox_close());
	private FlexTable flexTable;
	private MessageBoxPresenter presenter;

	public DesktopMessageBox() {
		super(false, true);
		setTitle(ApplicationController.getMessages()
				.DesktopMessageBox_message());

		flexTable = new FlexTable();
		setWidget(flexTable);
		flexTable.setSize("100%", "100%");
		flexTable.setCellSpacing(2);
		flexTable.setCellPadding(2);

		flexTable.setWidget(0, 0, image);
		// flexTable.getFlexCellFormatter().setWidth(0, 0, "25%");

		flexTable.setWidget(0, 1, label);
		// flexTable.getFlexCellFormatter().setWidth(0, 1, "75%");
		label.setWidth("100%");

		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.setSpacing(5);
		buttonPanel.setWidth("100%");

		flexTable.setWidget(1, 0, buttonPanel);
		flexTable.getFlexCellFormatter().setColSpan(1, 0, 2);

		buttonPanel.add(closeButton);
		buttonPanel.setCellHorizontalAlignment(closeButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		flexTable.getCellFormatter().setVerticalAlignment(0, 0,
				HasVerticalAlignment.ALIGN_MIDDLE);
		flexTable.getCellFormatter().setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_CENTER);

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
		getCaption().setText(title);

		label.setHTML(pad(message));

		if (icon != null) {
			image.setUrl(icon);
		} else {
			image.setUrl(DEFAULT_ICON);
		}

		closeButton.setVisible(enableClose);

		show();
		center();
	}

	private String pad(String message) {
		StringBuffer buffer = new StringBuffer(message);
		if (message.length() < PAD_SIZE) {
			for (int i = message.length(); i < PAD_SIZE; i++) {
				buffer.append("&nbsp");
			}
		}
		return buffer.toString();
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
