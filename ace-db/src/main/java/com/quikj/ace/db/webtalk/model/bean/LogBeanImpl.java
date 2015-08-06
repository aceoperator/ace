/**
 * 
 */
package com.quikj.ace.db.webtalk.model.bean;

import java.util.Calendar;
import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.Log;
import com.quikj.ace.db.webtalk.dao.LogDao;
import com.quikj.ace.db.webtalk.model.LogBean;

/**
 * @author amit
 * 
 */
public class LogBeanImpl implements LogBean {

	private LogDao logDao;

	public void setLogDao(LogDao logDao) {
		this.logDao = logDao;
	}

	@Override
	public int delete(Calendar before) {
		return logDao.delete(before);
	}

	@Override
	public List<String> getUniqueProcessNames() {
		return logDao.getUniqueProcessNames();
	}

	@Override
	public List<Log> search(Calendar startDate, Calendar endDate,
			List<String> severityLevels, List<String> processNames,
			String message) {
		return logDao.search(startDate, endDate, severityLevels, processNames,
				message);
	}
}
