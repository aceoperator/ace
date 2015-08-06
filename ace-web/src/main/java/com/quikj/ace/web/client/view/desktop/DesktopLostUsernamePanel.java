/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import java.util.Date;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.presenter.LostUsernamePresenter;
import com.quikj.ace.web.client.view.LostUsernamePanel;

/**
 * @author becky
 * 
 */
public class DesktopLostUsernamePanel extends LayoutPanel implements
		LostUsernamePanel {

	private static final String CAPTCHA_HELP = ApplicationController
			.getMessages().DesktopUserBusyEmailPanel_capchaHelp();
	private VerticalPanel collectAddressForm;
	private TextBox address;
	private HTML caption;
	private LostUsernamePresenter presenter;
	private Label addressLabel;
	private HorizontalPanel buttonPanel;
	private Image captchaImg;
	private TextBox captchaText;
	private HorizontalPanel captchaPanel;
	private Label captchaLabel;
	private HTML captchaHelp;

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
					captchaText.setFocus(true);
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

		// TODO create a common captcha widget
		captchaLabel = new Label(ApplicationController.getMessages()
				.DesktopUserBusyEmailPanel_typeChars());
		collectAddressForm.add(captchaLabel);

		captchaImg = new Image(GWT.getModuleBaseURL() + "../jcaptcha?nocache="
				+ new Date().getTime());
		collectAddressForm.add(captchaImg);

		captchaPanel = new HorizontalPanel();
		collectAddressForm.add(captchaPanel);

		captchaText = new TextBox();
		captchaPanel.add(captchaText);
		captchaText.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					processSubmit();
				}
			}
		});

		HTML sp = new HTML("&nbsp;");
		captchaPanel.add(sp);

		Button captchaResetButton = new Button(ApplicationController
				.getMessages().DesktopUserBusyEmailPanel_regenPicture());
		captchaPanel.add(captchaResetButton);
		captchaResetButton.setTitle(ApplicationController.getMessages()
				.DesktopUserBusyEmailPanel_instructionsToRegen());

		captchaResetButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				captchaText.setText("");
				captchaText.setFocus(true);
				captchaImg.setUrl(GWT.getModuleBaseURL()
						+ "../jcaptcha?nocache=" + new Date().getTime());
			}
		});

		sp = new HTML("&nbsp;");
		captchaPanel.add(sp);

		captchaHelp = new HTML("<smallest>"
				+ ApplicationController.getMessages()
						.DesktopUserBusyEmailPanel_whatIsThis() + "</smallest>");
		captchaPanel.add(captchaHelp);

		captchaPanel.setCellVerticalAlignment(captchaHelp,
				HasVerticalAlignment.ALIGN_MIDDLE);
		captchaHelp.setTitle(CAPTCHA_HELP);

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
		captchaText.setText("");
		address.setText("");
		captchaImg.setUrl(GWT.getModuleBaseURL() + "../jcaptcha?nocache="
				+ new Date().getTime());
		address.setFocus(true);
	}

	public void processSubmit() {
		presenter.emailAddressSubmitted(address.getText(),
				captchaText.getText());
	}

	@Override
	public void setPresenter(LostUsernamePresenter presenter) {
		this.presenter = presenter;
	}

}
