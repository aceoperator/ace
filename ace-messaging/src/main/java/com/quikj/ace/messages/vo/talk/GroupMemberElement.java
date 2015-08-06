/*
 * GroupMemberElement.java
 *
 * Created on April 27, 2002, 9:19 AM
 */

package com.quikj.ace.messages.vo.talk;

/**
 * 
 * @author amit
 */
public class GroupMemberElement implements TalkMessageInterface {
	
	private static final long serialVersionUID = 8710085345100946859L;
	
	public static final int OPERATION_ADD_LIST = 1;
	public static final int OPERATION_REM_LIST = 2;
	public static final int OPERATION_MOD_LIST = 3;

	private int operation = 0;

	private String user = null;

	private String fullName = null;
	
	private String avatar;

	private int callCount = -1;

	public GroupMemberElement() {
	}

	public int getCallCount() {
		return callCount;
	}

	public java.lang.String getFullName() {
		return fullName;
	}

	public int getOperation() {
		return operation;
	}

	public java.lang.String getUser() {
		return user;
	}

	public void setCallCount(int callCount) {
		this.callCount = callCount;
	}

	public void setFullName(java.lang.String fullName) {
		this.fullName = fullName;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}

	public void setUser(java.lang.String user) {
		this.user = user;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
}
