/**
 * 
 */
package com.quikj.ace.db.webtalk.model.bean;

import java.util.Date;
import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.Blacklist;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.dao.BlackListDao;
import com.quikj.ace.db.webtalk.model.BlacklistBean;

/**
 * @author amit
 *
 */
public class BlacklistBeanImpl implements BlacklistBean {

	private BlackListDao blackListDao;
	
	@Override
	public void create(Blacklist blacklist) {
		blacklist.setLastModified(new Date().getTime());
		int affected = blackListDao.createBlacklist(blacklist);
		if (affected == 0) {
			throw new WebTalkException("The entry could not be created");
		}
	}

	@Override
	public List<Blacklist> list(String userName) {
		return blackListDao.getBlacklist(userName);
	}

	@Override
	public void modify(Blacklist blacklist) {
		blacklist.setLastModified(new Date().getTime());
		int affected = blackListDao.updateBlacklist(blacklist);
		if (affected == 0) {
			throw new WebTalkException("The entry could not be found");
		}
	}

	@Override
	public void delete(long id) {
		int affected = blackListDao.deleteBlacklist(id);
		if (affected == 0) {
			throw new WebTalkException("The entry could not be found");
		}
	}

	public void setBlackListDao(BlackListDao blackListDao) {
		this.blackListDao = blackListDao;
	}

	@Override
	public Blacklist get(long id) {
		return blackListDao.getById(id);
	}
}
