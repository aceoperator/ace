/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import java.util.HashMap;

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
public class DesktopLostPasswordPanel2 extends LayoutPanel implements LostPasswordPanel, CaptchaListener {

	private VerticalPanel collectAnswersForm;
	private TextBox answer1;
	private TextBox answer2;
	private TextBox answer3;
	private HTML caption;
	private LostPasswordPresenter presenter;
	private HorizontalPanel buttonPanel;
	private CaptchaWidget captcha;
	private String name;

	public DesktopLostPasswordPanel2(String name, HashMap<Integer, String> questions) {
		super();
		this.name = name;
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

		collectAnswersForm = new VerticalPanel();
		collectAnswersForm.setSpacing(5);
		scrollPanel.setWidget(collectAnswersForm);

		caption = new HTML(
				"<p>" + ApplicationController.getMessages().DesktopLostPasswordPanel2_questionAnswerFormInstructions());
		collectAnswersForm.add(caption);

		for (Integer id : questions.keySet()) {
			Label label = new Label(questions.get(id));
			collectAnswersForm.add(label);

			TextBox answer = new TextBox();
			collectAnswersForm.add(answer);
			answer.setVisibleLength(30);
			answer.addKeyUpHandler(new KeyUpHandler() {

				@Override
				public void onKeyUp(KeyUpEvent event) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
						setNextFocus(event);
					}
				}
			});

			if (id == 0) {
				answer1 = answer;
			} else if (id == 1) {
				answer2 = answer;
			} else {
				answer3 = answer;
			}
		}

		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				setInitialFocus();
			}
		});

		collectAnswersForm.add(new HTML("<p>"));

		captcha = CaptchaFactory.create();
		collectAnswersForm.add(captcha.render(this));

		collectAnswersForm.add(new HTML("<p>"));
		buttonPanel = new HorizontalPanel();
		collectAnswersForm.add(buttonPanel);
		buttonPanel.setSpacing(5);
		buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		Button submitButton = new Button(ApplicationController.getMessages().DesktopLostPasswordPanel2_submit());
		buttonPanel.add(submitButton);
		submitButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				processSubmit(DesktopLostPasswordPanel2.this.name);
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

	private void setInitialFocus() {
		if (answer1 != null) {
			answer1.setFocus(true);
		} else if (answer2 != null) {
			answer2.setFocus(true);
		} else {
			answer3.setFocus(true);
		}
	}

	protected void setNextFocus(KeyUpEvent event) {
		if (event.getSource().equals(answer1)) {
			if (answer2 != null) {
				answer2.setFocus(true);
			} else if (answer3 != null) {
				answer3.setFocus(true);
			} else {
				captcha.focus();
			}
			return;
		}

		if (event.getSource().equals(answer2)) {
			if (answer3 != null) {
				answer3.setFocus(true);
			} else {
				captcha.focus();
			}
			return;
		}

		captcha.focus();
	}

	@Override
	public void reset() {
		captcha.reset();

		if (answer1 != null) {
			answer1.setText("");
		}

		if (answer2 != null) {
			answer2.setText("");
		}

		if (answer3 != null) {
			answer3.setText("");
		}

		setInitialFocus();
	}

	private void processSubmit(String name) {
		HashMap<Integer, String> answers = new HashMap<Integer, String>();

		if (answer1 != null) {
			answers.put(0, answer1.getText().trim());
		}

		if (answer2 != null) {
			answers.put(1, answer2.getText().trim());
		}

		if (answer3 != null) {
			answers.put(2, answer3.getText().trim());
		}

		presenter.answersSubmitted(name, answers, captcha.getCaptcha(), captcha.getType());
	}

	@Override
	public void setPresenter(LostPasswordPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void captchaEntered(String captcha, String type) {
		processSubmit(DesktopLostPasswordPanel2.this.name);
	}
}
