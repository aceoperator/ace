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
import com.quikj.ace.common.client.captcha.CaptchaFactory;
import com.quikj.ace.common.client.captcha.CaptchaWidget;
import com.quikj.ace.common.client.captcha.CaptchaWidget.CaptchaListener;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.presenter.LostUsernamePresenter;
import com.quikj.ace.web.client.view.CaptchaProperties;
import com.quikj.ace.web.client.view.LostUsernamePanel;

/**
 * @author becky
 * 
 */
public class DesktopLostUsernamePanel extends LayoutPanel implements
		LostUsernamePanel, CaptchaListener {

	private VerticalPanel collectAddressForm;
	private TextBox address;
	private HTML caption;
	private LostUsernamePresenter presenter;
	private Label addressLabel;
	private HorizontalPanel buttonPanel;
	private CaptchaWidget captcha;

	public DesktopLostUsernamePanel() {
		super();
		setSize("100%", "100%");

		boolean fromLoginPage = true;
		String startPage = ClientProperties.getInstance().getStringValue(
				ClientProperties.ONCLICK_START_PAGE, null);
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

		collectAddressForm = new VerticalPanel();
		collectAddressForm.setSpacing(5);
		scrollPanel.setWidget(collectAddressForm);

		caption = new HTML("<p>"
				+ ApplicationController.getMessages()
						.DesktopLostUsernamePanel_formInstructions());
		collectAddressForm.add(caption);

		addressLabel = new Label(ApplicationController.getMessages()
				.DesktopLostUsernamePanel_email());
		collectAddressForm.add(addressLabel);

		address = new TextBox();
		collectAddressForm.add(address);
		address.setVisibleLength(30);
		address.addKeyUpHandler(new KeyUpHandler() {

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
				address.setFocus(true);
			}
		});

		collectAddressForm.add(new HTML("<p>"));

		captcha = CaptchaFactory.create(CaptchaProperties.properties());
		collectAddressForm.add(captcha.render(this));
		
		collectAddressForm.add(new HTML("<p>"));
		buttonPanel = new HorizontalPanel();
		collectAddressForm.add(buttonPanel);
		buttonPanel.setSpacing(5);
		buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		Button submitButton = new Button(ApplicationController.getMessages()
				.DesktopLostUsernamePanel_submit());
		buttonPanel.add(submitButton);
		submitButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				processSubmit();
			}
		});

		Button resetButton = new Button(ApplicationController.getMessages()
				.DesktopLostUsernamePanel_reset());
		buttonPanel.add(resetButton);
		resetButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				reset();
			}
		});

		if (fromLoginPage) {
			Button cancelButton = new Button(ApplicationController
					.getMessages().DesktopLostUsernamePanel_cancel());
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
		captcha.reset();
		address.setText("");
		address.setFocus(true);
	}

	public void processSubmit() {
		presenter.emailAddressSubmitted(address.getText(),
				captcha.getCaptcha(), captcha.getType());
	}

	@Override
	public void setPresenter(LostUsernamePresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void captchaEntered(String captcha, String captchaType) {
		processSubmit();		
	}
}
