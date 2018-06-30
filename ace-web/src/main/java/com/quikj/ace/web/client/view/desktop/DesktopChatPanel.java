/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.ace.messages.vo.talk.DisconnectReasonElement;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.presenter.ChatSessionPresenter;
import com.quikj.ace.web.client.presenter.EmoticonPresenter;
import com.quikj.ace.web.client.presenter.MessageBoxPresenter;
import com.quikj.ace.web.client.view.ChatPanel;
import com.quikj.ace.web.client.view.EmoticonUtils;
import com.quikj.ace.web.client.view.FormRenderer;
import com.quikj.ace.web.client.view.FormRenderer.FormListener;
import com.quikj.ace.web.client.view.HtmlUtils;
import com.quikj.ace.web.client.view.ViewUtils;

/**
 * @author amit
 * 
 */
public class DesktopChatPanel extends StackLayoutPanel implements ChatPanel, FormListener {

	private static final double VISITOR_TITLE_SIZE = 30.0;
	private static final double TITLE_SIZE = 45.0;
	private static final int DATA_ENTRY_PANEL_SIZE = 100;

	private HTMLPanel transcriptPanel;
	private RichTextArea chatEditTextArea;
	private ChatSessionPresenter presenter;
	private Button discButton;
	private Button sendButton;
	private ListBox cannedMessageListBox;
	private ScrollPanel transcriptScrollPanel;
	private Button btnBold;
	private Button btnItalics;
	private Button btnUnderline;
	private HorizontalPanel commandPanel;
	private PopupPanel emoticonPallette;
	private boolean adjusted = false;
	private Button btnEmoticon;
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
	private FormPanel fileShareFormPanel;
	private FileUpload fileShare;
	private Hidden sessionField;
	private Button shareButton;

	private FormRenderer formRenderer = new FormRenderer();

	public DesktopChatPanel() {
		super(Unit.PX);
		setSize("100%", "97%");
	}

	private void init(String me, CallPartyElement otherParty, CannedMessageElement[] cannedMessages, boolean operator,
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
		add(chatInfoDetailTable, ApplicationController.getMessages().DesktopChatPanel_chatSessionInformation(), false,
				VISITOR_TITLE_SIZE);
		chatInfoDetailTable.setWidth("100%");
		chatInfoDetailTable.setCellPadding(10);

		setOtherPartyDetails(0, otherParty);
	}

