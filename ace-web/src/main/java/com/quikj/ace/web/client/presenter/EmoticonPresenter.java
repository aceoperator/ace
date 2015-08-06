/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import com.google.gwt.user.client.ui.PopupPanel;
import com.quikj.ace.web.client.view.ChatPanel;
import com.quikj.ace.web.client.view.desktop.DesktopEmoticonPallette;

/**
 * @author amit
 * 
 */
public class EmoticonPresenter {
	
	private ChatPanel chatPanel;
	
	public EmoticonPresenter() {
	}
	
	public PopupPanel createView(ChatPanel chatPanel) {	
		this.chatPanel = chatPanel;
		PopupPanel w = new DesktopEmoticonPallette(this);
		return w;
	}

	public void emoticonSelected(String url) {		
		chatPanel.emoticonSelected(url);
	}
}
