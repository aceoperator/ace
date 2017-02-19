/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import java.util.Date;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.adapter.GroupInfo;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.AudioSettings;
import com.quikj.ace.web.client.ChatSettings;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.presenter.UserInfoPresenter;
import com.quikj.ace.web.client.theme.ThemeFactory;
import com.quikj.ace.web.client.view.UserInfoPanel;
import com.quikj.ace.web.client.view.ViewUtils;

/**
 * @author beckie
 * 
 */
public class DesktopUserInfoPanel extends StackLayoutPanel implements UserInfoPanel {

	private static final double HEADER_SIZE = 30.0;
	private static final int CHANGE_PASSWORD_PANEL_INDEX = 2;
	private UserInfoPresenter presenter;
	private HTML userInfo;
	private CheckBox dndCheckBox;
	private CheckBox autoAnswerCheckBox;
	private CheckBox buzzCheckBox;
	private CheckBox chimeCheckBox;
	private CheckBox ringingCheckBox;
	private CheckBox presenceCheckBox;
	private PasswordTextBox oldPasswordTextBox;
	private PasswordTextBox passwordTextBox;
	private PasswordTextBox verifyPasswordTextBox;
	private CheckBoxClickHandler clickHandler = new CheckBoxClickHandler();
	private ListBox themeListBox;
	private HTML groupInfo;
	private Image avatarImage;
	private boolean navigationLocked = false;

