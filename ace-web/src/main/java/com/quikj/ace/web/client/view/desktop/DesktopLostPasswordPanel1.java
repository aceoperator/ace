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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.presenter.LostPasswordPresenter;
import com.quikj.ace.web.client.view.CaptchaFactory;
import com.quikj.ace.web.client.view.CaptchaWidget;
import com.quikj.ace.web.client.view.CaptchaWidget.CaptchaListener;
import com.quikj.ace.web.client.view.LostPasswordPanel;

/**
 * @author becky
 * 
 */
public class DesktopLostPasswordPanel1 extends LayoutPanel implements LostPasswordPanel, CaptchaListener {

	private VerticalPanel collectUsernameForm;
	private TextBox name;
	private HTML caption;
	private LostPasswordPresenter presenter;
	private Label nameLabel;
	private HorizontalPanel buttonPanel;
	private CaptchaWidget captcha;

	public DesktopLostPasswordPanel1() {
		super();
		setSize("100%", "100%");

		boolean fromLoginPage = true;
		String startPage = ClientProperties.getInstance().getStringValue(ClientProperties.ONCLICK_START_PAGE, null);
		if (startPage != null) {
			fromLoginPage = false;
		}

		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setTouchScrollingDisabled(false);
		scrollPanel.setAlwaysShowScrollBars(false);
		add(scrollPanel);
		scrollPanel.setSize("100%", "100%");
		setWidgetVerticalPosition(scrollPanel, Layout.Alignment.BEGIN);
		setWidgetLeftRight(scrollPanel, 0, Style.Unit.PCT, 0, Style.Unit.PCT);

		collectUsernameForm = new VerticalPanel();
		collectUsernameForm.setSpacing(5);
		scrollPanel.setWidget(collectUsernameForm);

		caption = new HTML(
				"<p>" + ApplicationController.getMessages().DesktopLostPasswordPanel1_usernameFormInstructions());
		collectUsernameForm.add(caption);

		nameLabel = new Label(ApplicationController.getMessages().DesktopLostPasswordPanel1_name());
		collectUsernameForm.add(nameLabel);

		name = new TextBox();
		collectUsernameForm.add(name);
		name.setVisibleLength(30);
		name.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					captcha.focus();
				}
			}
		});
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				name.setFocus(true);
			}
		});

		collectUsernameForm.add(new HTML("<p>"));

		captcha = CaptchaFactory.create();
		collectUsernameForm.add(captcha.render(this));

		buttonPanel = new HorizontalPanel();
		collectUsernameForm.add(buttonPanel);
		buttonPanel.setSpacing(5);
		buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		Button submitButton = new Button(ApplicationController.getMessages().DesktopLostPasswordPanel1_submit());
		buttonPanel.add(submitButton);
		submitButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				processSubmit();
			}
		});

		Button resetButton = new Button(ApplicationController.getMessages().DesktopLostPasswordPanel_reset());
		buttonPanel.add(resetButton);
		resetButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				reset();
			}
		});

		if (fromLoginPage) {
			Button cancelButton = new Button(ApplicationController.getMessages().DesktopLostPasswordPanel_cancel());
			buttonPanel.add(cancelButton);
			cancelButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					presenter.backToLoginPage();
				}
			});
		}
	}

	@Override
	public void reset() {
		name.setText("");
		name.setFocus(true);
		captcha.reset();
	}

	public void processSubmit() {
		presenter.userNameSubmitted(name.getText(), captcha.getCaptcha(), captcha.getType());
	}

	@Override
	public void setPresenter(LostPasswordPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void captchaEntered(String captcha, String type) {
		processSubmit();
	}
}
