/*
 * EndPointList.java
 *
 * Created on March 31, 2002, 12:24 PM
 */

package com.quikj.application.web.talk.plugin;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.quikj.application.web.talk.plugin.accounting.LogoutCDR;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceLogger;

public class RegisteredEndPointList {
	// see note in GroupList

	private static RegisteredEndPointList instance = null;

	private Hashtable registeredNames = new Hashtable(); // key = name, value =

	// EndPointInfo
	private Hashtable registeredEndPoints = new Hashtable(); // key =
																// EndPointInterface,
																// value =
																// EndPointInfo

	public static final int NOTIFY_NONE = 0;

	public static final int NOTIFY_OWNER = 1;

	public static final int NOTIFY_MEMBERS = 2;

	public static final int NOTIFY_ALL = 3;

	public RegisteredEndPointList() {
		instance = this;
	}

	public static RegisteredEndPointList Instance() {
		if (instance == null) {
			new RegisteredEndPointList();
		}

		return instance;
	}

	public boolean addRegisteredEndPoint(EndPointInfo endpointinfo) {
		// called only by CP, never OAMP
		// caller handles GroupList items separately

		synchronized (registeredNames) {
			if (registeredNames.get(endpointinfo.getName()) == null) // not
																		// already
																		// registered
			{
				registeredNames.put(endpointinfo.getName(), endpointinfo);
				registeredEndPoints.put(endpointinfo.getEndPoint(), endpointinfo);
				return true;
			} else {
				return false;
			}
		}
	}

	public void clearEndpointList() {
		synchronized (registeredNames) {
			Enumeration e = registeredEndPoints.elements();

			while (e.hasMoreElements() == true) {
				EndPointInfo info = (EndPointInfo) e.nextElement();
				ServiceController.Instance().sendCDR(new LogoutCDR(info.getEndPoint().getIdentifier()));
			}

			registeredEndPoints.clear();
			registeredNames.clear();
		}
	}

	public void dispose() {
		synchronized (registeredNames) {
			registeredNames.clear();
			registeredEndPoints.clear();
		}
	}

	public EndPointInterface findRegisteredEndPoint(String name) {
		synchronized (registeredNames) {
			EndPointInfo info = (EndPointInfo) registeredNames.get(name);
			if (info == null) {
				return null;
			}

			return info.getEndPoint();
		}
	}

	public EndPointInfo findRegisteredEndPointInfo(EndPointInterface endpoint) {
		synchronized (registeredNames) {
			return ((EndPointInfo) registeredEndPoints.get(endpoint));
		}
	}

	public EndPointInfo findRegisteredEndPointInfo(String name) {
		synchronized (registeredNames) {
			return ((EndPointInfo) registeredNames.get(name));
		}
	}

	public String findRegisteredName(EndPointInterface endpoint) {
		synchronized (registeredNames) {
			EndPointInfo info = (EndPointInfo) registeredEndPoints.get(endpoint);
			if (info == null) {
				return null;
			}

			return info.getName();
		}
	}

	public UserElement findRegisteredUserData(EndPointInterface endpoint) {
		synchronized (registeredNames) {
			EndPointInfo info = (EndPointInfo) registeredEndPoints.get(endpoint);
			if (info == null) {
				return null;
			}

			return info.getUserData();
		}
	}

	public UserElement findRegisteredUserData(String name) {
		synchronized (registeredNames) {
			EndPointInfo info = (EndPointInfo) registeredNames.get(name);
			if (info == null) {
				return null;
			}

			return info.getUserData();
		}
	}

	public String[] getActiveMembers(String name)
	// called to get list of active group members to notify parm name of
	// returns an array of 1 or more elements, or null if no users to notify
	{
		String[] list = null;

		UserElement user_data = findRegisteredUserData(name);
		if (user_data == null) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					Thread.currentThread().getName()
							+ " EndPointList.getActiveMembers() -- Couldn't find user data for user " + name
							+ " in registered user list.");

