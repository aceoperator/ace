/*
 * ConferencedEndPoints.java
 *
 * Created on April 14, 2002, 4:38 AM
 */

package com.quikj.application.web.talk.plugin;

import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.ConferencePartyInfo;
import com.quikj.server.app.EndPointInterface;

/**
 * 
 * @author amit
 */
public class ConferencedEndPoints {

	private EndPointInterface endpoint = null;

	private CallPartyElement information = null;
	
	private int status = ConferencePartyInfo.STATUS_PARTY;

	public ConferencedEndPoints(EndPointInterface ep) {
		this(ep, null);
	}

	public ConferencedEndPoints(EndPointInterface ep, CallPartyElement ele) {
		endpoint = ep;
		information = ele;
	}

	public EndPointInterface getEndpoint() {
		return endpoint;
	}

	public CallPartyElement getInformation() {
		return information;
	}

	public void setEndpoint(EndPointInterface endpoint) {
		this.endpoint = endpoint;
	}

	public void setInformation(CallPartyElement information) {
		this.information = information;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
