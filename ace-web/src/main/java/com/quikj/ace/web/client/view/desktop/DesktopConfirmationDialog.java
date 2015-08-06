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
import com.quikj.ace.web.client.presenter.ConfirmationDialogPresenter;
import com.quikj.ace.web.client.view.ConfirmationDialog;

/**
 * @author amit
 * 
 */
public class DesktopConfirmationDialog extends DialogBox implements
		ConfirmationDialog {

	private static final String DEFAULT_ICON = Images.WARNING_MEDIUM;
	private Image image = new Image(DEFAULT_ICON);
	private HTML label = new HTML(ApplicationController.getMessages()
			.DesktopConfirmationDialog_text());

	private FlexTable flexTable;
	private ConfirmationDialogPresenter presenter;
	private Button yesButton;
	private Button noButton;
	private Button cancelButton;

	public DesktopConfirmationDialog() {
		super(false, true);
		setSize("100%", "100%");
		setTitle(ApplicationController.getMessages()
				.DesktopConfirmationDialog_message());

		flexTable = new FlexTable();
		setWidget(flexTable);
		flexTable.setSize("100%", "100%");
		flexTable.setCellSpacing(2);
		flexTable.setCellPadding(2);

		flexTable.setWidget(0, 0, image);
		flexTable.getFlexCellFormatter().setWidth(0, 0, "25%");
		image.setSize(Images.MEDIUM_IMG_WIDTH, Images.MEDIUM_IMG_HEIGHT);

		flexTable.setWidget(0, 1, label);
		flexTable.getFlexCellFormatter().setWidth(0, 1, "75%");

		HorizontalPanel buttonPanel = new HorizontalPanel();
		flexTable.setWidget(1, 0, buttonPanel);
		buttonPanel.setSpacing(5);
		flexTable.getFlexCellFormatter().setColSpan(1, 0, 2);

		yesButton = new Button(ApplicationController.getMessages()
				.DesktopConfirmationDialog_yes());
		buttonPanel.add(yesButton);
		yesButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
				presenter.yes();
			}
		});

		noButton = new Button(ApplicationController.getMessages()
				.DesktopConfirmationDialog_no());
		buttonPanel.add(noButton);
		noButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
				presenter.no();
			}
		});

		cancelButton = new Button(ApplicationController.getMessages()
				.DesktopConfirmationDialog_cancel());
		buttonPanel.add(cancelButton);
		flexTable.getCellFormatter().setVerticalAlignment(0, 0,
				HasVerticalAlignment.ALIGN_MIDDLE);
		flexTable.getCellFormatter().setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_CENTER);
		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
				presenter.cancel();
			}
		});
	}

	@Override
	public void show(String title, String message, String icon,
			boolean showCancel) {
		getCaption().setText(title);

		label.setHTML(message);

		if (icon != null) {
			image.setUrl(icon);
		} else {
			image.setUrl(DEFAULT_ICON);
		}

		if (showCancel) {
			cancelButton.setEnabled(true);
		} else {
			cancelButton.setEnabled(false);
		}

		// setPopupPositionAndShow(new PositionCallback() {
		// @Override
		// public void setPosition(int offsetWidth, int offsetHeight) {
		//
		// int left = (ApplicationController.getInstance().getRootPanel()
		// .getOffsetWidth() - offsetWidth) / 3;
		// int top = (ApplicationController.getInstance().getRootPanel()
		// .getOffsetHeight() - offsetHeight) / 3;
		// setPopupPosition(left, top);
		// }
		// });

		show();
		center();
	}

	@Override
	public void hide() {
		super.hide();
	}

	@Override
	public void setPresenter(ConfirmationDialogPresenter presenter) {
		this.presenter = presenter;
	}
}
