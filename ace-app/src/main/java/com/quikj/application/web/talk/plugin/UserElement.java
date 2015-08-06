package com.quikj.application.web.talk.plugin;

import java.util.HashSet;

public class UserElement {
	private String name;

	private String fullName;

	private String address;

	private String additionalInfo;

	private String unavailXferTo;
	
	private String avatar;

	private boolean changePassword;

	private HashSet<String> belongsToGroups = new HashSet<String>();

	private HashSet<String> ownsGroups = new HashSet<String>();

	private String gatekeeper;
	
	private boolean privateInfo;

	public UserElement() {
	}

	public synchronized boolean addBelongsToGroup(String group) {
		return belongsToGroups.add(group);
	}

	public synchronized boolean addOwnsGroup(String group) {
		return ownsGroups.add(group);
	}

	public synchronized boolean belongsToGroup(String group) {
		return belongsToGroups.contains(group);
	}

	public synchronized String getAdditionalInfo() {
		return additionalInfo;
	}

	public synchronized String getAddress() {
		return address;
	}

	public synchronized String[] getBelongsToGroups() {
		String[] list = new String[belongsToGroups.size()];
		int count = 0;
		
		for (String group: belongsToGroups) {
			list[count++] = group;
		}
		
		return list;
	}

	public synchronized String getFullName() {
		return fullName;
	}

	public String getGatekeeper() {
		return this.gatekeeper;
	}

	public String getName() {
		return name;
	}

	public synchronized String[] getOwnsGroups() {
		String[] list = new String[ownsGroups.size()];
		int count = 0;
		
		for (String group: ownsGroups) {
			list[count++] = group;
		}
		
		return list;
	}

	public String getUnavailXferTo() {
		return unavailXferTo;
	}

	public synchronized int numBelongsToGroups() {
		return belongsToGroups.size();
	}

	public synchronized int numOwnsGroups() {
		return ownsGroups.size();
	}

	public synchronized boolean ownsGroup(String group) {
		return ownsGroups.contains(group);
	}

	public synchronized boolean removeBelongsToGroup(String group) {
		return belongsToGroups.remove(group);
	}

	public synchronized boolean removeOwnsGroup(String group) {
		return ownsGroups.remove(group);
	}

	public synchronized void setAdditionalInfo(String info) {
		additionalInfo = info;
	}
	
	public synchronized void setAddress(String address) {
		this.address = address;
	}
	
	public synchronized void setFullName(String fullname) {
		fullName = fullname;
	}
	
	public void setGatekeeper(String gatekeeper) {
		this.gatekeeper = gatekeeper;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUnavailXferTo(String unavailXferTo) {
		this.unavailXferTo = unavailXferTo;
	}
	
	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public boolean isChangePassword() {
		return changePassword;
	}

	public void setChangePassword(boolean changePassword) {
		this.changePassword = changePassword;
	}

	public boolean isPrivateInfo() {
		return privateInfo;
	}

	public void setPrivateInfo(boolean privateInfo) {
		this.privateInfo = privateInfo;
	}
}
