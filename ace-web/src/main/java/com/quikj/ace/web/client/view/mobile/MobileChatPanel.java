/**
 * 
 */
package com.quikj.ace.web.client.view.mobile;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.ace.messages.vo.talk.DisconnectReasonElement;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.presenter.ChatSessionPresenter;
import com.quikj.ace.web.client.view.ChatPanel;
import com.quikj.ace.web.client.view.EmoticonUtils;
import com.quikj.ace.web.client.view.ViewUtils;

/**
 * @author amit
 * 
 */
public class MobileChatPanel extends StackLayoutPanel implements ChatPanel {

	private static final double TITLE_SIZE = 45.0;
	private static final int DATA_ENTRY_PANEL_SIZE = 50;

	private HTMLPanel transcriptPanel;
	private TextBox chatEditTextBox;
	private ChatSessionPresenter presenter;
	private Button discButton;
	private Button sendButton;
	private ScrollPanel transcriptScrollPanel;
	private boolean adjusted = false;
	private VerticalPanel dataEntryPanel;
	private VerticalPanel chatPanel;
	private boolean operator;
	private HTML chatPanelHeaderLabel;
	private HorizontalPanel chatPanelHeaderControls;
	private String me;

	private Widget typing;
	private String typingFrom;

	private List<HandlerRegistration> eventHandlers = new ArrayList<HandlerRegistration>();

	public MobileChatPanel() {
		super(Unit.PX);
		setSize("100%", "99%");
	}

	public void init(String me, CallPartyElement otherParty,
			CannedMessageElement[] cannedMessages, boolean operator,
			boolean showOtherPartyInfo) {

		this.operator = operator;
		this.me = me;

		initTranscriptArea(otherParty, operator);

		initChatEditArea(cannedMessages);
	}

