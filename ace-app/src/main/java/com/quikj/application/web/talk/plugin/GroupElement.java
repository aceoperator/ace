package com.quikj.application.web.talk.plugin;

public class GroupElement {
	// some synchronization required due to: CP+OAMP interaction or OAMP+OAMP
	// interaction

	private String name;

	private int memberLoginNotificationControl;

	private int memberBusyNotificationControl;

	private int ownerLoginNotificationControl;

	private int ownerBusyNotificationControl;

	/** Creates a new instance of GroupElement */
	public GroupElement() {
	}

	/**
	 * Getter for property memberBusyNotificationControl.
	 * 
	 * @return Value of property memberBusyNotificationControl.
	 */
	public synchronized int getMemberBusyNotificationControl() {
		return memberBusyNotificationControl;
	}

	/**
	 * Getter for property memberLoginNotificationControl.
	 * 
	 * @return Value of property memberLoginNotificationControl.
	 */
	public synchronized int getMemberLoginNotificationControl() {
		return memberLoginNotificationControl;
	}

	/**
	 * Getter for property name.
	 * 
	 * @return Value of property name.
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * Getter for property ownerBusyNotificationControl.
	 * 
	 * @return Value of property ownerBusyNotificationControl.
	 */
	public int getOwnerBusyNotificationControl() {
		return ownerBusyNotificationControl;
	}

	/**
	 * Getter for property ownerLoginNotificationControl.
	 * 
	 * @return Value of property ownerLoginNotificationControl.
	 */
	public int getOwnerLoginNotificationControl() {
		return ownerLoginNotificationControl;
	}

	/**
	 * Setter for property memberBusyNotificationControl.
	 * 
	 * @param memberBusyNotificationControl
	 *            New value of property memberBusyNotificationControl.
	 */
	public synchronized void setMemberBusyNotificationControl(
			int memberBusyNotificationControl) {
		this.memberBusyNotificationControl = memberBusyNotificationControl;
	}

	/**
	 * Setter for property memberLoginNotificationControl.
	 * 
	 * @param memberLoginNotificationControl
	 *            New value of property memberLoginNotificationControl.
	 */
	public synchronized void setMemberLoginNotificationControl(
			int memberLoginNotificationControl) {
		this.memberLoginNotificationControl = memberLoginNotificationControl;
	}

	/**
	 * Setter for property name.
	 * 
	 * @param name
	 *            New value of property name.
	 */
	public void setName(java.lang.String name) {
		this.name = name;
	}

	/**
	 * Setter for property ownerBusyNotificationControl.
	 * 
	 * @param ownerBusyNotificationControl
	 *            New value of property ownerBusyNotificationControl.
	 */
	public void setOwnerBusyNotificationControl(int ownerBusyNotificationControl) {
		this.ownerBusyNotificationControl = ownerBusyNotificationControl;
	}

	/**
	 * Setter for property ownerLoginNotificationControl.
	 * 
	 * @param ownerLoginNotificationControl
	 *            New value of property ownerLoginNotificationControl.
	 */
	public void setOwnerLoginNotificationControl(
			int ownerLoginNotificationControl) {
		this.ownerLoginNotificationControl = ownerLoginNotificationControl;
	}

}
