/**
 * 
 */
package com.quikj.ace.messages.vo.talk;

import java.io.Serializable;

/**
 * @author amit
 * 
 */
public class ConferencePartyInfo implements Serializable {
	private static final long serialVersionUID = 429410869322019890L;

	public static final int STATUS_PARTY = 0;
	public static final int STATUS_ADDED = 1;
	public static final int STATUS_REMOVED = 2;

	private CallPartyElement participantInfo;

	private int status = STATUS_PARTY;

	public ConferencePartyInfo() {
	}

	public ConferencePartyInfo(ConferencePartyInfo confPartyToClone) {
		this(new CallPartyElement(confPartyToClone.getParticipantInfo()),
				confPartyToClone.getStatus());
	}

	public ConferencePartyInfo(CallPartyElement participantInfo, int status) {
		this.participantInfo = participantInfo;
		this.status = status;
	}

	public CallPartyElement getParticipantInfo() {
		return participantInfo;
	}

	public void setParticipantInfo(CallPartyElement participantInfo) {
		this.participantInfo = participantInfo;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