	class CheckBoxClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			if (event.getSource() == dndCheckBox) {
				presenter.processEvent(UserInfoPresenter.UserInfoEvents.CHAT_DND, dndCheckBox.getValue());
			} else if (event.getSource() == autoAnswerCheckBox) {
				presenter.processEvent(UserInfoPresenter.UserInfoEvents.CHAT_AUTOANSWER, autoAnswerCheckBox.getValue());
			} else if (event.getSource() == buzzCheckBox) {
				presenter.processEvent(UserInfoPresenter.UserInfoEvents.AUDIO_BUZZ, buzzCheckBox.getValue());
			} else if (event.getSource() == chimeCheckBox) {
				presenter.processEvent(UserInfoPresenter.UserInfoEvents.AUDIO_CHAT_NOTIFICATION,
						chimeCheckBox.getValue());
			} else if (event.getSource() == ringingCheckBox) {
				presenter.processEvent(UserInfoPresenter.UserInfoEvents.AUDIO_NEW_CHAT, ringingCheckBox.getValue());
			} else if (event.getSource() == presenceCheckBox) {
				presenter.processEvent(UserInfoPresenter.UserInfoEvents.AUDIO_PRESENCE_NOTIFICATION,
						presenceCheckBox.getValue());
			}
		}
	}

	public DesktopUserInfoPanel() {
		super(Unit.PX);
		setSize("100%", "97%");

		initMyInfoPanel();

		initGroupInfoPanel();

		initChangePasswordPanel();

		initChatSettingsPanel();

		initPreferencesPanel();
	}

	private void initGroupInfoPanel() {
		HorizontalPanel groupInfoPanel = new HorizontalPanel();
		add(groupInfoPanel, ApplicationController.getMessages().DesktopUserInfoPanel_groupInfo(), false, HEADER_SIZE);
		groupInfoPanel.setWidth("100%");
		groupInfoPanel.setSpacing(5);

		groupInfo = new HTML("--");
		groupInfoPanel.add(groupInfo);
		groupInfoPanel.setCellHorizontalAlignment(groupInfo, HasHorizontalAlignment.ALIGN_LEFT);
		groupInfoPanel.setCellVerticalAlignment(groupInfo, HasVerticalAlignment.ALIGN_MIDDLE);

		HTML refreshGroupInfo = new HTML("<u style='cursor:pointer;'>"
				+ ApplicationController.getMessages().DesktopUserInfoPanel_refresh() + "</u>&nbsp;&nbsp;");
		groupInfoPanel.add(refreshGroupInfo);
		groupInfoPanel.setCellHorizontalAlignment(refreshGroupInfo, HasHorizontalAlignment.ALIGN_RIGHT);
		groupInfoPanel.setCellVerticalAlignment(refreshGroupInfo, HasVerticalAlignment.ALIGN_TOP);

		refreshGroupInfo.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				refreshQueueSize();
			}
		});
	}

	public void refreshQueueSize() {
		presenter.processGroupInfoRefresh();
	}

	@Override
	public void setGroupInfo(GroupInfo[] groups) {
		groupInfo.setHTML(formatGroupInfo(groups));
	}

	private String formatGroupInfo(GroupInfo[] groups) {
		StringBuffer buffer = new StringBuffer(
				ApplicationController.getMessages().DesktopUserInfoPanel_updateTime() + ": ");

		buffer.append(
				DateTimeFormat.getFormat(ClientProperties.getInstance().getStringValue(ClientProperties.TIME_FORMAT,
						ClientProperties.DEFAULT_TIME_FORMAT)).format(new Date()));
		buffer.append("<br><blockquote>");

		for (GroupInfo group : groups) {
			buffer.append(ApplicationController.getMessages().DesktopUserInfoPanel_group() + ": ");
			buffer.append(group.getGroupName());

			buffer.append("<blockquote>");

			Date pausedUntil = new Date(group.getPausedUntil());
			if (pausedUntil.after(new Date())) {
				buffer.append(ApplicationController.getMessages().DesktopUserInfoPanel_queuePausedUntil() + ": ");
				buffer.append(
						DateTimeFormat
								.getFormat(ClientProperties.getInstance().getStringValue(
										ClientProperties.DATE_TIME_FORMAT, ClientProperties.DEFAULT_DATE_TIME_FORMAT))
						.format(pausedUntil));
				buffer.append("<br>");
			}

			buffer.append(ApplicationController.getMessages().DesktopUserInfoPanel_queueFull() + ": ");
			boolean queueFull = group.isAllOperatorsBusy();
			if (queueFull) {
				buffer.append(ApplicationController.getMessages().DesktopConfirmationDialog_yes());
			} else {
				buffer.append(ApplicationController.getMessages().DesktopConfirmationDialog_no());
			}
			buffer.append("<br>");

			buffer.append(ApplicationController.getMessages().DesktopUserInfoPanel_queueSize() + ": ");
			buffer.append(group.getQueueSize());
			buffer.append("<br>");

			buffer.append(ApplicationController.getMessages().DesktopUserInfoPanel_numOperators() + ": ");
			buffer.append(group.getNumOperators());
			buffer.append("<br>");

			buffer.append(ApplicationController.getMessages().DesktopUserInfoPanel_numOperatorsWithDND() + ": ");
			buffer.append(group.getNumDND());
			buffer.append("<br>");

			buffer.append(ApplicationController.getMessages().DesktopUserInfoPanel_maxWaitTime() + ": ");
			buffer.append(group.getWaitTime());

			buffer.append("</blockquote>");
		}

		buffer.append("</blockquote>");
		return buffer.toString();
	}

	private void initMyInfoPanel() {
		HorizontalPanel userInfoPanel = new HorizontalPanel();
		userInfoPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		userInfoPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		userInfoPanel.setSpacing(5);

		add(userInfoPanel, ApplicationController.getMessages().DesktopUserInfoPanel_aboutMe(), false, HEADER_SIZE);
		userInfoPanel.setWidth("100%");

		avatarImage = new Image(Images.USER_MEDIUM);
		userInfoPanel.add(avatarImage);

		userInfo = new HTML(ApplicationController.getMessages().DesktopUserInfoPanel_userInfo());
		userInfoPanel.add(userInfo);
		userInfo.setWidth("");
		userInfoPanel.setCellHorizontalAlignment(userInfo, HasHorizontalAlignment.ALIGN_LEFT);
		userInfoPanel.setCellVerticalAlignment(userInfo, HasVerticalAlignment.ALIGN_TOP);

		HTML logoutLink = new HTML("<u style='cursor:pointer;'>"
				+ ApplicationController.getMessages().DesktopUserInfoPanel_logout() + "</u>&nbsp;&nbsp;");

		userInfoPanel.add(logoutLink);
		userInfoPanel.setCellHorizontalAlignment(logoutLink, HasHorizontalAlignment.ALIGN_RIGHT);
		userInfoPanel.setCellVerticalAlignment(logoutLink, HasVerticalAlignment.ALIGN_TOP);
		logoutLink.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.logOut();
			}
		});
	}

	private void initChangePasswordPanel() {
		FlowPanel changePasswordPanel = new FlowPanel();
		add(changePasswordPanel, ApplicationController.getMessages().DesktopUserInfoPanel_changePassword(), false,
				HEADER_SIZE);
		changePasswordPanel.setWidth("100%");

		Label oldPasswordLabel = new Label(ApplicationController.getMessages().DesktopUserInfoPanel_oldPassword());
		changePasswordPanel.add(oldPasswordLabel);
		oldPasswordLabel.setWidth("100%");

		oldPasswordTextBox = new PasswordTextBox();
		oldPasswordTextBox.setMaxLength(50);
		changePasswordPanel.add(oldPasswordTextBox);
		changePasswordPanel.add(new HTML("<p/>"));

		Label newPasswordLabel = new Label(ApplicationController.getMessages().DesktopUserInfoPanel_newPassword());
		changePasswordPanel.add(newPasswordLabel);
		newPasswordLabel.setWidth("100%");

		passwordTextBox = new PasswordTextBox();
		passwordTextBox.setMaxLength(50);
		changePasswordPanel.add(passwordTextBox);
		changePasswordPanel.add(new HTML("<p/>"));

		Label verifyPasswordLabel = new Label(
				ApplicationController.getMessages().DesktopUserInfoPanel_verifyPassword());
		changePasswordPanel.add(verifyPasswordLabel);
		verifyPasswordLabel.setWidth("100%");

		verifyPasswordTextBox = new PasswordTextBox();
		verifyPasswordTextBox.setMaxLength(50);
		changePasswordPanel.add(verifyPasswordTextBox);
		changePasswordPanel.add(new HTML("<p/>"));

		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.setSpacing(5);
		buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		changePasswordPanel.add(buttonPanel);

		Button btnChange = new Button(ApplicationController.getMessages().DesktopUserInfoPanel_change());
		buttonPanel.add(btnChange);
		btnChange.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.processPasswordChange(oldPasswordTextBox.getText().trim(), passwordTextBox.getText().trim(),
						verifyPasswordTextBox.getText().trim());
			}
		});

		Button btnReset = new Button(ApplicationController.getMessages().DesktopUserInfoPanel_reset());
		buttonPanel.add(btnReset);
		btnReset.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				reset();
			}
		});
	}

	private void initChatSettingsPanel() {
		FlowPanel chatSettingsPanel = new FlowPanel();
		add(chatSettingsPanel, ApplicationController.getMessages().DesktopUserInfoPanel_chatSettings(), false,
				HEADER_SIZE);
		chatSettingsPanel.setWidth("100%");

		dndCheckBox = new CheckBox(ApplicationController.getMessages().DesktopUserInfoPanel_doNotDisturb());
		chatSettingsPanel.add(dndCheckBox);
		dndCheckBox.setWidth("100%");
		dndCheckBox.addClickHandler(clickHandler);
		chatSettingsPanel.add(new HTML("<p/>"));

		autoAnswerCheckBox = new CheckBox(ApplicationController.getMessages().DesktopUserInfoPanel_autoAnswerLabel());
		autoAnswerCheckBox.setFormValue(ApplicationController.getMessages().DesktopUserInfoPanel_autoAnswer());
		chatSettingsPanel.add(autoAnswerCheckBox);
		autoAnswerCheckBox.setWidth("100%");
		autoAnswerCheckBox.addClickHandler(clickHandler);
	}

	private void initPreferencesPanel() {
		FlowPanel preferencesPanel = new FlowPanel();
		add(preferencesPanel, ApplicationController.getMessages().DesktopUserInfoPanel_preferences(), false,
				HEADER_SIZE);
		preferencesPanel.setWidth("100%");

		HTML displaySettingsLabel = new HTML(
				"<p><b>" + ApplicationController.getMessages().DesktopUserInfoPanel_displaySettings() + "</b></p>");
		displaySettingsLabel.setWidth("100%");
		preferencesPanel.add(displaySettingsLabel);

		Label themeLabel = new Label(ApplicationController.getMessages().DesktopUserInfoPanel_theme());
		preferencesPanel.add(themeLabel);

		themeListBox = new ListBox();
		themeListBox.setVisibleItemCount(1);

		List<String> themes = ThemeFactory.listThemes();
		int currentTheme = -1;
		int index = 0;
		for (String theme : themes) {
			themeListBox.addItem(theme);
			if (theme.equals(ThemeFactory.getSelectedTheme())) {
				currentTheme = index;
			}

			index++;
		}

		if (currentTheme >= 0) {
			themeListBox.setSelectedIndex(currentTheme);
		}

		preferencesPanel.add(themeListBox);

		themeListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (themeListBox.getSelectedIndex() >= 0) {
					String theme = themeListBox.getItemText(themeListBox.getSelectedIndex());
					ThemeFactory.initTheme(theme);
				}
			}
		});

		HTML audioSettingsLabel = new HTML(
				"<p><b>" + ApplicationController.getMessages().DesktopUserInfoPanel_audioSettings() + "</b></p>");
		audioSettingsLabel.setWidth("100%");
		preferencesPanel.add(audioSettingsLabel);

		buzzCheckBox = new CheckBox(ApplicationController.getMessages().DesktopUserInfoPanel_buzz());
		preferencesPanel.add(buzzCheckBox);
		buzzCheckBox.setWidth("100%");
		preferencesPanel.add(new HTML("<p/>"));
		buzzCheckBox.addClickHandler(clickHandler);

		chimeCheckBox = new CheckBox(ApplicationController.getMessages().DesktopUserInfoPanel_messageNotification());
		preferencesPanel.add(chimeCheckBox);
		chimeCheckBox.setWidth("100%");
		preferencesPanel.add(new HTML("<p/>"));
		chimeCheckBox.addClickHandler(clickHandler);

		ringingCheckBox = new CheckBox(ApplicationController.getMessages().DesktopUserInfoPanel_newChatNotification());
		preferencesPanel.add(ringingCheckBox);
		ringingCheckBox.setWidth("100%");
		preferencesPanel.add(new HTML("<p/>"));
		ringingCheckBox.addClickHandler(clickHandler);

		presenceCheckBox = new CheckBox(
				ApplicationController.getMessages().DesktopUserInfoPanel_contactsNotification());
		preferencesPanel.add(presenceCheckBox);
		presenceCheckBox.setWidth("100%");
		preferencesPanel.add(new HTML("<p/>"));
		presenceCheckBox.addClickHandler(clickHandler);
	}

	@Override
	public void setPresenter(UserInfoPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setValues(CallPartyElement userInfo, AudioSettings audioSettings, ChatSettings chatSettings) {
		this.userInfo.setHTML(ViewUtils.formatUserInfo(userInfo));

		if (userInfo.getAvatar() != null) {
			avatarImage.setUrl(userInfo.getAvatar());
			avatarImage.setSize(Images.MEDIUM_IMG_WIDTH, Images.MEDIUM_IMG_HEIGHT);
		}

		dndCheckBox.setValue(chatSettings.isDnd());
		autoAnswerCheckBox.setValue(chatSettings.isAutoAnswer());

		buzzCheckBox.setValue(audioSettings.isBuzz());
		chimeCheckBox.setValue(audioSettings.isChime());
		presenceCheckBox.setValue(audioSettings.isPresence());
		ringingCheckBox.setValue(audioSettings.isRinging());
	}

	@Override
	public void reset() {
		passwordTextBox.setText("");
		oldPasswordTextBox.setText("");
		verifyPasswordTextBox.setText("");
	}

	private void showItem(int index) {
		if (!navigationLocked) {
			super.showWidget(index);
		}
	}

	@Override
	public void showWidget(Widget child) {
		if (!navigationLocked) {
			super.showWidget(child);
		}
	}

	@Override
	public void showChangePassword() {
		super.showWidget(CHANGE_PASSWORD_PANEL_INDEX);
	}

	@Override
	public void lockEnabled(boolean enabled) {
		navigationLocked = enabled;
	}

	@Override
	public void showMyInfo() {
		showItem(0);
	}
}
