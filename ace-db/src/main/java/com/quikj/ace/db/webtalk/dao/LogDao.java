/**
 * 
 */
package com.quikj.ace.db.webtalk.dao;

import java.util.Calendar;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.quikj.ace.db.core.webtalk.vo.Log;

/**
 * @author amit
 * 
 */
public interface LogDao {

	int delete(@Param("before") Calendar before);

	List<String> getUniqueProcessNames();

	List<Log> search(@Param("startDate") Calendar startDate,
			@Param("endDate") Calendar endDate,
			@Param("severityLevels") List<String> severityLevels,
			@Param("processNames") List<String> processNames,
			@Param("message") String message);
}
