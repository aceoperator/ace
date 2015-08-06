package com.quikj.ace.messages.vo.talk;

import java.io.Serializable;

public class DisconnectReasonElement implements Serializable {
	private static final long serialVersionUID = -8225735935762704416L;

	// TODO convert to an enum
	public static final int NORMAL_DISCONNECT = 0;
	public static final int CLIENT_EXIT = 1;
	public static final int SERVER_DISCONNECT = 2;
	
	public static final String[] DISCONNECT_CODE_DESCRIPTIONS = {
		"normal disconnect",
		"client terminated",
		"server terminated"
	};
	
	private int reasonCode = 0;

	private String reasonText = null;

	public DisconnectReasonElement() {
	}

	public int getReasonCode() {
		return reasonCode;
	}

	public String getReasonText() {
		return reasonText;
	}

	public void setReasonCode(int code) {
		reasonCode = code;
	}
	public void setReasonText(String text) {
		reasonText = text;
	}
}
