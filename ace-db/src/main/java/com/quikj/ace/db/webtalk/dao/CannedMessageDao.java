/**
 * 
 */
package com.quikj.ace.db.webtalk.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.quikj.ace.db.core.webtalk.vo.CannedMessage;

/**
 * @author amit
 * 
 */
public interface CannedMessageDao {

	int create(CannedMessage canned);

	int delete(long id);

	int update(CannedMessage canned);

	CannedMessage getById(long id);

	CannedMessage get(@Param("groupName") String groupName,
			@Param("description") String description);
	
	List<CannedMessage> search (CannedMessage search);
}