			return null;
		}

		HashSet results = new HashSet();

		// process owns groups

		String[] group_list = user_data.getOwnsGroups();
		for (int i = 0; i < group_list.length; i++) {
			GroupInfo group_info = GroupList.Instance().findGroup(group_list[i]);
			if (group_info == null) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ " EndPointList.getActiveMembers() -- Couldn't find owned group data for user " + name
								+ ", group name " + group_list[i]);

				continue;
			}

			// should this owner see the members?
			int notif_control = group_info.getGroupData().getMemberLoginNotificationControl();
			switch (notif_control) {
			case RegisteredEndPointList.NOTIFY_OWNER:
			case RegisteredEndPointList.NOTIFY_ALL: {
				String[] members = group_info.getMembers();
				for (int j = 0; j < members.length; j++) {
					results.add(new String(members[j]));
				}
			}
				break;
			}
		}

		// process belongs to groups

		group_list = user_data.getBelongsToGroups();
		for (int i = 0; i < group_list.length; i++) {
			GroupInfo group_info = GroupList.Instance().findGroup(group_list[i]);
			if (group_info == null) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ " EndPointList.getActiveMembers() -- Couldn't find belong to group data for user "
								+ name + ", group name " + group_list[i]);

				continue;
			}

			// should this member see the other members?
			int notif_control = group_info.getGroupData().getMemberLoginNotificationControl();
			switch (notif_control) {
			case RegisteredEndPointList.NOTIFY_MEMBERS:
			case RegisteredEndPointList.NOTIFY_ALL: {
				String[] members = group_info.getMembers();
				for (int j = 0; j < members.length; j++) {
					if (members[j].equals(name) == false) {
						results.add(new String(members[j]));
					}
				}
			}
				break;
			}

			// should this member see the owner?
			notif_control = group_info.getGroupData().getOwnerLoginNotificationControl();
			switch (notif_control) {
			case RegisteredEndPointList.NOTIFY_MEMBERS:
			case RegisteredEndPointList.NOTIFY_ALL:
				results.add(new String(group_info.getOwner()));
				break;
			}
		}

		if (results.size() > 0) {
			for (Iterator i = results.iterator(); i.hasNext();) {
				if (RegisteredEndPointList.Instance().findRegisteredEndPoint((String) i.next()) == null) {
					i.remove();
				}
			}

			if (results.size() > 0) {
				String[] temp = new String[results.size()];
				list = ((String[]) (results.toArray(temp)));
			}
		}

		return list;
	}

	public int getCallCount(EndPointInterface endpoint)
	// returns current call count
	// returns -1 if element not found
	{
		synchronized (registeredNames) {
			EndPointInfo info = (EndPointInfo) registeredEndPoints.get(endpoint);
			if (info == null) {
				return -1;
			}

			return info.getCallCount();
		}
	}

	public int getCallCount(String name)
	// returns current call count
	// returns -1 if element not found
	{
		synchronized (registeredNames) {
			EndPointInfo info = (EndPointInfo) registeredNames.get(name);
			if (info == null) {
				return -1;
			}

			return info.getCallCount();
		}
	}

	public String[] notifyOfCallCountChange(String name)
	// called to get list of group members to notify when parm name's call count
	// changes
	// returns an array of 1 or more elements, or null if no users to notify
	{
		String[] list = null;

		UserElement user_data = findRegisteredUserData(name);
		if (user_data == null) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					Thread.currentThread().getName()
							+ " EndPointList.notifyOfBusyIdle() -- Couldn't find user data for user " + name
							+ " in registered user list.");

			return null;
		}

		HashSet<String> results = new HashSet<String>();

		// process owns groups

		String[] group_list = user_data.getOwnsGroups();
		for (int i = 0; i < group_list.length; i++) {
			GroupInfo group_info = GroupList.Instance().findGroup(group_list[i]);
			if (group_info == null) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ " EndPointList.notifyOfBusyIdle() -- Couldn't find owned group data for user " + name
								+ ", group name " + group_list[i]);

				continue;
			}

			int notif_control = group_info.getGroupData().getOwnerBusyNotificationControl();
			switch (notif_control) {
			case RegisteredEndPointList.NOTIFY_MEMBERS:
			case RegisteredEndPointList.NOTIFY_ALL: {
				String[] members = group_info.getMembers();
				for (int j = 0; j < members.length; j++) {
					results.add(new String(members[j]));
				}
			}
				break;
			case RegisteredEndPointList.NOTIFY_OWNER:
			case RegisteredEndPointList.NOTIFY_NONE:
				break;
			default: {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ " EndPointList.notifyOfBusyIdle() -- Invalid busy/idle notification setting "
								+ notif_control + " in group data for group " + group_info.getName());
			}
				break;
			}
		}

		// process belongs to groups

		group_list = user_data.getBelongsToGroups();
		for (int i = 0; i < group_list.length; i++) {
			GroupInfo group_info = GroupList.Instance().findGroup(group_list[i]);
			if (group_info == null) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ " EndPointList.notifyOfBusyIdle() -- Couldn't find belong to group data for user "
								+ name + ", group name " + group_list[i]);

				continue;
			}

			int notif_control = group_info.getGroupData().getMemberBusyNotificationControl();
			switch (notif_control) {
			case RegisteredEndPointList.NOTIFY_MEMBERS: {
				String[] members = group_info.getMembers();
				for (int j = 0; j < members.length; j++) {
					if (members[j].equals(name) == false) {
						results.add(new String(members[j]));
					}
				}
			}
				break;
			case RegisteredEndPointList.NOTIFY_ALL: {
				String[] members = group_info.getMembers();
				for (int j = 0; j < members.length; j++) {
					if (members[j].equals(name) == false) {
						results.add(new String(members[j]));
					}
				}

				results.add(new String(group_info.getOwner()));
			}
				break;
			case RegisteredEndPointList.NOTIFY_OWNER: {
				results.add(new String(group_info.getOwner()));
			}
				break;
			case RegisteredEndPointList.NOTIFY_NONE:
				break;
			default: {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ " EndPointList.notifyOfBusyIdle() -- Invalid busy/idle notification setting "
								+ notif_control + " in group data for group " + group_info.getName());
			}
				break;
			}
		}

		if (results.size() > 0) {
			list = results.toArray(new String[results.size()]);
		}

		return list;
	}

	public String[] notifyOfLoginLogout(String name) {
		// called to get list of group members to notify when parm name logs
		// in/out returns an array of 1 or more elements, or null if no users to
		// notify
		String[] list = null;

		UserElement userData = findRegisteredUserData(name);
		if (userData == null) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					Thread.currentThread().getName()
							+ " EndPointList.notifyOfLoginLogout() -- Couldn't find user data for user " + name
							+ " in registered user list.");

			return null;
		}

		Set<String> results = new HashSet<String>();

		// process owns groups
		String[] groupList = userData.getOwnsGroups();
		for (int i = 0; i < groupList.length; i++) {
			GroupInfo groupInfo = GroupList.Instance().findGroup(groupList[i]);
			if (groupInfo == null) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ " EndPointList.notifyOfLoginLogout() -- Couldn't find owned group data for user "
								+ name + ", group name " + groupList[i]);

				continue;
			}

			int notifControl = groupInfo.getGroupData().getOwnerLoginNotificationControl();
			switch (notifControl) {
			case RegisteredEndPointList.NOTIFY_MEMBERS:
			case RegisteredEndPointList.NOTIFY_ALL: {
				String[] members = groupInfo.getMembers();
				for (int j = 0; j < members.length; j++) {
					results.add(new String(members[j]));
				}
			}
				break;
			case RegisteredEndPointList.NOTIFY_OWNER:
			case RegisteredEndPointList.NOTIFY_NONE:
				break;
			default: {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ " EndPointList.notifyOfLoginLogout() -- Invalid login notification setting "
								+ notifControl + " in group data for group " + groupInfo.getName());
			}
				break;
			}
		}

		// process belongs to groups
		groupList = userData.getBelongsToGroups();
		for (int i = 0; i < groupList.length; i++) {
			GroupInfo group_info = GroupList.Instance().findGroup(groupList[i]);
			if (group_info == null) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ " EndPointList.notifyOfLoginLogout() -- Couldn't find belong to group data for user "
								+ name + ", group name " + groupList[i]);

				continue;
			}

			int notifControl = group_info.getGroupData().getMemberLoginNotificationControl();
			switch (notifControl) {
			case RegisteredEndPointList.NOTIFY_MEMBERS: {
				String[] members = group_info.getMembers();
				for (int j = 0; j < members.length; j++) {
					if (members[j].equals(name) == false) {
						results.add(new String(members[j]));
					}
				}
			}
				break;
			case RegisteredEndPointList.NOTIFY_ALL: {
				String[] members = group_info.getMembers();
				for (int j = 0; j < members.length; j++) {
					if (members[j].equals(name) == false) {
						results.add(new String(members[j]));
					}
				}

				results.add(new String(group_info.getOwner()));
			}
				break;
			case RegisteredEndPointList.NOTIFY_OWNER: {
				results.add(new String(group_info.getOwner()));
			}
				break;
			case RegisteredEndPointList.NOTIFY_NONE:
				break;
			default: {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ " EndPointList.notifyOfLoginLogout() -- Invalid login notification setting "
								+ notifControl + " in group data for group " + group_info.getName());
			}
				break;
			}
		}

		if (results.size() > 0) {
			String[] temp = new String[results.size()];
			list = ((String[]) (results.toArray(temp)));
		}

		return list;
	}

	public boolean removeRegisteredEndPoint(String name) {
		// this method also handles 'removing' GroupList items associated with
		// the endpoint
		// tbd: decide how OAMP handles deleting a cp-busy user - if we decide
		// to drop the
		// call, the EndPoint should deregister itself normally, not OAMP
		// calling this method

		synchronized (registeredNames) {
			EndPointInfo info = (EndPointInfo) registeredNames.get(name);
			if (info == null) // not found
			{
				return false;
			}

			String[] group_list = info.getUserData().getOwnsGroups();
			for (int i = 0; i < group_list.length; i++) {
				GroupList.Instance().removeGroup(group_list[i]);
			}

			group_list = info.getUserData().getBelongsToGroups();
			for (int i = 0; i < group_list.length; i++) {
				GroupList.Instance().removeGroup(group_list[i]);
			}

			registeredNames.remove(name);
			registeredEndPoints.remove(info.getEndPoint());
		}
		return true;
	}

	public void setCallCount(EndPointInterface endpoint, int count) {
		synchronized (registeredNames) {
			EndPointInfo info = (EndPointInfo) registeredEndPoints.get(endpoint);
			if (info == null) {
				return;
			}

			info.setCallCount(count);
		}
	}
}
