/*
 * OperatorElement.java
 *
 * Created on May 2, 2003, 3:01 AM
 */

package com.quikj.application.web.talk.feature.operator;

import com.quikj.ace.messages.vo.talk.GroupMemberElement;

/**
 * 
 * @author amit
 */
public class OperatorElement {
	private GroupMemberElement operatorInfo;

	public OperatorElement() {
	}

	public GroupMemberElement getOperatorInfo() {
		return this.operatorInfo;
	}

	public void setOperatorInfo(GroupMemberElement operatorInfo) {
		this.operatorInfo = operatorInfo;
	}
}
