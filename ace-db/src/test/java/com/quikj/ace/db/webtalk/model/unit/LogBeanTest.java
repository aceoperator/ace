/**
 * 
 */
package com.quikj.ace.db.webtalk.model.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.Log;
import com.quikj.ace.db.webtalk.model.LogBean;

/**
 * @author amit
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/META-INF/AoDbSpringBase.xml", "/META-INF/AoDbSpringBeans.xml", "/AoDbSpringOverride.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class LogBeanTest {

	@Autowired
	private LogBean log;
	
	public void setLog(LogBean log) {
		this.log = log;
	}

	@Test
	public void testOperations() {
		List<String> processes = log.getUniqueProcessNames();
		assertNotNull(processes);
		
		Calendar date1 = Calendar.getInstance();
		date1.setTimeInMillis(new Date().getTime() - 30 * 24 * 3600 * 1000L);
		Calendar date2 = Calendar.getInstance();
		date2.setTimeInMillis(new Date().getTime());
		List<String> levels = new ArrayList<String>();
		levels.add("INFO");
		levels.add("WARN");
		List<Log> results = log.search(date1, date2, levels, processes, "foo%");
		assertNotNull(results);
		assertEquals(0, results.size());
		
		Calendar date3 = Calendar.getInstance();
		date3.setTimeInMillis(new Date().getTime() - 90 * 24 * 3600 * 1000L);
		log.delete(date3);
	}
}
