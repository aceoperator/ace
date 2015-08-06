/**
 * 
 */
package com.quikj.ace.db.webtalk.model;

import java.util.Calendar;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.TrafficStatistics;
import com.quikj.ace.db.core.webtalk.vo.User;
import com.quikj.ace.db.core.webtalk.vo.UserStatistics;
import com.quikj.ace.db.core.webtalk.vo.VisitorStatistics;

/**
 * @author amit
 * 
 */
public interface UserBean {
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	long getUserId(String userName);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<String> getMemberOfGroupNames(String userName);

	@Transactional(propagation = Propagation.REQUIRED)
	void changePassword(String userName, String oldPassword, String newPassword);

	@Transactional(propagation = Propagation.REQUIRED)
	User authenticate(String userName, String password);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	String getGroupOwner(String groupName);

	@Transactional(propagation = Propagation.REQUIRED)
	void createUser(User user);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	User getUserByName(String userName);

	@Transactional(propagation = Propagation.REQUIRED)
	void modifyUser(User user);

	@Transactional(propagation = Propagation.REQUIRED)
	void removeUser(String userName);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<User> listUsers(String group);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<String> searchUser(User searchCriteria);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<String> findMembersByGroupDomain(String domainName);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<String> findOwnersByGroupDomain(String domainName);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<String> listUsersByDomain(String domainName);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<UserStatistics> getUserReport(Calendar startDate, Calendar endDate,
			boolean groupByLoginDate);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<VisitorStatistics> getVisitorReport(Calendar startDate,
			Calendar endDate, boolean groupByLoginDate);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<TrafficStatistics> getTrafficReport(Calendar startDate,
			Calendar endDate, String groupName);
}
