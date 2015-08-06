/**
 * 
 */
package com.quikj.ace.db.webtalk.model.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.CannedMessage;
import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.core.webtalk.vo.User;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.model.CannedMessageBean;
import com.quikj.ace.db.webtalk.model.GroupBean;
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
public class GroupBeanTest {

	@Autowired
	private GroupBean groupBean;

	@Autowired
	private UserBean userBean;

	@Autowired
	private CannedMessageBean cannedMessageBean;

	public void setCannedMessageBean(CannedMessageBean cannedMessageBean) {
		this.cannedMessageBean = cannedMessageBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public void setGroupBean(GroupBean groupBean) {
		this.groupBean = groupBean;
	}

	@Test
	public void testGroupOperations() {

		Group group = new Group("testGroup", null, GroupBean.NOTIFY_ALL,
				GroupBean.NOTIFY_ALL, GroupBean.NOTIFY_MEMBERS,
				GroupBean.NOTIFY_NONE);

		groupBean.createGroup(group);

		Group retGroup = groupBean.findByName("testGroup");
		assertGroup(group, retGroup);

		group.setDomain("testDomain");
		group.setMemberBusyNotification(GroupBean.NOTIFY_MEMBERS);
		group.setMemberLoginNotification(GroupBean.NOTIFY_MEMBERS);
		group.setOwnerBusyNotification(GroupBean.NOTIFY_ALL);
		group.setOwnerLoginNotification(GroupBean.NOTIFY_ALL);
		groupBean.modifyGroup(group);

		retGroup = groupBean.findByName("testGroup");
		assertGroup(group, retGroup);

		List<Group> list = groupBean.listGroups("testDomain");
		assertNotNull(list);
		assertEquals(1, list.size());
		assertGroup(group, list.get(0));

		List<String> domains = groupBean.listDomains();
		assertNotNull(domains);
		assertTrue(domains.size() > 0);
		assertTrue(domains.contains("testDomain"));

		list = groupBean.listGroups(null);
		assertNotNull(list);
		assertTrue(list.size() > 0);

		boolean found = false;
		for (Group grp : list) {
			if (grp.getName().equals("testGroup")) {
				found = true;
				break;
			}
		}

		if (!found) {
			fail();
		}

		User user = new User(0L, "testUser", "Test User", "test@quik-j.com",
				"Test User", null, null, null, null, 0, false, false, false,
				null, null, null, null, new ArrayList<String>(), null, null,
				null);
		user.getMemberOfGroups().add("testGroup");
		userBean.createUser(user);

		User owner = new User(0L, "testOwner", "Test Owner",
				"owner@quik-j.com", "Test Owner", null, null, null, null, 0,
				false, false, false, null, null, null, null,
				new ArrayList<String>(), null, null, null);
		user.getOwnsGroups().add("testGroup");
		userBean.createUser(owner);

		CannedMessage canned = new CannedMessage(0L, "testGroup", "Message 1",
				"Hello user");
		cannedMessageBean.create(canned);

		groupBean.deleteGroup("testGroup");
		list = groupBean.listGroups("testDomain");
		assertNotNull(list);
		assertEquals(0, list.size());

		try {
			groupBean.findByName("testGroup");
		} catch (WebTalkException e) {
			// Expected
		}

		// Test for cascaded deletes
		User retUser = userBean.getUserByName("testUser");
		assertNotNull(retUser);
		assertEquals(0, retUser.getMemberOfGroups().size());

		retUser = userBean.getUserByName("testOwner");
		assertNotNull(retUser);
		assertEquals(0, retUser.getOwnsGroups().size());

		CannedMessage retCanned = cannedMessageBean.get("testGroup",
				"Message 1");
		assertNull(retCanned);
	}

	private void assertGroup(Group group, Group retGroup) {
		assertEquals(group.getName(), retGroup.getName());
		assertEquals(group.getDomain(), retGroup.getDomain());
		assertEquals(group.getMemberBusyNotification(),
				retGroup.getMemberBusyNotification());
		assertEquals(group.getMemberLoginNotification(),
				retGroup.getMemberLoginNotification());
		assertEquals(group.getOwnerBusyNotification(),
				retGroup.getOwnerBusyNotification());
		assertEquals(group.getOwnerLoginNotification(),
				retGroup.getOwnerLoginNotification());
	}
}
