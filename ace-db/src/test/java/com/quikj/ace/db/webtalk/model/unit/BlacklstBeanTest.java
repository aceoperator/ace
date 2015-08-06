/**
 * 
 */
package com.quikj.ace.db.webtalk.model.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.Blacklist;
import com.quikj.ace.db.webtalk.dao.UserDao;
import com.quikj.ace.db.webtalk.model.BlacklistBean;

/**
 * @author amit
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/META-INF/AoDbSpringBase.xml", "/META-INF/AoDbSpringBeans.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class BlacklstBeanTest {

	@Autowired
	private BlacklistBean blacklist;

	@Autowired
	private UserDao userDao;
	
	public void setBlacklist(BlacklistBean blacklist) {
		this.blacklist = blacklist;
	}
	
	@Before
	public void init() {
		List<Blacklist> list = blacklist.list("operator");
		for (Blacklist e: list) {
			blacklist.delete(e.getId());
		}
	}

	@Test
	public void testOperations() {
		
		List<Blacklist> list = blacklist.list("operator");
		assertNotNull(list);
		assertEquals(0, list.size());

		long userId = userDao.getUserId("operator");

		Blacklist bl = new Blacklist();
		bl.setUserId(userId);
		bl.setIdentifier("cookie1");
		bl.setLevel(10);

		try {
			blacklist.create(bl);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		list = blacklist.list("operator");
		assertNotNull(list);
		assertEquals(1, list.size());

		bl = list.get(0);
		assertEquals("cookie1", bl.getIdentifier());
		assertEquals(10, bl.getLevel());
		assertNotNull(bl.getLastModified());
		assertEquals(userId, bl.getUserId().longValue());
		assertTrue(bl.getId() > 0);

		long id = bl.getId();
		
		try {
			bl = blacklist.get(id);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		assertNotNull(bl);
		assertEquals("cookie1", bl.getIdentifier());
		assertEquals(10, bl.getLevel());
		assertNotNull(bl.getLastModified());
		assertEquals(userId, bl.getUserId().longValue());
		
		bl.setId(id);
		bl.setLevel(20);
		try {
			blacklist.modify(bl);
			bl = blacklist.get(id);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		assertNotNull(bl);
		assertEquals("cookie1", bl.getIdentifier());
		assertEquals(20, bl.getLevel());
		assertNotNull(bl.getLastModified());
		assertNotNull(bl.getLastModified());
		assertEquals(userId, bl.getUserId().longValue());
		
		try {
			blacklist.delete(id);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		list = blacklist.list("operator");
		assertNotNull(list);
		assertEquals(0, list.size());
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
}
