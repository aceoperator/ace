/**
 * 
 */
package com.quikj.ace.db.webtalk.dao;

import java.util.Calendar;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.quikj.ace.db.core.webtalk.vo.SecurityQuestion;
import com.quikj.ace.db.core.webtalk.vo.User;
import com.quikj.ace.db.core.webtalk.vo.UserStatistics;
import com.quikj.ace.db.core.webtalk.vo.VisitorStatistics;
import com.quikj.ace.db.webtalk.value.TrafficMeasurement;

/**
 * @author amit
 * 
 */
public interface UserDao {
	long getUserId(String userName);

	User authenticate(@Param("userName") String userName,
			@Param("password") String password);

	List<String> getBelongsToGroups(String userName);

	List<String> getOwnsGroups(String userName);

	int changePassword(@Param("userName") String userName,
			@Param("oldPassword") String oldPassword,
			@Param("newPassword") String newPassword);

	int changePassword2(@Param("userName") String userName,
			@Param("newPassword") String newPassword);

	String groupOwner(String groupName);

	void createUser(User user);

	void createSecurityQuestion(SecurityQuestion question);

	void createGroupOwner(@Param("userName") String userName,
			@Param("group") String group);

	void createGroupMember(@Param("userName") String userName,
			@Param("group") String group);

	User getByName(String userName);

	List<SecurityQuestion> getSecurityQuestions(String userName);

	void modifyUser(User user);

	void removeGroupMember(@Param("userName") String userName,
			@Param("group") String group);

	void removeGroupOwner(@Param("userName") String userName,
			@Param("group") String group);

	void removeSecurityQuestion(SecurityQuestion question);

	int removeUser(String userName);

	List<User> listUsers(String group);

	List<String> searchUser(User user);

	List<String> findMembersByGroupDomain(String domainName);

	List<String> findOwnersByGroupDomain(String domainName);

	List<String> listUsersByDomain(String domainName);

	List<UserStatistics> getUserReport(@Param("startDate") Calendar startDate,
			@Param("endDate") Calendar endDate,
			@Param("groupByLoginDate") boolean groupByLoginDate);
	
	List<VisitorStatistics> getVisitorReport(@Param("startDate") Calendar startDate,
			@Param("endDate") Calendar endDate,
			@Param("groupByLoginDate") boolean groupByLoginDate);
	
	List<TrafficMeasurement> getTrafficReport(@Param("startDate") Calendar startDate,
			@Param("endDate") Calendar endDate, @Param("groupName") String groupName);

	// /////////////////////////////////////////////////
	// The following methods are used for tests only
	// /////////////////////////////////////////////////
	void replace(User user);

	void replaceGroup(@Param("groupName") String groupName,
			@Param("domainName") String domainName);

	void replaceUserGroupAssociation(@Param("userName") String userName,
			@Param("groupName") String groupName);
}
