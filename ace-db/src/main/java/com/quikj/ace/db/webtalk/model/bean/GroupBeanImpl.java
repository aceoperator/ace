/**
 * 
 */
package com.quikj.ace.db.webtalk.model.bean;

import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.dao.GroupDao;
import com.quikj.ace.db.webtalk.model.GroupBean;

/**
 * @author amit
 *
 */
public class GroupBeanImpl implements GroupBean {
	
	private GroupDao groupDao;

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	@Override
	public void createGroup(Group group) {
		groupDao.createGroup(group);
	}

	@Override
	public void deleteGroup(String name) {
		int affected = groupDao.deleteGroup(name);
		if (affected == 0) {
			throw new WebTalkException("The group could not be found");
		}
	}

	@Override
	public List<Group> listGroups(String domain) {
		return groupDao.listGroups(domain);
	}

	@Override
	public List<String> listDomains() {
		return groupDao.listDomains();
	}

	@Override
	public void modifyGroup(Group group) {
		int affected = groupDao.modifyGroup(group);
		if (affected == 0) {
			throw new WebTalkException("The group could not be found");
		}
	}

	@Override
	public Group findByName(String name) {
		Group group =  groupDao.findByName(name);
		if (group == null) {
			throw new WebTalkException("The group is not found");
		}
		
		return group;
	}
}
