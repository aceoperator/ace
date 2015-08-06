/*
 * JoinResponseMessage.java
 *
 * Created on March 6, 2002, 2:33 AM
 */

package com.quikj.ace.messages.vo.talk;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author amit
 */
public class JoinResponseMessage implements TalkMessageInterface {

	private static final long serialVersionUID = -7052907002128984440L;

	private List<Long> sessionList = new ArrayList<Long>();

	public JoinResponseMessage() {
	}

	public List<Long> getSessionList() {
		return sessionList;
	}

	public void setSessionList(List<Long> sessionList) {
		this.sessionList = sessionList;
	}
}
