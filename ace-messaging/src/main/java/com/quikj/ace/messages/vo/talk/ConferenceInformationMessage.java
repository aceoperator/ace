/*
 * ConferenceInformationMessage.java
 *
 * Created on April 14, 2002, 8:19 AM
 */

package com.quikj.ace.messages.vo.talk;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author amit
 */
public class ConferenceInformationMessage implements TalkMessageInterface {
	
	private static final long serialVersionUID = -653839158285873492L;

	private long sessionId = -1;

	private List<ConferencePartyInfo> endpointList = new ArrayList<ConferencePartyInfo>();
	
	public ConferenceInformationMessage() {
	}
	
	public ConferenceInformationMessage(ConferenceInformationMessage confInfoToClone) {
		List<ConferencePartyInfo> list = new ArrayList<ConferencePartyInfo>();		
		setValues(confInfoToClone.getSessionId(), list);
		
		for (ConferencePartyInfo party: confInfoToClone.getEndpointList()) {
			list.add(new ConferencePartyInfo(party));
		}
	}

	public ConferenceInformationMessage(long sessionId,
			List<ConferencePartyInfo> endpointList) {
		setValues(sessionId, endpointList);
	}

	private void setValues(long sessionId,
			List<ConferencePartyInfo> endpointList) {
		this.sessionId = sessionId;
		this.endpointList = endpointList;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public List<ConferencePartyInfo> getEndpointList() {
		return endpointList;
	}

	public void setEndpointList(List<ConferencePartyInfo> endpointList) {
		this.endpointList = endpointList;
	}
}
