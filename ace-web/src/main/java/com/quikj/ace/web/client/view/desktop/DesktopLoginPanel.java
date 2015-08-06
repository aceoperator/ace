/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.presenter.LoginPresenter;
import com.quikj.ace.web.client.view.LoginPanel;

/**
 * @author amit
 * 
 */
public class DesktopLoginPanel extends LayoutPanel implements LoginPanel {

	private LoginPresenter presenter;
	private TextBox userNameText;
	private PasswordTextBox passwordText;
	private Button btnLogin;
	private Button btnReset;

	public DesktopLoginPanel() {
		super();
		setSize("100%", "100%");

		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setTouchScrollingDisabled(false);
		scrollPanel.setAlwaysShowScrollBars(false);
		add(scrollPanel);
		scrollPanel.setSize("100%", "100%");
		setWidgetVerticalPosition(scrollPanel, Layout.Alignment.BEGIN);
		setWidgetLeftRight(scrollPanel, 0, Style.Unit.PCT, 0, Style.Unit.PCT);

		VerticalPanel loginPanel = new VerticalPanel();
		scrollPanel.setWidget(loginPanel);

		loginPanel.setSpacing(5);

		HorizontalPanel labelPanel = new HorizontalPanel();
		loginPanel.setCellHorizontalAlignment(labelPanel,
				HasHorizontalAlignment.ALIGN_CENTER);
		loginPanel.add(labelPanel);
		labelPanel.setSpacing(5);

		Image icon = new Image(Images.LOGIN_MEDIUM);
		labelPanel.add(icon);

		Label headingLabel = new Label(ApplicationController.getMessages()
				.DesktopLoginPanel_enterNamePassword());
		labelPanel.add(headingLabel);
		labelPanel.setCellVerticalAlignment(headingLabel,
				HasVerticalAlignment.ALIGN_MIDDLE);

		Label userLabel = new Label(ApplicationController.getMessages()
				.DesktopLoginPanel_name());
		loginPanel.add(userLabel);

		userNameText = new TextBox();
		userNameText.setVisibleLength(50);
		loginPanel.add(userNameText);
		userNameText.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					passwordText.setFocus(true);
				}
			}
		});

		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				userNameText.setFocus(true);
			}
		});

		Label passwordLabel = new Label(ApplicationController.getMessages()
				.DesktopLoginPanel_password());
		loginPanel.add(passwordLabel);

		passwordText = new PasswordTextBox();
		passwordText.setVisibleLength(50);
		loginPanel.add(passwordText);

		passwordText.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					processLogin();
				}
			}
		});
		
		HorizontalPanel buttonPanel = new HorizontalPanel();
		loginPanel.add(buttonPanel);
		buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		buttonPanel.setSpacing(5);

		btnLogin = new Button(ApplicationController.getMessages()
				.DesktopLoginPanel_login());
		btnLogin.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				processLogin();
			}
		});
		buttonPanel.add(btnLogin);
		btnLogin.setFocus(true);
		
		btnReset = new Button(ApplicationController.getMessages()
				.DesktopLoginPanel_reset());
		btnReset.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				reset();
			}
		});
		buttonPanel.add(btnReset);

		loginPanel.add(new HTML("<p>"));

		HTML lostPasswordLink = new HTML("<u style='cursor:pointer;'>"
				+ ApplicationController.getMessages()
						.DesktopLoginPanel_lostPassword() + "</u>&nbsp;&nbsp;");
		loginPanel.add(lostPasswordLink);
		loginPanel.setCellHorizontalAlignment(lostPasswordLink,
				HasHorizontalAlignment.ALIGN_RIGHT);
		loginPanel.setCellVerticalAlignment(lostPasswordLink,
				HasVerticalAlignment.ALIGN_BOTTOM);
		lostPasswordLink.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.resetLostPassword();
			}
		});

		HTML lostUsernameLink = new HTML("<u style='cursor:pointer;'>"
				+ ApplicationController.getMessages()
						.DesktopLoginPanel_lostUsername() + "</u>&nbsp;&nbsp;");
		loginPanel.add(lostUsernameLink);
		loginPanel.setCellHorizontalAlignment(lostUsernameLink,
				HasHorizontalAlignment.ALIGN_RIGHT);
		loginPanel.setCellVerticalAlignment(lostUsernameLink,
				HasVerticalAlignment.ALIGN_BOTTOM);
		lostUsernameLink.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.recoverLostUsername();
			}
		});
	}

	public LoginPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void setPresenter(LoginPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void reset() {
		userNameText.setText("");
		passwordText.setText("");
	}

	public void processLogin() {
		DesktopLoginPanel.this.presenter.processLogin(userNameText
				.getText().trim(), passwordText.getText().trim());
	}
}
