/**
 * 
 */
package com.quikj.ace.db.webtalk.model;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.Group;

/**
 * @author amit
 *
 */
public interface GroupBean {
	static final int NOTIFY_NONE = 0;
	static final int NOTIFY_OWNER = 1;
	static final int NOTIFY_MEMBERS = 2;
	static final int NOTIFY_ALL = 3;
	
	@Transactional(propagation = Propagation.REQUIRED)
	void createGroup(Group group);
	
	@Transactional(propagation = Propagation.REQUIRED)
	void deleteGroup(String name);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<Group> listGroups(String domain);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<String> listDomains();
	
	@Transactional(propagation = Propagation.REQUIRED)
	void modifyGroup(Group group);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	Group findByName(String name);
}
