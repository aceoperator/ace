/**
 * Copyright 2011-2012 QUIK Computing All rights reserved.
 */
package com.quikj.ace.db.webtalk.dao;

import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.Blacklist;

/**
 * @author amit
 * 
 */
public interface BlackListDao {

	List<Blacklist> getBlacklist(String userName);

	Blacklist getById(long id);

	int createBlacklist(Blacklist blacklist);

	int deleteBlacklist(long id);

	int updateBlacklist(Blacklist blacklist);
}
