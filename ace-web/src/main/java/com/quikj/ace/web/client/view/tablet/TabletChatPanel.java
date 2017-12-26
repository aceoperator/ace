/**
 * 
 */
package com.quikj.ace.web.client.view.tablet;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.ace.messages.vo.talk.DisconnectReasonElement;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.presenter.ChatSessionPresenter;
import com.quikj.ace.web.client.view.ChatPanel;
import com.quikj.ace.web.client.view.EmoticonUtils;
import com.quikj.ace.web.client.view.ViewUtils;

/**
 * @author amit
 * 
 */
public class TabletChatPanel extends StackLayoutPanel implements ChatPanel {

	private static final double VISITOR_TITLE_SIZE = 30.0;
	private static final double TITLE_SIZE = 45.0;
	private static final int DATA_ENTRY_PANEL_SIZE = 100;

	private HTMLPanel transcriptPanel;
	private TextArea chatEditTextArea;
	private ChatSessionPresenter presenter;
	private Button discButton;
	private Button sendButton;
	private ListBox cannedMessageListBox;
	private ScrollPanel transcriptScrollPanel;
	private boolean adjusted = false;
	private HorizontalPanel toolsPanel;
	private VerticalPanel dataEntryPanel;
	private VerticalPanel chatPanel;
	private boolean operator;
	private FlexTable chatInfoDetailTable;
	private HTML chatPanelHeaderLabel;
	private ListBox contactList;
	private HorizontalPanel chatPanelHeaderControls;
	private Button conferenceButton;
	private Button transferButton;
	private String me;

	private Widget typing;
	private String typingFrom;

	private List<HandlerRegistration> eventHandlers = new ArrayList<HandlerRegistration>();

	private HTML controlSeparator;
	private boolean hideAvatar;

	public TabletChatPanel() {
		super(Unit.PX);
		setSize("100%", "100%");
	}

	private void init(String me, CallPartyElement otherParty,
			CannedMessageElement[] cannedMessages, boolean operator,
			boolean showOtherPartyInfo, boolean hideAvatar) {
		this.operator = operator;
		this.me = me;
		this.hideAvatar = hideAvatar;

		initTranscriptArea(otherParty, operator);

		initChatEditArea(cannedMessages);

		if (showOtherPartyInfo) {
			initChatInfoArea(otherParty);
		}
	}

	private void initChatInfoArea(CallPartyElement otherParty) {
		chatInfoDetailTable = new FlexTable();
		add(chatInfoDetailTable, ApplicationController.getMessages()
				.DesktopChatPanel_chatSessionInformation(), false,
				VISITOR_TITLE_SIZE);
		chatInfoDetailTable.setWidth("100%");
		chatInfoDetailTable.setCellPadding(10);

		setOtherPartyDetails(0, otherParty);
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
				.getMessages().DesktopChatPanel_private() : formatOtherParties(
				ViewUtils.formatName(otherParty), otherParty.getAvatar())
				.toString());

		chatPanelHeaderControls = new HorizontalPanel();
		chatPanelHeader.add(chatPanelHeaderControls);
		chatPanelHeaderControls.setSpacing(4);
		chatPanelHeader.setCellHorizontalAlignment(chatPanelHeaderControls,
				HasHorizontalAlignment.ALIGN_RIGHT);
		chatPanelHeader.setCellVerticalAlignment(chatPanelHeaderControls,
				HasVerticalAlignment.ALIGN_MIDDLE);

