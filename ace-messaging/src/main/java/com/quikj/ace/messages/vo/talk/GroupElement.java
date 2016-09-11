/*
 * GroupElement.java
 *
 * Created on April 27, 2002, 9:38 AM
 */

package com.quikj.ace.messages.vo.talk;

import java.util.Vector;

/**
 * 
 * @author amit
 */
public class GroupElement implements TalkMessageInterface {
	private static final long serialVersionUID = -6417946797053471282L;
	
	private Vector<GroupMemberElement> elements = new Vector<GroupMemberElement>();

	public GroupElement() {
	}

	public void addElement(GroupMemberElement element) {
		elements.addElement(element);
	}

	public GroupMemberElement elementAt(int index) {
		return elements.elementAt(index);
	}

	public int numElements() {
		return elements.size();
	}

	public Vector<GroupMemberElement> getElements() {
		return elements;
	}
}