	private void initTranscriptArea(CallPartyElement otherParty,
			boolean operator) {
		chatPanel = new VerticalPanel();
		chatPanel.setSize("100%", "100%");

		HorizontalPanel chatPanelHeader = new HorizontalPanel();
		add(chatPanel, chatPanelHeader, TITLE_SIZE);

		chatPanelHeader.setWidth("100%");
		chatPanelHeader.setSpacing(2);

		chatPanelHeaderLabel = new HTML();
		chatPanelHeaderLabel.setStyleName("gwt-StackLayoutPanelHeader");
		chatPanelHeaderLabel.setWidth("100%");

		chatPanelHeader.add(chatPanelHeaderLabel);
		chatPanelHeader.setCellHorizontalAlignment(chatPanelHeaderLabel,
				HasHorizontalAlignment.ALIGN_LEFT);
		chatPanelHeader.setCellVerticalAlignment(chatPanelHeaderLabel,
				HasVerticalAlignment.ALIGN_MIDDLE);

		chatPanelHeaderLabel.setHTML(otherParty == null ? ApplicationController
				.getMessages().DesktopChatPanel_private() : ViewUtils
				.formatName(otherParty));

		chatPanelHeaderControls = new HorizontalPanel();
		chatPanelHeader.add(chatPanelHeaderControls);
		chatPanelHeaderControls.setSpacing(4);
		chatPanelHeader.setCellHorizontalAlignment(chatPanelHeaderControls,
				HasHorizontalAlignment.ALIGN_RIGHT);
		chatPanelHeader.setCellVerticalAlignment(chatPanelHeaderControls,
				HasVerticalAlignment.ALIGN_MIDDLE);

		discButton = new Button(ApplicationController.getMessages()
				.DesktopChatPanel_disconnect());
		chatPanelHeaderControls.add(discButton);
		chatPanelHeaderControls.setCellHorizontalAlignment(discButton,
				HasHorizontalAlignment.ALIGN_LEFT);
		chatPanelHeaderControls.setCellVerticalAlignment(discButton,
				HasVerticalAlignment.ALIGN_MIDDLE);

		discButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.userDisconnected(
						DisconnectReasonElement.NORMAL_DISCONNECT, null);
			}
		});

		if (operator) {
			Button closeButton = new Button(ApplicationController.getMessages()
					.DesktopChatPanel_close());
			chatPanelHeaderControls.add(closeButton);
			chatPanelHeaderControls.setCellHorizontalAlignment(closeButton,
					HasHorizontalAlignment.ALIGN_LEFT);
			chatPanelHeaderControls.setCellVerticalAlignment(closeButton,
					HasVerticalAlignment.ALIGN_MIDDLE);

			closeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.chatClosed();
				}
			});
		}

		transcriptScrollPanel = new ScrollPanel();
		transcriptScrollPanel.setTouchScrollingDisabled(false);
		transcriptScrollPanel.setAlwaysShowScrollBars(true);
		chatPanel.add(transcriptScrollPanel);

		transcriptPanel = new HTMLPanel("");
		transcriptScrollPanel.setWidget(transcriptPanel);
		transcriptPanel.setSize("100%", "100%");
	}

	@Override
	public void dispose() {
		for (HandlerRegistration handler : eventHandlers) {
			handler.removeHandler();
		}

		eventHandlers.clear();
	}

	private void initChatEditArea(CannedMessageElement[] cannedMessages) {
		dataEntryPanel = new VerticalPanel();
		dataEntryPanel.setSpacing(3);

		chatPanel.add(dataEntryPanel);
		chatPanel.setCellVerticalAlignment(dataEntryPanel,
				HasVerticalAlignment.ALIGN_BOTTOM);
		dataEntryPanel.setWidth("100%");
		dataEntryPanel.setHeight(DATA_ENTRY_PANEL_SIZE + "px");

		HorizontalPanel editPanel = new HorizontalPanel();
		dataEntryPanel.add(editPanel);
		editPanel.setSize("100%", "100%");
		editPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		dataEntryPanel.setCellVerticalAlignment(editPanel,
				HasVerticalAlignment.ALIGN_TOP);

		chatEditTextBox = new TextBox();
		editPanel.add(chatEditTextBox);
		chatEditTextBox.setWidth("100%");

		chatEditTextBox.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					processTypedText();
				} else {
					presenter.typing();
				}
			}
		});

		chatEditTextBox.setEnabled(false);
		editPanel.add(new HTML("&nbsp;&nbsp;"));

		sendButton = new Button(ApplicationController.getMessages()
				.DesktopChatPanel_send());
		editPanel.add(sendButton);
		editPanel.setCellHorizontalAlignment(sendButton,
				HasHorizontalAlignment.ALIGN_LEFT);
		sendButton.setWidth("100%");
		sendButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				processTypedText();
				chatEditTextBox.setFocus(true);
			}
		});

		sendButton.setEnabled(false);
	}

	@Override
	public void appendToConveration(String from, long timeStamp, String message) {

		if (!adjusted) {
			adjustScrollHeight();
		}

		boolean userTyping = false;
		if (typing != null) {
			userTyping = true;
			transcriptPanel.remove(typing);
		}

		Widget html = ChatPanel.Util.formatChat(from, timeStamp, message, me,
				true);
		transcriptPanel.add(html);

		if (userTyping) {
			transcriptPanel.add(typing);
		}

		transcriptScrollPanel.setVerticalScrollPosition(transcriptPanel
				.getOffsetHeight());
	}

	@Override
	public void setPresenter(ChatSessionPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public ChatSessionPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void makeReadOnly() {
		discButton.removeFromParent();
		dataEntryPanel.removeFromParent();

		adjustScrollHeight();
	}

	private void processTypedText() {
		String text = chatEditTextBox.getText().trim();
		chatEditTextBox.setText("");
		if (text.length() == 0) {
			return;
		}

		text = EmoticonUtils.replaceEmoticonText(text);

		appendToConveration(null, ApplicationController.getInstance()
				.timestamp(), text);
		presenter.sendTextMessage(text);
	}

	@Override
	public void emoticonSelected(String url) {
	}

	@Override
	public void onResize() {
		super.onResize();
		adjustScrollHeight();
	}

	private void adjustScrollHeight() {
		if (chatPanel.getWidgetCount() > 1) {
			int pheight = getOffsetHeight();
			int dheight = dataEntryPanel.getOffsetHeight();

			int height = pheight - dheight - 30;

			if (operator) {
				height -= 15;
			}

			if (height >= 0) {
				transcriptScrollPanel.setHeight(height + "px");
				adjusted = true;
			}
		} else {
			transcriptScrollPanel.setSize("100%", "100%");
			transcriptPanel.setSize("100%", "100%");
		}

		transcriptScrollPanel.scrollToBottom();
	}

	@Override
	public void attach(String me, CallPartyElement otherParty,
			CannedMessageElement[] cannedMessages, boolean operator,
			boolean showOtherPartyInfo, boolean hideAvatar) {
		init(me, otherParty, cannedMessages, operator, showOtherPartyInfo);
	}

	@Override
	public String getTranscript() {
		return transcriptPanel.toString();
	}

	@Override
	public void chatEnabled() {
		chatEditingSetEnabled(true);
	}

	@Override
	public void chatDisabled() {
		chatEditingSetEnabled(false);
	}

	private void chatEditingSetEnabled(boolean enabled) {
		chatEditTextBox.setEnabled(enabled);
		sendButton.setEnabled(enabled);
	}

	@Override
	public void setOtherPartyInfo(List<CallPartyElement> otherParties) {
		String parties;
		if (otherParties.size() == 1) {
			CallPartyElement userInfo = otherParties.get(0);
			parties = ViewUtils.formatName(userInfo);
		} else {
			parties = ApplicationController.getMessages()
					.DesktopChatPanel_numUsersInConference(
							otherParties.size() + 1 + "");
		}

		chatPanelHeaderLabel.setHTML(parties);
		chatPanelHeaderLabel.setStyleName("gwt-StackLayoutPanelHeader");
		chatPanelHeaderLabel.setWidth("100%");
	}

	@Override
	public void transferSetEnabled(boolean enabled) {
	}

	@Override
	public void showTyping(String from, long timestamp) {
		boolean render = false;
		if (typing == null) {
			render = true;
		} else if (!typingFrom.equals(from)) {
			hideTyping();
			render = true;
		}

		if (render) {
			typing = ChatPanel.Util.formatChat(from, timestamp,
					ChatPanel.TYPING, me, true);
			typingFrom = from;
			transcriptPanel.add(typing);
			transcriptScrollPanel.setVerticalScrollPosition(transcriptPanel
					.getOffsetHeight());
		}
	}

	@Override
	public void hideTyping() {
		if (typing != null) {
			transcriptPanel.remove(typing);
			typing = null;
			typingFrom = null;
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();

		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				adjustScrollHeight();
			}
		});
	}

	@Override
	public void appendToConveration(String from, long timeStamp, long formId, String formDef) {
		// TODO Auto-generated method stub
	}
}
