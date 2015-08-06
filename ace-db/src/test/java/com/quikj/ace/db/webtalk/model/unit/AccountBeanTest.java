/**
 * 
 */
package com.quikj.ace.db.webtalk.model.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.Account;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.model.AccountBean;

/**
 * @author amit
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/META-INF/AoDbSpringBase.xml",
		"/META-INF/AoDbSpringBeans.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class AccountBeanTest {

	@Autowired
	private AccountBean account;

	public void setAccount(AccountBean account) {
		this.account = account;
	}

	@Test
	public void testOperations() {

		Account accountElement = new Account("admin", "password!",
				"System administrator");
		account.create(accountElement);

		Account retAccount = account.findByUserName("admin");
		assertNotNull(retAccount);
		assertEquals(accountElement.getUserName(), retAccount.getUserName());
		assertEquals(accountElement.getAdditionalInfo(),
				retAccount.getAdditionalInfo());
		
		accountElement.setAdditionalInfo("System admin 2");
		account.modify(accountElement);
		retAccount = account.findByUserName("admin");
		assertNotNull(retAccount);
		assertEquals(accountElement.getAdditionalInfo(),
				retAccount.getAdditionalInfo());
		
		// Verify that the modify action above did not change the password
		retAccount = account.authenticate("admin", "password!");
		assertNotNull(retAccount);
		assertEquals(accountElement.getUserName(), retAccount.getUserName());
		assertEquals(accountElement.getAdditionalInfo(),
				retAccount.getAdditionalInfo());
		
		accountElement.setPassword("password2!");
		account.modify(accountElement);		
		retAccount = account.authenticate("admin", "password2!");
		assertNotNull(retAccount);
		
		try {
			account.changePassword("admin", "password!", "password!");
		} catch (WebTalkException e) {
			// Expected
		}
		
		account.changePassword("admin", "password2!", "password!");
		retAccount = account.authenticate("admin", "password!");
		assertNotNull(retAccount);
		
		account.delete("admin");
		retAccount = account.findByUserName("admin");
		assertNull(retAccount);
		
		retAccount = account.authenticate("admin", "password!");
		assertNull(retAccount);
	}
}
