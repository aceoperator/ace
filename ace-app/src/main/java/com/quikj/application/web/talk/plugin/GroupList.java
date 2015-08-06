package com.quikj.application.web.talk.plugin;

import java.util.Hashtable;

import com.quikj.server.framework.AceLogger;

public class GroupList
// synchronization not required for CP-CP interaction, since changes to list
// content come
// out of single ServiceController thread execution.
// (OAMP note for this class and EndPointList: update DB, then look in these
// lists
// to update Group/EndPointInfo. If item found immediately, update and done;
// otherwise,
// wait some time and look again (CP may have gotten old data from DB and is
// adding
// the item to the list). Or, run OAMP lower priority than CP.)
{

	private static GroupList instance = null;

	private Hashtable activeGroups = new Hashtable(); // key = name, value =
														// GroupInfo

	public GroupList() {
		instance = this;
	}

	public static GroupList Instance() {
		if (instance == null) {
			new GroupList();
		}

		return instance;
	}

	public int addGroup(GroupInfo groupinfo)
	// returns number of active users associated with this group, after the add
	// operation
	{
		String name = groupinfo.getName();
		GroupInfo info = (GroupInfo) activeGroups.get(name);
		if (info == null) // not currently in list
		{
			activeGroups.put(name, groupinfo);
			groupinfo.setActiveUserCount(1);
			return 1;
		} else {
			int new_count = info.incrementActiveUserCount();
			activeGroups.remove(name);
			activeGroups.put(name, groupinfo);
			groupinfo.setActiveUserCount(new_count);

			return new_count;
		}
	}

	public void dispose() {
		activeGroups.clear();
	}

	public GroupInfo findGroup(String name) {
		return (GroupInfo) activeGroups.get(name);
	}
	public int removeGroup(String name)
	// returns the new number of active users associated with this group, if
	// zero then removed from list
	{
		GroupInfo info = (GroupInfo) activeGroups.get(name);
		if (info == null) // not currently in list
		{
			AceLogger
					.Instance()
					.log(
							AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							name
									+ " group - GroupList.removeGroup() -- Couldn't find group "
									+ name + " in active group list.");
			return 0;
		}

		int ret = info.decrementActiveUserCount();
		if (ret == 0) {
			activeGroups.remove(name);
		}

		return ret;
	}

}
