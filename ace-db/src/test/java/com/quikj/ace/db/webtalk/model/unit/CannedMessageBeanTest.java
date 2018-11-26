/**
 * 
 */
package com.quikj.ace.db.webtalk.model.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.CannedMessage;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.dao.UserDao;
import com.quikj.ace.db.webtalk.model.CannedMessageBean;

/**
 * @author amit
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/META-INF/AoDbSpringBase.xml",
		"/META-INF/AoDbSpringBeans.xml" , "/AoDbSpringOverride.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class CannedMessageBeanTest {

	@Autowired
	private UserDao userDao;

	@Autowired
	private CannedMessageBean cannedMessage;

	public void setCannedMessage(CannedMessageBean cannedMessage) {
		this.cannedMessage = cannedMessage;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Test
	public void testCRUDOperations() {
		userDao.replaceGroup("testGroup1", "domain");
		userDao.replaceGroup("testGroup2", "domain");

		CannedMessage canned = new CannedMessage(0L, "testGroup1",
				"canned message 1", "Canned message 1 HTML");
		cannedMessage.create(canned);

		canned = cannedMessage.get("testGroup1", "canned message 1");
		assertNotNull(canned);
		assertTrue(canned.getId() > 0);
		assertEquals("testGroup1", canned.getGroupName());
		assertEquals("canned message 1", canned.getDescription());
		assertEquals("Canned message 1 HTML", canned.getMessage());

		// Create a canned message for all groups
		canned = new CannedMessage(0L, null, "canned message 2",
				"Canned message 2 HTML");
		cannedMessage.create(canned);

		canned = cannedMessage.get("testGroup1", "canned message 2");
		assertNotNull(canned);

		canned = cannedMessage.get("testGroup2", "canned message 2");
		assertNotNull(canned);
		assertTrue(canned.getId() > 0);
		assertNull(canned.getGroupName());
		assertEquals("canned message 2", canned.getDescription());
		assertEquals("Canned message 2 HTML", canned.getMessage());

		// Try creating duplicates
		try {
			canned = new CannedMessage(0L, "testGroup1", "canned message 1",
					"Canned message 1 HTML");
			cannedMessage.create(canned);
			fail();
		} catch (DuplicateKeyException e) {
			// Expected
		}

		// Modify
		canned = cannedMessage.get("testGroup2", "canned message 2");
		canned.setGroupName("testGroup2");
		canned.setDescription("canned message 2 changed");
		canned.setMessage("canned message 2 HTML changed");
		cannedMessage.update(canned);

		canned = cannedMessage.get("testGroup2", "canned message 2 changed");
		assertNotNull(canned);

		canned = cannedMessage.get("testGroup1", "canned message 2 changed");
		assertNull(canned);

		// Get by ID
		canned = cannedMessage.get("testGroup2", "canned message 2 changed");
		canned = cannedMessage.getById(canned.getId());
		assertNotNull(canned);
		assertTrue(canned.getId() > 0);
		assertEquals("testGroup2", canned.getGroupName());
		assertEquals("canned message 2 changed", canned.getDescription());
		assertEquals("canned message 2 HTML changed", canned.getMessage());

		long savedId = canned.getId();

		canned = cannedMessage.getById(0L);
		assertNull(canned);

		cannedMessage.delete(savedId);
		canned = cannedMessage.get("testGroup2", "canned message 2 changed");
		assertNull(canned);

		try {
			cannedMessage.delete(0L);
		} catch (WebTalkException e) {
			// Expected
		}
	}

	@Test
	public void testSearchOperation() {
		userDao.replaceGroup("testGroup1", "domain");
		userDao.replaceGroup("testGroup2", "domain");

		CannedMessage canned = new CannedMessage(0L, "testGroup1",
				"canned message 1", "Canned message 1 HTML");
		cannedMessage.create(canned);

		canned = new CannedMessage(0L, "testGroup2", "canned message 2",
				"Canned message 2 HTML");
		cannedMessage.create(canned);

		CannedMessage searchCriteria = new CannedMessage(0L, "testGroup%",
				null, null);
		List<CannedMessage> list = cannedMessage.search(searchCriteria);
		assertNotNull(list);
		assertEquals(2, list.size());

		searchCriteria.setGroupName(null);
		list = cannedMessage.search(searchCriteria);
		assertNotNull(list);
		assertTrue(list.size() > 2);

		searchCriteria.setDescription(null);
		searchCriteria.setMessage("%HTML%");
		list = cannedMessage.search(searchCriteria);
		assertNotNull(list);
		assertEquals(2, list.size());

		searchCriteria = new CannedMessage(0L, "testGroup%",
				"canned message %", "%HTML%");
		list = cannedMessage.search(searchCriteria);
		assertNotNull(list);
		assertEquals(2, list.size());

		searchCriteria = new CannedMessage(0L, "testGroup3",
				"canned message %", "%HTML%");
		list = cannedMessage.search(searchCriteria);
		assertNotNull(list);
		assertEquals(0, list.size());

		canned = new CannedMessage(0L, null, "canned message 3",
				"Canned message 3 HTML");
		cannedMessage.create(canned);

		searchCriteria = new CannedMessage(0L, null, "canned message %",
				"%HTML%");
		list = cannedMessage.search(searchCriteria);
		assertNotNull(list);
		assertEquals(3, list.size());
		
		Collections.sort(list, new Comparator<CannedMessage>() {
			@Override
			public int compare(CannedMessage o1, CannedMessage o2) {
				return o1.getDescription().compareTo(o2.getDescription());
			}
		});
		
		assertTrue(list.get(0).getId() > 0);
		assertEquals("canned message 1", list.get(0).getDescription());
		assertEquals("testGroup1", list.get(0).getGroupName());
	}
}
