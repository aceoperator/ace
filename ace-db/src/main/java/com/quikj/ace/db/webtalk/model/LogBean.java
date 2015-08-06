/**
 * 
 */
package com.quikj.ace.db.webtalk.model;

import java.util.Calendar;
import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.Log;

/**
 * @author amit
 * 
 */
public interface LogBean {
	int delete(Calendar before);

	List<String> getUniqueProcessNames();

	List<Log> search(Calendar startDate, Calendar endDate,
			List<String> severityLevels, List<String> processNames,
			String message);
}
