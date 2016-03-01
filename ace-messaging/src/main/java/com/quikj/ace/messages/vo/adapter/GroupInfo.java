/**
 * 
 */
package com.quikj.ace.messages.vo.adapter;

import java.io.Serializable;

/**
 * @author amit
 *
 */
public class GroupInfo implements Serializable {

	private static final long serialVersionUID = -8968395806296951928L;
	
	private String groupName;
	private int queueSize;
	private boolean allOperatorsBusy;
	private int numOperators;
	private int numDND;
	private int waitTime;
	
	public GroupInfo() {
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public boolean isAllOperatorsBusy() {
		return allOperatorsBusy;
	}

	public void setAllOperatorsBusy(boolean allOperatorsBusy) {
		this.allOperatorsBusy = allOperatorsBusy;
	}

	public int getNumOperators() {
		return numOperators;
	}

	public void setNumOperators(int numOperators) {
		this.numOperators = numOperators;
	}

	public int getNumDND() {
		return numDND;
	}

	public void setNumDND(int numDND) {
		this.numDND = numDND;
	}

	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}
}
