package com.quikj.ace.messages.vo.talk;

import java.io.Serializable;
import java.util.Vector;

/**
 * 
 * @author amit
 */
public class GroupList implements Serializable {
	private static final long serialVersionUID = 7993662296330003623L;
	
	private Vector<String> list = new Vector<String>();

	public GroupList() {
	}

	public void addElement(String group) {
		list.addElement(group);
	}

	public String getElementAt(int index) {
		return list.elementAt(index);
	}

	public int numElements() {
		return list.size();
	}
}
