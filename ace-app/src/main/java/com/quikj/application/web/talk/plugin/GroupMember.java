/**
 * 
 */
package com.quikj.application.web.talk.plugin;

/**
 * @author amit
 *
 */
public class GroupMember {

	private String groupName;
	private String userName;
	
	public GroupMember(String groupName, String userName) {
		this.groupName = groupName;
		this.userName = userName;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getUserName() {
		return userName;
	}
}
