package com.quikj.ace.messages.vo.talk;


public class ChangePasswordRequestMessage implements TalkMessageInterface {
	
	private static final long serialVersionUID = 2078805731523171460L;

	private String userName = null;

	private String oldPassword = null;

	private String newPassword = null;

	public ChangePasswordRequestMessage() {
	}

	/**
	 * Getter for property newPassword.
	 * 
	 * @return Value of property newPassword.
	 */
	public java.lang.String getNewPassword() {
		return newPassword;
	}

	/**
	 * Getter for property oldPassword.
	 * 
	 * @return Value of property oldPassword.
	 */
	public java.lang.String getOldPassword() {
		return oldPassword;
	}

	/**
	 * Getter for property userName.
	 * 
	 * @return Value of property userName.
	 */
	public java.lang.String getUserName() {
		return userName;
	}

	/**
	 * Setter for property newPassword.
	 * 
	 * @param newPassword
	 *            New value of property newPassword.
	 */
	public void setNewPassword(java.lang.String newPassword) {
		this.newPassword = newPassword;
	}
	/**
	 * Setter for property oldPassword.
	 * 
	 * @param oldPassword
	 *            New value of property oldPassword.
	 */
	public void setOldPassword(java.lang.String oldPassword) {
		this.oldPassword = oldPassword;
	}
	/**
	 * Setter for property userName.
	 * 
	 * @param userName
	 *            New value of property userName.
	 */
	public void setUserName(java.lang.String userName) {
		this.userName = userName;
	}
}
