package com.quikj.application.web.talk.plugin;

import java.util.HashSet;

public class GroupInfo {
	// some synchronization required due to: CP+OAMP interaction or OAMP+OAMP
	// interaction

	private GroupElement groupData = new GroupElement();

	private String owner = null;

	private HashSet<String> members = new HashSet<String>();

	private int activeUserCount = 0;

	public GroupInfo(String name) {
		groupData.setName(name);
	}

	public synchronized boolean addMember(String member) {
		return members.add(member);
	}

	public int decrementActiveUserCount() {
		// returns active user count, after decrementing

		return --activeUserCount;
	}

	public int getActiveUserCount() {
		return activeUserCount;
	}

	public GroupElement getGroupData() {
		return groupData;
	}

	public synchronized String[] getMembers() {
		String[] list = new String[members.size()];
		return members.toArray(list);
	}

	public String getName() {
		return groupData.getName();
	}

	public synchronized String getOwner() {
		return owner;
	}

	public int incrementActiveUserCount() {
		return ++activeUserCount;
	}

	public synchronized boolean isMember(String member) {
		return members.contains(member);
	}

	public synchronized int numMembers() {
		return members.size();
	}

	public synchronized boolean removeMember(String member) {
		return members.remove(member);
	}

	public void setActiveUserCount(int activeUserCount) {
		this.activeUserCount = activeUserCount;
	}

	public void setGroupData(GroupElement groupData) {
		this.groupData = groupData;
	}

	public synchronized void setOwner(String owner) {
		this.owner = owner;
	}
}
