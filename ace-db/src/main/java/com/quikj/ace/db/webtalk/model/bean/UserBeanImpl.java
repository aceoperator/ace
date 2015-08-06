/**
 * 
 */
package com.quikj.ace.db.webtalk.model.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.SecurityQuestion;
import com.quikj.ace.db.core.webtalk.vo.TrafficStatistics;
import com.quikj.ace.db.core.webtalk.vo.User;
import com.quikj.ace.db.core.webtalk.vo.UserStatistics;
import com.quikj.ace.db.core.webtalk.vo.VisitorStatistics;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.dao.UserDao;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.ace.db.webtalk.value.TrafficMeasurement;

/**
 * @author amit
 * 
 */
public class UserBeanImpl implements UserBean {

	private UserDao userDao;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public long getUserId(String userName) {
		try {
			return userDao.getUserId(userName);
		} catch (Exception e) {
			throw new WebTalkException(e);
		}
	}

	@Override
	public List<String> getMemberOfGroupNames(String userName) {
		return userDao.getBelongsToGroups(userName);
	}

	@Override
	public void changePassword(String userName, String oldPassword,
			String newPassword) {
		int affected = userDao.changePassword(userName, oldPassword,
				newPassword);
		if (affected == 0) {
			throw new WebTalkException(
					"The user does not exist or the password entered is invalid");
		}
	}

	@Override
	public User authenticate(String userName, String password) {
		return userDao.authenticate(userName, password);
	}

	@Override
	public String getGroupOwner(String groupName) {
		return userDao.groupOwner(groupName);
	}

	@Override
	public void createUser(User user) {
		userDao.createUser(user);
		long userId = userDao.getUserId(user.getUserName());

		for (SecurityQuestion q : user.getSecurityQuestions()) {
			q.setUserId(userId);
			userDao.createSecurityQuestion(q);
		}

		for (String group : user.getOwnsGroups()) {
			userDao.createGroupOwner(user.getUserName(), group);
		}

		for (String group : user.getMemberOfGroups()) {
			userDao.createGroupMember(user.getUserName(), group);
		}
	}

	@Override
	public User getUserByName(String userName) {
		User user = userDao.getByName(userName);
		if (user == null) {
			throw new WebTalkException("The user not found");
		}

		List<String> belongs = userDao.getBelongsToGroups(userName);
		for (String group : belongs) {
			user.getMemberOfGroups().add(group);
		}

		List<String> owns = userDao.getOwnsGroups(userName);
		for (String group : owns) {
			user.getOwnsGroups().add(group);
		}

		List<SecurityQuestion> questions = userDao
				.getSecurityQuestions(userName);
		for (SecurityQuestion question : questions) {
			user.getSecurityQuestions().add(question);
		}

		return user;
	}

	@Override
	public void modifyUser(User user) {
		User dbUser = getUserByName(user.getUserName());
		if (dbUser == null) {
			throw new WebTalkException("The user was not found");
		}

		if (user.getPassword() != null) {
			userDao.changePassword2(user.getUserName(), user.getPassword());
		}

		userDao.modifyUser(user);

		List<String> newGroups = user.getMemberOfGroups();
		List<String> oldGroups = dbUser.getMemberOfGroups();

		List<String> groupsRemoved = groupDiff(oldGroups, newGroups);
		for (String group : groupsRemoved) {
			userDao.removeGroupMember(user.getUserName(), group);
		}

		List<String> groupsAdded = groupDiff(newGroups, oldGroups);
		for (String group : groupsAdded) {
			userDao.createGroupMember(user.getUserName(), group);
		}

		newGroups = user.getOwnsGroups();
		oldGroups = dbUser.getOwnsGroups();

		groupsRemoved = groupDiff(oldGroups, newGroups);
		for (String group : groupsRemoved) {
			userDao.removeGroupOwner(user.getUserName(), group);
		}

		groupsAdded = groupDiff(newGroups, oldGroups);
		for (String group : groupsAdded) {
			userDao.createGroupOwner(user.getUserName(), group);
		}

		List<SecurityQuestion> newQuestions = user.getSecurityQuestions();
		List<SecurityQuestion> oldQuestions = dbUser.getSecurityQuestions();

		List<SecurityQuestion> questionsRemoved = securityQuestionDiff(
				oldQuestions, newQuestions);
		for (SecurityQuestion question : questionsRemoved) {
			question.setUserId(dbUser.getId());
			userDao.removeSecurityQuestion(question);
		}

		List<SecurityQuestion> questionsAdded = securityQuestionDiff(
				newQuestions, oldQuestions);
		for (SecurityQuestion question : questionsAdded) {
			question.setUserId(dbUser.getId());
			userDao.createSecurityQuestion(question);
		}
	}

