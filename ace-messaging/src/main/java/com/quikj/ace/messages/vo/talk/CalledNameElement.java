package com.quikj.ace.messages.vo.talk;

import java.io.Serializable;

public class CalledNameElement implements Serializable {
	private static final long serialVersionUID = -1928771289718528512L;

	private CallPartyElement callParty = null;

	private String terminal = null;

	public CalledNameElement() {
	}

	public CalledNameElement(CalledNameElement calledNameElementToClone) {
		this(new CallPartyElement(calledNameElementToClone.getCallParty()),
				calledNameElementToClone.getTerminal());
	}

	public CalledNameElement(CallPartyElement callParty, String terminal) {
		this.callParty = callParty;
		this.terminal = terminal;
	}

	public CallPartyElement getCallParty() {
		return callParty;
	}

	public String getTerminal() {
		return terminal;
	}

	public void setCallParty(CallPartyElement party) {
		callParty = party;
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
}
