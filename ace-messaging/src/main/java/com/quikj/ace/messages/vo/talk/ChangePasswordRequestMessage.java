package com.quikj.ace.messages.vo.talk;


public class ChangePasswordRequestMessage implements TalkMessageInterface {
	
	private static final long serialVersionUID = 2078805731523171460L;

	private String userName = null;

	private String oldPassword = null;

	private String newPassword = null;

	public ChangePasswordRequestMessage() {
	}

	public java.lang.String getNewPassword() {
		return newPassword;
	}

	public java.lang.String getOldPassword() {
		return oldPassword;
	}

	public java.lang.String getUserName() {
		return userName;
	}

	public void setNewPassword(java.lang.String newPassword) {
		this.newPassword = newPassword;
	}
	
	public void setOldPassword(java.lang.String oldPassword) {
		this.oldPassword = oldPassword;
	}
	public void setUserName(java.lang.String userName) {
		this.userName = userName;
	}
}