	private List<SecurityQuestion> securityQuestionDiff(
			List<SecurityQuestion> questions1, List<SecurityQuestion> questions2) {
		List<SecurityQuestion> questions = new ArrayList<SecurityQuestion>();
		start: for (SecurityQuestion question1 : questions1) {
			for (SecurityQuestion question2 : questions2) {
				if (question1.getQuestionId() == question2.getQuestionId()
						&& question1.getQuestion().equals(
								question2.getQuestion())
						&& question1.getAnswer().equals(question2.getAnswer())) {
					continue start;
				}
			}

			questions.add(question1);
		}

		return questions;
	}

	// What elements are there in group 1 that cannot be found in group 2
	private List<String> groupDiff(List<String> groups1, List<String> groups2) {
		if (groups2.size() == 0) {
			return groups1;
		}

		List<String> groups = new ArrayList<String>();
		start: for (String group1 : groups1) {
			for (String group2 : groups2) {
				if (group1.equals(group2)) {
					continue start;
				}
			}

			groups.add(group1);
		}

		return groups;
	}

	@Override
	public void removeUser(String userName) {
		User dbUser = getUserByName(userName);
		if (dbUser == null) {
			throw new WebTalkException("The user was not found");
		}

		for (String group : dbUser.getOwnsGroups()) {
			userDao.removeGroupOwner(userName, group);
		}

		for (String group : dbUser.getMemberOfGroups()) {
			userDao.removeGroupMember(userName, group);
		}

		int affected = userDao.removeUser(userName);
		if (affected == 0) {
			throw new WebTalkException("The user was not found");
		}
	}

	@Override
	public List<User> listUsers(String group) {
		return userDao.listUsers(group);
	}

	@Override
	public List<String> searchUser(User searchCriteria) {
		return userDao.searchUser(searchCriteria);
	}

	@Override
	public List<String> findMembersByGroupDomain(String domainName) {
		return userDao.findMembersByGroupDomain(domainName);
	}

	@Override
	public List<String> findOwnersByGroupDomain(String domainName) {
		return userDao.findOwnersByGroupDomain(domainName);
	}

	@Override
	public List<String> listUsersByDomain(String domainName) {
		return userDao.listUsersByDomain(domainName);
	}

	@Override
	public List<UserStatistics> getUserReport(Calendar startDate,
			Calendar endDate, boolean groupByLoginDate) {
		return userDao.getUserReport(startDate, endDate, groupByLoginDate);
	}

	@Override
	public List<VisitorStatistics> getVisitorReport(Calendar startDate,
			Calendar endDate, boolean groupByLoginDate) {
		return userDao.getVisitorReport(startDate, endDate, groupByLoginDate);
	}

	@Override
	public List<TrafficStatistics> getTrafficReport(Calendar startDate,
			Calendar endDate, String groupName) {
		String groupOwner = getGroupOwner(groupName);
		if (groupOwner == null) {
			throw new WebTalkException("The group owner for group " + groupName
					+ " not found");
		}

		List<TrafficMeasurement> traffic = userDao.getTrafficReport(startDate,
				endDate, groupOwner);
		List<TrafficStatistics> ret = new ArrayList<TrafficStatistics>(
				traffic.size());
		TrafficStatistics stat = null;
		for (TrafficMeasurement measElement : traffic) {
			if (stat == null
					|| measElement.getTimestamp().getTimeInMillis() != stat
							.getTimestamp().getTimeInMillis()) {
				stat = new TrafficStatistics();
				stat.setTimestamp(measElement.getTimestamp());
				ret.add(stat);
			}

			if (measElement.getParam().equals("actv_ops")) {
				stat.setNumActiveOperators(measElement.getValue());
			} else if (measElement.getParam().equals("users_waiting")) {
				stat.setNumUsersInQueue(measElement.getValue());
			} else if (measElement.getParam().equals("users_talking")) {
				stat.setNumConversations(measElement.getValue());
			}
		}

		return ret;
	}
}
