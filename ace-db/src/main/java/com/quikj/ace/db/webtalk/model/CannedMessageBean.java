/**
 * 
 */
package com.quikj.ace.db.webtalk.model;

import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.CannedMessage;

/**
 * @author amit
 * 
 */
public interface CannedMessageBean {
	void create(CannedMessage canned);

	void delete(long id);

	void update(CannedMessage canned);

	CannedMessage getById(long id);

	CannedMessage get(String groupName, String description);

	List<CannedMessage> search(CannedMessage search);
}
