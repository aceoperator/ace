package com.quikj.ace.messages.vo.talk;

import java.io.Serializable;

public class CallingNameElement implements Serializable {
	private static final long serialVersionUID = 7361971110361120833L;
	
	private CallPartyElement callParty = null;

	public CallingNameElement() {
	}

	public CallPartyElement getCallParty() {
		return callParty;
	}

	public void setCallParty(CallPartyElement party) {
		callParty = party;
	}
}
