/**
 * 
 */
package com.quikj.ace.web.client.view;


/**
 * @author beckie
 * 
 */
public class UserContact implements Comparable<UserContact> {

	private String user;
	private String fullName;
	private int callCount;
	private String avatar;
	private boolean dnd;

	public UserContact(String user, String fullName, int callCount, String avatar, boolean dnd) {
		super();
		this.user = user;
		this.fullName = fullName;
		this.callCount = callCount;
		this.avatar = avatar;
		this.dnd = dnd;
	}

	public UserContact(String user) {
		super();
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public int getCallCount() {
		return callCount;
	}

	public void setCallCount(int callCount) {
		this.callCount = callCount;
	}

	@Override
	public int compareTo(UserContact other) {
		if (other == null) {
			return 1;
		}

		String thisName = ViewUtils.formatName(getUser(), getFullName());
		String otherName = ViewUtils.formatName(other.getUser(),
				other.getFullName());
		return thisName.compareTo(otherName);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof UserContact) {
			if (this.getUser().equals(((UserContact) obj).getUser())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return user.hashCode();
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public boolean isDnd() {
		return dnd;
	}

	public void setDnd(boolean dnd) {
		this.dnd = dnd;
	}
}
