/*
 * GroupActivityMessage.java
 *
 * Created on April 27, 2002, 10:58 AM
 */

package com.quikj.ace.messages.vo.talk;

/**
 * 
 * @author amit
 */
public class GroupActivityMessage implements TalkMessageInterface {
	private static final long serialVersionUID = 7193289496340809834L;
	
	GroupElement group = null;

	/** Creates a new instance of GroupActivityMessage */
	public GroupActivityMessage() {
	}

	/**
	 * Getter for property group.
	 * 
	 * @return Value of property group.
	 */
	public GroupElement getGroup() {
		return group;
	}

	/**
	 * Setter for property group.
	 * 
	 * @param group
	 *            New value of property group.
	 */
	public void setGroup(GroupElement group) {
		this.group = group;
	}
}
