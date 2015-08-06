/**
 * 
 */
package com.quikj.ace.db.webtalk.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.quikj.ace.db.core.webtalk.vo.Group;

/**
 * @author amit
 *
 */
public interface GroupDao {	
	void createGroup(Group group);
	int deleteGroup(String name);
	List<Group> listGroups(@Param("domain") String domain);
	List<String> listDomains();
	int modifyGroup(Group group);
	Group findByName(String name);
}
