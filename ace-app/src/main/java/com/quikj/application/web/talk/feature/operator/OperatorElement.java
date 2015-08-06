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
	/** Holds value of property operatorInfo. */
	private GroupMemberElement operatorInfo;

	/** Creates a new instance of OperatorElement */
	public OperatorElement() {
	}

	/**
	 * Getter for property operatorInfo.
	 * 
	 * @return Value of property operatorInfo.
	 * 
	 */
	public GroupMemberElement getOperatorInfo() {
		return this.operatorInfo;
	}

	/**
	 * Setter for property operatorInfo.
	 * 
	 * @param operatorInfo
	 *            New value of property operatorInfo.
	 * 
	 */
	public void setOperatorInfo(GroupMemberElement operatorInfo) {
		this.operatorInfo = operatorInfo;
	}
}
