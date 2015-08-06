/*
 * JoinRequestMessage.java
 *
 * Created on March 5, 2002, 4:00 AM
 */

package com.quikj.ace.messages.vo.talk;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author amit
 */
public class JoinRequestMessage implements TalkMessageInterface {

	private static final long serialVersionUID = 6455102472748068845L;
	
	private List<Long> sessionList = new ArrayList<Long>();

	public JoinRequestMessage() {
	}

	public List<Long> getSessionList() {
		return sessionList;
	}

	public void setSessionList(List<Long> sessionList) {
		this.sessionList = sessionList;
	}
}
