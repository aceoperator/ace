package com.quikj.ace.messages.vo.talk;

import java.util.Date;

public class RegistrationResponseMessage implements TalkMessageInterface {
	
	private static final long serialVersionUID = -4830404648632237414L;

	private GroupElement group = null;

	private String errorMessage = "";

	private MediaElements media = null;

	private CallPartyElement callPartyInfo = null;

	private GroupList groupList;

	private Date loginDate;

	public RegistrationResponseMessage() {
	}

	public CallPartyElement getCallPartyInfo() {
		return callPartyInfo;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public GroupElement getGroup() {
		return group;
	}

	public GroupList getGroupList() {
		return this.groupList;
	}

	public Date getLoginDate() {
		return this.loginDate;
	}

	public MediaElements getMediaElements() {
		return media;
	}

	public void setCallPartyInfo(CallPartyElement info) {
		callPartyInfo = info;
	}

	public void setGroup(
			GroupElement group) {
		this.group = group;
	}
	public void setGroupList(GroupList groupList) {
		this.groupList = groupList;
	}

	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}

	public void setMediaElements(MediaElements media) {
		this.media = media;
	}
}
