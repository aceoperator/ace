/**
 * 
 */
package com.quikj.ace.db.webtalk.model.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.SecurityQuestion;
import com.quikj.ace.db.core.webtalk.vo.User;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.dao.UserDao;
import com.quikj.ace.db.webtalk.model.UserBean;

/**
 * @author amit
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/META-INF/AoDbSpringBase.xml",
		"/META-INF/AoDbSpringBeans.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class UserBeanTest {

	@Autowired
	private UserBean userBean;

	@Autowired
	private UserDao userDao;

	public UserBeanTest() {
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	@Test
	public void testAuthenticationOperations() {
		User user = new User();
		user.setUserName("testUser");
		user.setPassword("testPassword");
		user.setFullName("Test User");
		userDao.replace(user);

		user = userBean.authenticate("testUser", "testPassword");
		assertNotNull(user);
		assertEquals("testUser", user.getUserName());
		assertNull(user.getPassword());
		assertEquals("Test User", user.getFullName());

		user = new User();
		user.setUserName("testUser2");
		user.setPassword("testPassword2");
		user.setFullName("Test User II");
		userDao.replace(user);
		user = userBean.authenticate("testUser2", "testPassword2");
		assertNotNull(user);

		user = userBean.authenticate("testUser", "testPassword2");
		assertNull(user);

		user = userBean.authenticate("testUser3", "testPassword3");
		assertNull(user);

		user = new User();
		user.setUserName("testLockedUser");
		user.setPassword("testPassword");
		user.setFullName("Test Locked User");
		user.setLocked(true);
		userDao.replace(user);
		User lockedUser = userBean.authenticate("testLockedUser",
				"testPassword");
		assertNull(lockedUser);

		user.setLocked(false);
		userDao.replace(user);
		user = userBean.authenticate("testLockedUser", "testPassword");
		assertNotNull(user);
	}

	@Test
	public void testChangePassword() throws InterruptedException {
		User user = new User();
		user.setUserName("testUser");
		user.setPassword("testPassword");
		user.setFullName("Test User");
		user.setFlags(1);
		userDao.replace(user);

		userDao.replaceGroup("testGroup1", "domain");
		userDao.replaceUserGroupAssociation("testUser", "testGroup1");

		User dbUser = userBean.getUserByName("testUser");
		assertNotNull(dbUser);

		try {
			userBean.changePassword("testUser", "wrongPassword", "newPassword");
			fail();
		} catch (WebTalkException e) {
			// expected
		}

		try {
			userBean.changePassword("testUser1", "testPassword", "newPassword");
			fail();
		} catch (WebTalkException e) {
			// expected
		}

		// Sleep for some time so that there is a noticeable difference in the
		// number of seconds that we can measure
		Thread.sleep(1000L);

		userBean.changePassword("testUser", "testPassword", "newPassword");

		user = userBean.authenticate("testUser", "newPassword");
		assertNotNull(user);

		User dbUser2 = userBean.getUserByName("testUser");
		assertNotNull(dbUser2);
		assertTrue(dbUser2.getPasswordUpdated().getTimeInMillis() != dbUser
				.getPasswordUpdated().getTimeInMillis());
	}

	@Test
	public void testClearChangePassword() {
		User user = new User();
		user.setUserName("testUser");
		user.setPassword("testPassword");
		user.setFullName("Test User");
		user.setFlags(1);
		user.setChangePassword(true);
		userDao.replace(user);

		try {
			userBean.changePassword("testUser", "wrongPassword", "newPassword");
			fail();
		} catch (WebTalkException e) {
			// expected
		}
		assertTrue(userDao.getByName("testUser").isChangePassword());

		userBean.changePassword("testUser", "testPassword", "newPassword");
		assertFalse(userDao.getByName("testUser").isChangePassword());

		user = userBean.authenticate("testUser", "newPassword");
		assertNotNull(user);
	}

	@Test
	public void testCreateUser() {
		User user = createUser("testUser", true, true);

		user = userBean.authenticate("testUser", "testPassword");
		assertNotNull(user);
	}

	@Test
	public void testDeleteUser() {
		createUser("testUser", true, true);
		userBean.removeUser("testUser");

		try {
			userBean.getUserByName("testUser");
			fail();
		} catch (WebTalkException e) {
			// Expected
		}
	}

	@Test
	public void testModifyUser() throws InterruptedException {
		User user = createUser("testUser", true, true);

		Calendar savedPasswordUpdated = user.getPasswordUpdated();
		assertNotNull(savedPasswordUpdated);

		Thread.sleep(1000L);

		userDao.replaceGroup("testGroup5", "domain");
		userDao.replaceGroup("testGroup6", "domain");
		userDao.replaceGroup("testGroup7", "domain");
		userDao.replaceGroup("testGroup8", "domain");

		user.setPassword("testPasswordChanged");
		user.setFullName("Test UserChanged");
		user.setAdditionalInfo("Additional information Changed");
		user.setEmail("useremailChanged@domain.com");
		user.setGatekeeper("gatekeeperChanged");
		user.setUnavailableTransferTo("messageboxChanged");
		user.setFlags(2);
		user.setAvatar("avatarChanged");
		user.setPrivateInfo(false);

		user.getOwnsGroups().clear();
		user.getMemberOfGroups().clear();
		user.getOwnsGroups().add("testGroup5");
		user.getMemberOfGroups().add("testGroup6");
		user.getMemberOfGroups().add("testGroup7");
		user.getMemberOfGroups().add("testGroup8");

		user.getSecurityQuestions().clear();
		SecurityQuestion q = new SecurityQuestion(0L, 0L, 1,
				"question1 changed", "answer1 changed");
		user.getSecurityQuestions().add(q);

		q = new SecurityQuestion(0L, 0L, 2, "question2 changed",
				"answer2 changed");
		user.getSecurityQuestions().add(q);

		q = new SecurityQuestion(0L, 0L, 3, "question3 changed",
				"answer3 changed");
		user.getSecurityQuestions().add(q);

		userBean.modifyUser(user);

		user = userBean.getUserByName("testUser");
		assertNotNull(user);

		assertEquals("testUser", user.getUserName());
		assertEquals("Test UserChanged", user.getFullName());
		assertEquals("Additional information Changed", user.getAdditionalInfo());
		assertEquals("useremailChanged@domain.com", user.getEmail());
		assertEquals("gatekeeperChanged", user.getGatekeeper());
		assertEquals("messageboxChanged", user.getUnavailableTransferTo());
		assertEquals(2, user.getFlags());
		assertEquals("avatarChanged", user.getAvatar());
		assertFalse(user.isPrivateInfo());
		assertNotNull(user.getPasswordUpdated());
		assertFalse(savedPasswordUpdated.getTimeInMillis() == user
				.getPasswordUpdated().getTimeInMillis());

		assertEquals(1, user.getOwnsGroups().size());
		assertEquals("testGroup5", user.getOwnsGroups().get(0));

		assertEquals(3, user.getMemberOfGroups().size());
		Collections.sort(user.getMemberOfGroups());
		assertEquals("testGroup6", user.getMemberOfGroups().get(0));
		assertEquals("testGroup7", user.getMemberOfGroups().get(1));
		assertEquals("testGroup8", user.getMemberOfGroups().get(2));

		assertEquals(3, user.getSecurityQuestions().size());
		Collections.sort(user.getSecurityQuestions(),
				new Comparator<SecurityQuestion>() {
					@Override
					public int compare(SecurityQuestion o1, SecurityQuestion o2) {
						return o1.getQuestionId() - o2.getQuestionId();
					}
				});

		assertEquals(1, user.getSecurityQuestions().get(0).getQuestionId());
		assertEquals("question1 changed", user.getSecurityQuestions().get(0)
				.getQuestion());
		assertEquals("answer1 changed", user.getSecurityQuestions().get(0)
				.getAnswer());

		assertEquals(2, user.getSecurityQuestions().get(1).getQuestionId());
		assertEquals("question2 changed", user.getSecurityQuestions().get(1)
				.getQuestion());
		assertEquals("answer2 changed", user.getSecurityQuestions().get(1)
				.getAnswer());

		assertEquals(3, user.getSecurityQuestions().get(2).getQuestionId());
		assertEquals("question3 changed", user.getSecurityQuestions().get(2)
				.getQuestion());
		assertEquals("answer3 changed", user.getSecurityQuestions().get(2)
				.getAnswer());

		user = userBean.authenticate("testUser", "testPasswordChanged");
		assertNotNull(user);

		user = userBean.getUserByName("testUser");
		assertNotNull(user);

		user.getMemberOfGroups().clear();
		user.getOwnsGroups().clear();
		userBean.modifyUser(user);

		user = userBean.getUserByName("testUser");
		assertNotNull(user);
		assertEquals(0, user.getOwnsGroups().size());
		assertEquals(0, user.getMemberOfGroups().size());

		user.setLocked(true);
		user.setChangePassword(false);
		userBean.modifyUser(user);

		user = userBean.getUserByName("testUser");
		assertNotNull(user);
		assertTrue(user.isLocked());
		assertFalse(user.isChangePassword());
	}

	private User createUser(String userName, boolean createGroups,
			boolean ownsGroup) {

		if (createGroups) {
			userDao.replaceGroup("testGroup1", "domain");
			userDao.replaceGroup("testGroup2", "domain");
			userDao.replaceGroup("testGroup3", "domain");
			userDao.replaceGroup("testGroup4", "domain");
		}

		User user = new User();
		user.setUserName(userName);
		user.setPassword("testPassword");
		user.setFullName("Test " + userName);
		user.setAdditionalInfo("Additional information for " + userName);
		user.setEmail(userName + "@domain.com");
		user.setGatekeeper("gatekeeper");
		user.setUnavailableTransferTo("messagebox");
		user.setFlags(1);
		user.setAvatar("avatar");
		user.setDomain("domain");
		user.setChangePassword(true);
		user.setLocked(false);
		user.setPrivateInfo(true);

		if (ownsGroup) {
			user.getOwnsGroups().add("testGroup1");
		}

		user.getMemberOfGroups().add("testGroup2");
		user.getMemberOfGroups().add("testGroup3");
		user.getMemberOfGroups().add("testGroup4");

		SecurityQuestion q = new SecurityQuestion(0L, 0L, 1, "question1",
				"answer1");
		user.getSecurityQuestions().add(q);

		q = new SecurityQuestion(0L, 0L, 2, "question2", "answer2");
		user.getSecurityQuestions().add(q);

		q = new SecurityQuestion(0L, 0L, 3, "question3", "answer3");
		user.getSecurityQuestions().add(q);

		userBean.createUser(user);

		user = userBean.getUserByName(userName);
		assertNotNull(user);

		assertEquals(userName, user.getUserName());
		assertEquals("Test " + userName, user.getFullName());
		assertEquals("Additional information for " + userName,
				user.getAdditionalInfo());
		assertEquals(userName + "@domain.com", user.getEmail());
		assertEquals("gatekeeper", user.getGatekeeper());
		assertEquals("messagebox", user.getUnavailableTransferTo());
		assertEquals(1, user.getFlags());
		assertEquals("avatar", user.getAvatar());
		assertEquals("domain", user.getDomain());
		assertTrue(user.isChangePassword());
		assertFalse(user.isLocked());
		assertTrue(user.isPrivateInfo());
		assertNotNull(user.getPasswordUpdated());

		if (ownsGroup) {
			assertEquals(1, user.getOwnsGroups().size());
			assertEquals("testGroup1", user.getOwnsGroups().get(0));
		} else {
			assertEquals(0, user.getOwnsGroups().size());
		}

		assertEquals(3, user.getMemberOfGroups().size());
		Collections.sort(user.getMemberOfGroups());
		assertEquals("testGroup2", user.getMemberOfGroups().get(0));
		assertEquals("testGroup3", user.getMemberOfGroups().get(1));
		assertEquals("testGroup4", user.getMemberOfGroups().get(2));

		assertEquals(3, user.getSecurityQuestions().size());
		Collections.sort(user.getSecurityQuestions(),
				new Comparator<SecurityQuestion>() {
					@Override
					public int compare(SecurityQuestion o1, SecurityQuestion o2) {
						return o1.getQuestionId() - o2.getQuestionId();
					}
				});

		assertEquals(1, user.getSecurityQuestions().get(0).getQuestionId());
		assertEquals("question1", user.getSecurityQuestions().get(0)
				.getQuestion());
		assertEquals("answer1", user.getSecurityQuestions().get(0).getAnswer());

		assertEquals(2, user.getSecurityQuestions().get(1).getQuestionId());
		assertEquals("question2", user.getSecurityQuestions().get(1)
				.getQuestion());
		assertEquals("answer2", user.getSecurityQuestions().get(1).getAnswer());

		assertEquals(3, user.getSecurityQuestions().get(2).getQuestionId());
		assertEquals("question3", user.getSecurityQuestions().get(2)
				.getQuestion());
		assertEquals("answer3", user.getSecurityQuestions().get(2).getAnswer());

		return user;
	}

	@Test
	public void testListUserOperation() {
		createUser("testUser1", true, false);
		createUser("testUser2", false, false);

		List<User> list = userBean.listUsers("testGroup2");
		assertNotNull(list);
		assertEquals(2, list.size());

		Collections.sort(list, new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				return o1.getUserName().compareTo(o2.getUserName());
			}
		});

		assertEquals("testUser1", list.get(0).getUserName());
		assertEquals("Test testUser1", list.get(0).getFullName());
		assertEquals("Additional information for testUser1", list.get(0)
				.getAdditionalInfo());
		assertEquals("avatar", list.get(0).getAvatar());
		assertEquals("testUser1@domain.com", list.get(0).getEmail());

		assertEquals("testUser2", list.get(1).getUserName());
		assertEquals("Test testUser2", list.get(1).getFullName());
		assertEquals("Additional information for testUser2", list.get(1)
				.getAdditionalInfo());
		assertEquals("avatar", list.get(1).getAvatar());
		assertEquals("testUser2@domain.com", list.get(1).getEmail());

		list = userBean.listUsers("testGroup3");
		assertNotNull(list);
		assertEquals(2, list.size());

		list = userBean.listUsers("testGroup4");
		assertNotNull(list);
		assertEquals(2, list.size());
	}

	@Test
	public void testSearchUserOperation() {
		createUser("testUser1", true, true);
		createUser("testUser2", false, false);

		User search = new User();
		search.setUserName("testUser%");
		List<String> users = userBean.searchUser(search);
		assertNotNull(users);
		assertEquals(2, users.size());
		assertEquals("testUser1", users.get(0));
		assertEquals("testUser2", users.get(1));

		search.setFullName("Test testUser1");
		users = userBean.searchUser(search);
		assertNotNull(users);
		assertEquals(1, users.size());
		assertEquals("testUser1", users.get(0));

		search.setUserName(null);
		search.setFullName("%testUser%");
		users = userBean.searchUser(search);
		assertNotNull(users);
		assertEquals(2, users.size());
		assertEquals("testUser1", users.get(0));
		assertEquals("testUser2", users.get(1));

		search.setEmail("testUser1@domain.com");
		users = userBean.searchUser(search);
		assertNotNull(users);
		assertEquals(1, users.size());
		assertEquals("testUser1", users.get(0));

		search.setEmail("testUser%@domain.com");
		users = userBean.searchUser(search);
		assertNotNull(users);
		assertEquals(2, users.size());
		assertEquals("testUser1", users.get(0));
		assertEquals("testUser2", users.get(1));

		search.setAdditionalInfo("Additional information for testUser1");
		users = userBean.searchUser(search);
		assertNotNull(users);
		assertEquals(1, users.size());
		assertEquals("testUser1", users.get(0));

		search.setAdditionalInfo("Additional information for testUser%");
		users = userBean.searchUser(search);
		assertEquals(2, users.size());
		assertEquals("testUser1", users.get(0));
		assertEquals("testUser2", users.get(1));

		search.setUnavailableTransferTo("message");
		users = userBean.searchUser(search);
		assertEquals(0, users.size());

		search.setUnavailableTransferTo("message%");
		users = userBean.searchUser(search);
		assertEquals(2, users.size());
		assertEquals("testUser1", users.get(0));
		assertEquals("testUser2", users.get(1));

		search.setGatekeeper("gate");
		users = userBean.searchUser(search);
		assertEquals(0, users.size());

		search.setGatekeeper("gate%");
		users = userBean.searchUser(search);
		assertEquals(2, users.size());
		assertEquals("testUser1", users.get(0));
		assertEquals("testUser2", users.get(1));

		search.setAvatar("noavatar");
		users = userBean.searchUser(search);
		assertEquals(0, users.size());

		search.setAvatar("avatar");
		users = userBean.searchUser(search);
		assertEquals(2, users.size());
		assertEquals("testUser1", users.get(0));
		assertEquals("testUser2", users.get(1));

		search.getMemberOfGroups().add("nogroup");
		users = userBean.searchUser(search);
		assertEquals(0, users.size());

		search.getMemberOfGroups().clear();
		search.getMemberOfGroups().add("testGroup2");
		users = userBean.searchUser(search);
		assertEquals(2, users.size());
		assertEquals("testUser1", users.get(0));
		assertEquals("testUser2", users.get(1));

		search.getMemberOfGroups().add("testGroup5");
		users = userBean.searchUser(search);
		assertEquals(2, users.size());
		assertEquals("testUser1", users.get(0));
		assertEquals("testUser2", users.get(1));

		search.getOwnsGroups().add("nogroup");
		users = userBean.searchUser(search);
		assertEquals(0, users.size());

		search.getOwnsGroups().clear();
		search.getOwnsGroups().add("testGroup1");
		users = userBean.searchUser(search);
		assertEquals(1, users.size());
		assertEquals("testUser1", users.get(0));

		search.setSearchLocked(false);
		search.setSearchChangePassword(true);
		users = userBean.searchUser(search);
		assertEquals(1, users.size());
		assertEquals("testUser1", users.get(0));

		search.setSearchChangePassword(false);
		users = userBean.searchUser(search);
		assertEquals(0, users.size());

		search.setSearchChangePassword(true);
		search.setSearchLocked(true);
		users = userBean.searchUser(search);
		assertEquals(0, users.size());

		User user = userBean.getUserByName("testUser1");
		user.setLocked(true);
		userBean.modifyUser(user);
		users = userBean.searchUser(search);
		assertEquals(1, users.size());
		assertEquals("testUser1", users.get(0));
	}

	@Test
	public void testDomainOperations() {
		createUser("testUser1", true, true);
		createUser("testUser2", false, false);

		List<String> users = userBean.findMembersByGroupDomain("domain");
		assertNotNull(users);
		assertEquals(1, users.size());
		assertEquals("testUser2", users.get(0));

		users = userBean.findOwnersByGroupDomain("domain");
		assertNotNull(users);
		assertEquals(1, users.size());
		assertEquals("testUser1", users.get(0));

		users = userBean.listUsersByDomain("domain");
		assertNotNull(users);
		assertEquals(2, users.size());
		assertEquals("testUser1", users.get(0));
		assertEquals("testUser2", users.get(1));
	}
}
