package com.quikj.ace.messages.vo.talk;

public class UserToUserMessage implements TalkMessageInterface {
	
	private static final long serialVersionUID = -4183597125615435089L;

	/** Holds value of property applicationClass. */
	private String applicationClass;

	/** Holds value of property applicationMessage. */
	private String applicationMessage;

	/** Holds value of property endPointName. */
	private String endPointName;

	public UserToUserMessage() {
	}

	/**
	 * Getter for property applicationClass.
	 * 
	 * @return Value of property applicationClass.
	 * 
	 */
	public String getApplicationClass() {
		return this.applicationClass;
	}

	/**
	 * Getter for property applicationMessage.
	 * 
	 * @return Value of property applicationMessage.
	 * 
	 */
	public String getApplicationMessage() {
		return this.applicationMessage;
	}

	/**
	 * Getter for property endPointName.
	 * 
	 * @return Value of property endPointName.
	 * 
	 */
	public String getEndPointName() {
		return this.endPointName;
	}

	/**
	 * Setter for property applicationClass.
	 * 
	 * @param applicationClass
	 *            New value of property applicationClass.
	 * 
	 */
	public void setApplicationClass(String applicationClass) {
		this.applicationClass = applicationClass;
	}
	/**
	 * Setter for property applicationMessage.
	 * 
	 * @param applicationMessage
	 *            New value of property applicationMessage.
	 * 
	 */
	public void setApplicationMessage(String applicationMessage) {
		this.applicationMessage = applicationMessage;
	}

	/**
	 * Setter for property endPointName.
	 * 
	 * @param endPointName
	 *            New value of property endPointName.
	 * 
	 */
	public void setEndPointName(String endPointName) {
		this.endPointName = endPointName;
	}
}