	private void initTranscriptArea(CallPartyElement otherParty, boolean operator) {
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
		chatPanelHeader.setCellHorizontalAlignment(chatPanelHeaderLabel, HasHorizontalAlignment.ALIGN_LEFT);
		chatPanelHeader.setCellVerticalAlignment(chatPanelHeaderLabel, HasVerticalAlignment.ALIGN_MIDDLE);

		chatPanelHeaderLabel.setHTML(otherParty == null ? ApplicationController.getMessages().DesktopChatPanel_private()
				: formatOtherParties(ViewUtils.formatName(otherParty), otherParty.getAvatar()).toString());

		chatPanelHeaderControls = new HorizontalPanel();
		chatPanelHeader.add(chatPanelHeaderControls);
		chatPanelHeaderControls.setSpacing(4);
		chatPanelHeader.setCellHorizontalAlignment(chatPanelHeaderControls, HasHorizontalAlignment.ALIGN_RIGHT);
		chatPanelHeader.setCellVerticalAlignment(chatPanelHeaderControls, HasVerticalAlignment.ALIGN_MIDDLE);

		if (operator) {
			contactList = new ListBox();
			chatPanelHeaderControls.add(contactList);
			chatPanelHeaderControls.setCellHorizontalAlignment(contactList, HasHorizontalAlignment.ALIGN_RIGHT);
			chatPanelHeaderControls.setCellVerticalAlignment(contactList, HasVerticalAlignment.ALIGN_MIDDLE);
			contactList.setEnabled(false);

			contactList.addItem(ApplicationController.getMessages().DesktopChatPanel_selectContact());
			contactList.addFocusHandler(new FocusHandler() {

				@Override
				public void onFocus(FocusEvent event) {
					contactList.clear();
					contactList.addItem(ApplicationController.getMessages().DesktopChatPanel_selectContact());
					List<String> contacts = presenter.getContactsList();
					for (String contact : contacts) {
						contactList.addItem(contact);
					}

				}
			});

			conferenceButton = new Button(ApplicationController.getMessages().DesktopChatPanel_add());
			chatPanelHeaderControls.add(conferenceButton);
			chatPanelHeaderControls.setCellHorizontalAlignment(conferenceButton, HasHorizontalAlignment.ALIGN_LEFT);
			chatPanelHeaderControls.setCellVerticalAlignment(conferenceButton, HasVerticalAlignment.ALIGN_MIDDLE);
			conferenceButton.setEnabled(false);
			conferenceButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					int index = contactList.getSelectedIndex();
					if (index == 0) {
						return;
					}

					presenter.addToConference(contactList.getItemText(index));
					contactList.setSelectedIndex(0);
				}
			});

			transferButton = new Button(ApplicationController.getMessages().DesktopChatPanel_transfer());
			chatPanelHeaderControls.add(transferButton);
			chatPanelHeaderControls.setCellHorizontalAlignment(transferButton, HasHorizontalAlignment.ALIGN_LEFT);
			chatPanelHeaderControls.setCellVerticalAlignment(transferButton, HasVerticalAlignment.ALIGN_MIDDLE);
			transferButton.setEnabled(false);
			transferButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					int index = contactList.getSelectedIndex();
					if (index == 0) {
						return;
					}

					presenter.transferTo(contactList.getItemText(index));
					contactList.setSelectedIndex(0);
				}
			});

			controlSeparator = new HTML("&nbsp;|&nbsp");
			chatPanelHeaderControls.add(controlSeparator);
		}

		discButton = new Button(ApplicationController.getMessages().DesktopChatPanel_disconnect());
		chatPanelHeaderControls.add(discButton);
		chatPanelHeaderControls.setCellHorizontalAlignment(discButton, HasHorizontalAlignment.ALIGN_LEFT);
		chatPanelHeaderControls.setCellVerticalAlignment(discButton, HasVerticalAlignment.ALIGN_MIDDLE);

		discButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.userDisconnected(DisconnectReasonElement.NORMAL_DISCONNECT, null);
			}
		});

		if (operator) {
			Button closeButton = new Button(ApplicationController.getMessages().DesktopChatPanel_close());
			chatPanelHeaderControls.add(closeButton);
			chatPanelHeaderControls.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_LEFT);
			chatPanelHeaderControls.setCellVerticalAlignment(closeButton, HasVerticalAlignment.ALIGN_MIDDLE);

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

		if (ClientProperties.getInstance().getBooleanValue(ClientProperties.SUPPRESS_COPY_PASTE,
				ChatPanel.DEFAULT_SUPPRESS_COPYPASTE)) {

			eventHandlers.add(RootPanel.get().addDomHandler(new ContextMenuHandler() {

				@Override
				public void onContextMenu(ContextMenuEvent event) {
					event.preventDefault();
					event.stopPropagation();
				}
			}, ContextMenuEvent.getType()));

			eventHandlers.add(RootPanel.get().addDomHandler(new KeyDownHandler() {

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
		chatPanel.setCellVerticalAlignment(dataEntryPanel, HasVerticalAlignment.ALIGN_BOTTOM);
		dataEntryPanel.setWidth("100%");
		dataEntryPanel.setHeight(DATA_ENTRY_PANEL_SIZE + "px");

		HorizontalPanel controlPanel = new HorizontalPanel();
		dataEntryPanel.add(controlPanel);
		controlPanel.setWidth("100%");

		commandPanel = new HorizontalPanel();
		commandPanel.setSpacing(5);
		controlPanel.add(commandPanel);

		btnBold = new Button("B");
		commandPanel.add(btnBold);
		btnBold.setTitle(ApplicationController.getMessages().DesktopChatPanel_boldType());
		commandPanel.setCellVerticalAlignment(btnBold, HasVerticalAlignment.ALIGN_MIDDLE);
		btnBold.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				chatEditTextArea.getFormatter().toggleBold();
				if (chatEditTextArea.getFormatter().isBold()) {
					btnBold.setHTML("<b>B</b>");
				} else {
					btnBold.setHTML("B");
				}
			}
		});

		btnItalics = new Button("I");
		commandPanel.add(btnItalics);
		btnItalics.setTitle(ApplicationController.getMessages().DesktopChatPanel_italicsType());
		commandPanel.setCellVerticalAlignment(btnItalics, HasVerticalAlignment.ALIGN_MIDDLE);
		btnItalics.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				chatEditTextArea.getFormatter().toggleItalic();
				if (chatEditTextArea.getFormatter().isItalic()) {
					btnItalics.setHTML("<i>I</i>");
				} else {
					btnItalics.setHTML("I");
				}
			}
		});

		btnUnderline = new Button("U");
		commandPanel.add(btnUnderline);
		btnUnderline.setTitle(ApplicationController.getMessages().DesktopChatPanel_underlineType());
		commandPanel.setCellVerticalAlignment(btnUnderline, HasVerticalAlignment.ALIGN_MIDDLE);
		btnUnderline.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				chatEditTextArea.getFormatter().toggleUnderline();
				if (chatEditTextArea.getFormatter().isUnderlined()) {
					btnUnderline.setHTML("<u>U</u>");
				} else {
					btnUnderline.setHTML("U");
				}
			}
		});

		btnEmoticon = new Button();
		Image img = new Image(EmoticonUtils.getEmoticons().get(0).url);
		btnEmoticon.setHTML(img.toString());
		commandPanel.add(btnEmoticon);
		commandPanel.setCellVerticalAlignment(btnEmoticon, HasVerticalAlignment.ALIGN_MIDDLE);

		btnEmoticon.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (emoticonPallette == null) {
					emoticonPallette = new EmoticonPresenter().createView(DesktopChatPanel.this);
				}

				emoticonPallette.show();
				emoticonPallette.setPopupPosition(btnEmoticon.getAbsoluteLeft() + btnEmoticon.getOffsetWidth(),
						btnEmoticon.getAbsoluteTop() + btnEmoticon.getOffsetWidth());
			}
		});

		btnEmoticon.setEnabled(false);

		if (operator) {
			initCannedMessages(cannedMessages, commandPanel);
		}

		if (operator || ClientProperties.getInstance().getBooleanValue(ClientProperties.DISPLAY_FILE_SHARE, false)) {
			initFileShareArea(commandPanel);
		}

		HorizontalPanel editPanel = new HorizontalPanel();
		dataEntryPanel.add(editPanel);
		editPanel.setSize("100%", "100%");
		editPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		chatEditTextArea = new RichTextArea();
		editPanel.add(chatEditTextArea);
		chatEditTextArea.setSize("98%", "55px");

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

		sendButton = new Button(ApplicationController.getMessages().DesktopChatPanel_send());
		editPanel.add(sendButton);
		editPanel.setCellHorizontalAlignment(sendButton, HasHorizontalAlignment.ALIGN_LEFT);
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

	private void initFileShareArea(HorizontalPanel commandButtonsPanel) {
		fileShareFormPanel = new FormPanel();
		fileShareFormPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		fileShareFormPanel.setMethod(FormPanel.METHOD_POST);
		fileShareFormPanel.setAction(fileSharingUrl());
		commandButtonsPanel.add(fileShareFormPanel);

		commandButtonsPanel.setCellHorizontalAlignment(fileShareFormPanel, HasHorizontalAlignment.ALIGN_RIGHT);
		commandButtonsPanel.setCellVerticalAlignment(fileShareFormPanel, HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel fileSharePanel = new HorizontalPanel();
		fileSharePanel.setSpacing(2);
		fileShareFormPanel.setWidget(fileSharePanel);

		sessionField = new Hidden("session");
		fileSharePanel.add(sessionField);

		fileShare = new FileUpload();
		fileSharePanel.add(fileShare);
		fileShare.setName("file");

		fileSharePanel.setCellHorizontalAlignment(fileShare, HasHorizontalAlignment.ALIGN_RIGHT);
		fileSharePanel.setCellVerticalAlignment(fileShare, HasVerticalAlignment.ALIGN_MIDDLE);

		shareButton = new Button(ApplicationController.getMessages().DesktopChatPanel_share());
		fileSharePanel.add(shareButton);

		fileSharePanel.setCellHorizontalAlignment(shareButton, HasHorizontalAlignment.ALIGN_LEFT);
		fileSharePanel.setCellVerticalAlignment(shareButton, HasVerticalAlignment.ALIGN_MIDDLE);

		shareButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				fileShareFormPanel.submit();
			}
		});
		shareButton.setEnabled(false);

		fileShareFormPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
			public void onSubmit(SubmitEvent event) {
				if (fileShare.getFilename().trim().length() == 0) {
					MessageBoxPresenter.getInstance().show(
							ApplicationController.getMessages().DesktopChatPanel_fileTransferStatus(),
							ApplicationController.getMessages().DesktopChatPanel_noFileName(),
							MessageBoxPresenter.Severity.WARN, true);
					event.cancel();
					return;
				}

				sessionField.setValue(Long.toString(presenter.getSessionId()));
				MessageBoxPresenter.getInstance().show(
						ApplicationController.getMessages().DesktopChatPanel_fileTransferStatus(),
						ApplicationController.getMessages().DesktopChatPanel_fileTransferInProgress() + " ...",
						MessageBoxPresenter.Severity.INFO, true);
			}
		});

		fileShareFormPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				MessageBoxPresenter.getInstance().hide();
				String results = event.getResults();

				int index = results.indexOf(",");
				if (index < 0) {
					// Should not happen
					return;
				}

				int status = Integer.parseInt(results.substring(0, index));
				String param = ApplicationController.getMessages().DesktopChatPanel_noReasonGiven();
				if (index + 1 < results.length()) {
					param = results.substring(index + 1);
				}

				switch (status) {
				case ResponseMessage.OK:
					presenter.notifyFileSharing(param);
					appendToConveration(null, ApplicationController.getInstance().timestamp(),
							ApplicationController.getMessages().DesktopChatPanel_fileShared(fileShare.getFilename()));
					break;
				case ResponseMessage.NO_CONTENT:
					appendToConveration(null, ApplicationController.getInstance().timestamp(),
							ApplicationController.getMessages().DesktopChatPanel_fileNoData());
					break;
				case ResponseMessage.NOT_ACCEPTABLE:
					appendToConveration(null, ApplicationController.getInstance().timestamp(),
							ApplicationController.getMessages().DesktopChatPanel_fileTooBig());
					break;
				default:
					appendToConveration(null, ApplicationController.getInstance().timestamp(), param);
					break;
				}
			}
		});
	}

	public String fileSharingUrl() {
		return GWT.getModuleBaseURL() + "../fileSharing";
	}

	private void initCannedMessages(CannedMessageElement[] cannedMessages, HorizontalPanel commandButtonsPanel) {
		if (cannedMessages != null && cannedMessages.length > 0) {
			cannedMessageListBox = new ListBox();
			commandButtonsPanel.add(cannedMessageListBox);
			cannedMessageListBox.setVisibleItemCount(1);
			cannedMessageListBox.setWidth("200px");

			cannedMessageListBox.addItem("");
			for (int i = 0; i < cannedMessages.length; i++) {
				cannedMessageListBox.addItem(cannedMessages[i].getDescription());
			}

			commandButtonsPanel.setCellHorizontalAlignment(cannedMessageListBox, HasHorizontalAlignment.ALIGN_RIGHT);
			commandButtonsPanel.setCellVerticalAlignment(cannedMessageListBox, HasVerticalAlignment.ALIGN_MIDDLE);

			cannedMessageListBox.addChangeHandler(new ChangeHandler() {

				@Override
				public void onChange(ChangeEvent event) {
					if (cannedMessageListBox.getSelectedIndex() > 0) {
						presenter.cannedMessageSelected(cannedMessageListBox.getSelectedIndex() - 1);
						cannedMessageListBox.setSelectedIndex(-1);
					}
				}
			});
		}
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

		Widget widget = ChatPanel.Util.formatChat(from, timeStamp, message, me,
				ClientProperties.getInstance().getBooleanValue(ClientProperties.CONVERSATION_SMALL_SPACE, false));
		transcriptPanel.add(widget);

		if (userTyping) {
			transcriptPanel.add(typing);
		}

		transcriptScrollPanel.setVerticalScrollPosition(transcriptPanel.getOffsetHeight());
	}

	@Override
	public void appendToConveration(String from, long timeStamp, long formId, String formDef) {
		if (!adjusted) {
			adjustScrollHeight();
		}

		boolean userTyping = false;
		if (typing != null) {
			userTyping = true;
			transcriptPanel.remove(typing);
		}

		transcriptPanel.add(ChatPanel.Util.formatChat(from, timeStamp, "", me, true));

		Widget widget = formRenderer.renderForm(formId, formDef, this);
		transcriptPanel.add(widget);

		if (userTyping) {
			transcriptPanel.add(typing);
		}
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

		commandPanel.removeFromParent();
		dataEntryPanel.setHeight(discButton.getOffsetHeight() + 20 + "px");

		adjustScrollHeight();
	}

	private void processEnteredText() {
		String text = chatEditTextArea.getHTML();
		chatEditTextArea.setHTML("");
		text = HtmlUtils.scrubTags(text);
		if (text.length() == 0) {
			return;
		}

		text = EmoticonUtils.replaceEmoticonText(text);

		appendToConveration(null, ApplicationController.getInstance().timestamp(), text);
		presenter.sendTextMessage(text);
	}

	@Override
	public void emoticonSelected(String url) {
		chatEditTextArea.getFormatter().insertHTML("&nbsp;" + EmoticonUtils.getEmoticonTag(url) + "&nbsp;");
		chatEditTextArea.setFocus(true);
		presenter.typing();
	}

	@Override
	public void onResize() {
		super.onResize();
		adjustScrollHeight();
	}

	private void adjustScrollHeight() {
		int pheight = getOffsetHeight();
		int dheight = dataEntryPanel.getOffsetHeight();
		int sessionInfoHeaderSize = chatInfoDetailTable != null ? (int) VISITOR_TITLE_SIZE : 0;

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
	public void attach(String me, CallPartyElement otherParty, CannedMessageElement[] cannedMessages, boolean operator,
			boolean showOtherPartyInfo, boolean hideAvatar) {
		init(me, otherParty, cannedMessages, operator, showOtherPartyInfo, hideAvatar);
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
		btnEmoticon.setEnabled(enabled);

		if (shareButton != null) {
			shareButton.setEnabled(enabled);
		}

		if (operator) {
			transferButton.setEnabled(enabled);
			conferenceButton.setEnabled(enabled);
			contactList.setEnabled(enabled);
		}

		if (enabled) {
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					chatEditTextArea.setFocus(true);
				}
			});
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
					.DesktopChatPanel_numUsersInConference(otherParties.size() + 1 + "");
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
			typing = ChatPanel.Util.formatChat(from, timestamp, ChatPanel.TYPING, me,
					ClientProperties.getInstance().getBooleanValue(ClientProperties.CONVERSATION_SMALL_SPACE, false));
			typingFrom = from;
			transcriptPanel.add(typing);
			transcriptScrollPanel.setVerticalScrollPosition(transcriptPanel.getOffsetHeight());
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
	public boolean formSubmitted(long formId, Map<String, String> result) {
		presenter.submitForm(formId, result);
		return true;
	}
}