		if (operator) {
			contactList = new ListBox();
			chatPanelHeaderControls.add(contactList);
			contactList.setEnabled(false);

			contactList.addItem(ApplicationController.getMessages()
					.DesktopChatPanel_selectContact());
			contactList.setSelectedIndex(0);

			chatPanelHeaderControls.setCellHorizontalAlignment(contactList,
					HasHorizontalAlignment.ALIGN_RIGHT);
			chatPanelHeaderControls.setCellVerticalAlignment(contactList,
					HasVerticalAlignment.ALIGN_MIDDLE);

			contactList.addTouchStartHandler(new TouchStartHandler() {

				@Override
				public void onTouchStart(TouchStartEvent event) {

					contactList.clear();
					contactList.addItem(ApplicationController.getMessages()
							.DesktopChatPanel_selectContact());
					List<String> contacts = presenter.getContactsList();
					for (String contact : contacts) {
						contactList.addItem(contact);
					}
					contactList.setSelectedIndex(0);
				}
			});

			conferenceButton = new Button(ApplicationController.getMessages()
					.DesktopChatPanel_add());
			chatPanelHeaderControls.add(conferenceButton);
			chatPanelHeaderControls.setCellHorizontalAlignment(
					conferenceButton, HasHorizontalAlignment.ALIGN_LEFT);
			chatPanelHeaderControls.setCellVerticalAlignment(conferenceButton,
					HasVerticalAlignment.ALIGN_MIDDLE);
			conferenceButton.setEnabled(false);
			conferenceButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					int index = contactList.getSelectedIndex();
					if (index <= 0) {
						return;
					}

					presenter.addToConference(contactList.getItemText(index));
					contactList.setSelectedIndex(0);
				}
			});

			transferButton = new Button(ApplicationController.getMessages()
					.DesktopChatPanel_transfer());
			chatPanelHeaderControls.add(transferButton);
			chatPanelHeaderControls.setCellHorizontalAlignment(transferButton,
					HasHorizontalAlignment.ALIGN_LEFT);
			chatPanelHeaderControls.setCellVerticalAlignment(transferButton,
					HasVerticalAlignment.ALIGN_MIDDLE);
			transferButton.setEnabled(false);
			transferButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					int index = contactList.getSelectedIndex();
					if (index <= 0) {
						return;
					}

					presenter.transferTo(contactList.getItemText(index));
					contactList.setSelectedIndex(0);
				}
			});

			controlSeparator = new HTML("&nbsp;|&nbsp");
			chatPanelHeaderControls.add(controlSeparator);
		}

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
		transcriptScrollPanel.setWidth("98%");

		transcriptPanel = new HTMLPanel("");
		transcriptScrollPanel.setWidget(transcriptPanel);
		transcriptPanel.setSize("100%", "100%");

		if (ClientProperties.getInstance().getBooleanValue(
				ClientProperties.SUPPRESS_COPY_PASTE,
				ChatPanel.DEFAULT_SUPPRESS_COPYPASTE)) {

			eventHandlers.add(RootPanel.get().addDomHandler(
					new ContextMenuHandler() {

						@Override
						public void onContextMenu(ContextMenuEvent event) {
							event.preventDefault();
							event.stopPropagation();
						}
					}, ContextMenuEvent.getType()));

			eventHandlers.add(RootPanel.get().addDomHandler(
					new KeyDownHandler() {

						@Override
						public void onKeyDown(KeyDownEvent event) {
							if (event.isControlKeyDown()) {
								event.preventDefault();
								event.stopPropagation();
							}
						}
					}, KeyDownEvent.getType()));
		}
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

		HorizontalPanel controlPanel = new HorizontalPanel();
		dataEntryPanel.add(controlPanel);
		controlPanel.setWidth("100%");

		if (operator) {
			toolsPanel = new HorizontalPanel();
			toolsPanel.setSpacing(5);
			controlPanel.add(toolsPanel);

			initCannedMessages(cannedMessages, toolsPanel);
		}

		HorizontalPanel editPanel = new HorizontalPanel();
		dataEntryPanel.add(editPanel);
		editPanel.setSize("100%", "100%");
		editPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		chatEditTextArea = new TextArea();
		editPanel.add(chatEditTextArea);
		chatEditTextArea.setWidth("100%");

		chatEditTextArea.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER
						&& !event.getNativeEvent().getShiftKey()) {
					processEnteredText();
				} else {
					presenter.typing();
				}
			}
		});

		chatEditTextArea.setEnabled(false);
		editPanel.add(new HTML("&nbsp;"));

		sendButton = new Button(ApplicationController.getMessages()
				.DesktopChatPanel_send());
		editPanel.add(sendButton);
		editPanel.setCellHorizontalAlignment(sendButton,
				HasHorizontalAlignment.ALIGN_LEFT);
		sendButton.setSize("100%", "100%");
		sendButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				processEnteredText();
				chatEditTextArea.setFocus(true);
			}
		});

		sendButton.setEnabled(false);
	}

	private void initCannedMessages(
			CannedMessageElement[] cannedMessages,
			HorizontalPanel commandButtonsPanel) {
		if (cannedMessages != null && cannedMessages.length > 0) {
			cannedMessageListBox = new ListBox();
			commandButtonsPanel.add(cannedMessageListBox);
			cannedMessageListBox.setVisibleItemCount(1);
			cannedMessageListBox.setWidth("200px");

			cannedMessageListBox.addItem("");
			for (int i = 0; i < cannedMessages.length; i++) {
				cannedMessageListBox
						.addItem(cannedMessages[i].getDescription());
			}

			commandButtonsPanel.setCellHorizontalAlignment(
					cannedMessageListBox, HasHorizontalAlignment.ALIGN_RIGHT);
			commandButtonsPanel.setCellVerticalAlignment(cannedMessageListBox,
					HasVerticalAlignment.ALIGN_MIDDLE);

			cannedMessageListBox.addChangeHandler(new ChangeHandler() {

				@Override
				public void onChange(ChangeEvent event) {
					if (cannedMessageListBox.getSelectedIndex() > 0) {
						presenter.cannedMessageSelected(cannedMessageListBox
								.getSelectedIndex() - 1);
						cannedMessageListBox.setSelectedIndex(-1);
					}
				}
			});
		}
	}

	@Override
	public void appendToConveration(String from, long timeStamp, Object obj) {

		if (!adjusted) {
			adjustScrollHeight();
		}

		boolean userTyping = false;
		if (typing != null) {
			userTyping = true;
			transcriptPanel.remove(typing);
		}

		Widget html = ChatPanel.Util.formatChat(
				from,
				timeStamp,
				(String)obj,
				me,
				ClientProperties.getInstance().getBooleanValue(
						ClientProperties.CONVERSATION_SMALL_SPACE, false));
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

		chatEditTextArea.removeFromParent();
		sendButton.removeFromParent();

		if (operator) {
			if (cannedMessageListBox != null) {
				cannedMessageListBox.removeFromParent();
			}

			controlSeparator.removeFromParent();
			contactList.removeFromParent();
			conferenceButton.removeFromParent();
			transferButton.removeFromParent();
		}

		if (toolsPanel != null) {
			toolsPanel.removeFromParent();
		}

		dataEntryPanel.setHeight(discButton.getOffsetHeight() + 20 + "px");

		adjustScrollHeight();
	}

	private void processEnteredText() {
		String text = chatEditTextArea.getText().trim();
		chatEditTextArea.setText("");
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
		int pheight = getOffsetHeight();
		int dheight = dataEntryPanel.getOffsetHeight();
		int sessionInfoHeaderSize = chatInfoDetailTable != null ? (int) VISITOR_TITLE_SIZE
				: 0;

		int height = pheight - dheight - sessionInfoHeaderSize - 30;
		if (operator) {
			height -= 15;
		}
		if (height >= 0) {
			transcriptScrollPanel.setHeight(height + "px");
			adjusted = true;
		}

		transcriptScrollPanel.scrollToBottom();
	}

	@Override
	public void attach(String me, CallPartyElement otherParty,
			CannedMessageElement[] cannedMessages, boolean operator,
			boolean showOtherPartyInfo, boolean hideAvatar) {
		init(me, otherParty, cannedMessages, operator, showOtherPartyInfo,
				hideAvatar);
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
		chatEditTextArea.setEnabled(enabled);
		sendButton.setEnabled(enabled);

		if (operator) {
			transferButton.setEnabled(enabled);
			conferenceButton.setEnabled(enabled);
			contactList.setEnabled(enabled);
		}
	}

	@Override
	public void setOtherPartyInfo(List<CallPartyElement> otherParties) {

		if (chatInfoDetailTable != null) {
			chatInfoDetailTable.clear();
			int row = 0;
			for (CallPartyElement cp : otherParties) {
				setOtherPartyDetails(row, cp);
				row++;
			}
		}

		String parties;
		String avatar = null;
		if (otherParties.size() == 1) {
			CallPartyElement userInfo = otherParties.get(0);
			parties = ViewUtils.formatName(userInfo);
			avatar = userInfo.getAvatar();
		} else {
			parties = ApplicationController.getMessages()
					.DesktopChatPanel_numUsersInConference(
							otherParties.size() + 1 + "");
		}

		StringBuilder builder = formatOtherParties(parties, avatar);

		chatPanelHeaderLabel.setHTML(builder.toString());
		chatPanelHeaderLabel.setStyleName("gwt-StackLayoutPanelHeader");
		chatPanelHeaderLabel.setWidth("100%");
	}

	private StringBuilder formatOtherParties(String parties, String avatar) {
		StringBuilder builder = new StringBuilder();
		if (avatar != null && !hideAvatar) {
			builder.append("<img src='");
			builder.append(avatar);
			builder.append("' width='");
			builder.append(Images.TINY_IMG_WIDTH);
			builder.append("' + height='");
			builder.append(Images.TINY_IMG_HEIGHT);
			builder.append("' border='0' align='center'>&nbsp;");
		}

		builder.append(parties);
		return builder;
	}

	private void setOtherPartyDetails(int row, CallPartyElement cp) {
		String image;
		if (cp.getAvatar() != null) {
			image = cp.getAvatar();
		} else {
			image = Images.USER_MEDIUM;
		}

		Image img = new Image(image);
		chatInfoDetailTable.setWidget(row, 0, img);
		img.setSize(Images.MEDIUM_IMG_WIDTH, Images.MEDIUM_IMG_WIDTH);

		String userInfo = ViewUtils.formatUserInfo(cp);
		chatInfoDetailTable.setWidget(row, 1, new HTML(userInfo));
	}

	@Override
	public void transferSetEnabled(boolean enabled) {
		if (transferButton != null) {
			transferButton.setEnabled(enabled);
		}
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
			typing = ChatPanel.Util.formatChat(
					from,
					timestamp,
					ChatPanel.TYPING,
					me,
					ClientProperties.getInstance().getBooleanValue(
							ClientProperties.CONVERSATION_SMALL_SPACE, false));
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
}
