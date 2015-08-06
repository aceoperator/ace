/**
 * 
 */
package com.quikj.ace.db.webtalk.model.bean;

import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.CannedMessage;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.dao.CannedMessageDao;
import com.quikj.ace.db.webtalk.model.CannedMessageBean;

/**
 * @author amit
 * 
 */
public class CannedMessageBeanImpl implements CannedMessageBean {

	private CannedMessageDao cannedMessageDao;

	public void setCannedMessageDao(CannedMessageDao cannedMessageDao) {
		this.cannedMessageDao = cannedMessageDao;
	}

	@Override
	public void create(CannedMessage canned) {
		cannedMessageDao.create(canned);
	}

	@Override
	public void delete(long id) {
		int affected = cannedMessageDao.delete(id);
		if (affected == 0) {
			throw new WebTalkException("The canned message could not be found");
		}
	}

	@Override
	public void update(CannedMessage canned) {
		int affected = cannedMessageDao.update(canned);
		if (affected == 0) {
			throw new WebTalkException("The canned message could not be found");
		}
	}

	@Override
	public CannedMessage getById(long id) {
		return cannedMessageDao.getById(id);
	}

	@Override
	public CannedMessage get(String groupName, String description) {
		return cannedMessageDao.get(groupName, description);
	}

	@Override
	public List<CannedMessage> search(CannedMessage search) {
		return cannedMessageDao.search(search);
	}
}
