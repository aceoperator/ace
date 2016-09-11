/**
 * 
 */
package com.quikj.ace.db.webtalk.model.bean;

import com.quikj.ace.db.core.webtalk.vo.Account;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.dao.AccountDao;
import com.quikj.ace.db.webtalk.model.AccountBean;

/**
 * @author amit
 *
 */
public class AccountBeanImpl implements AccountBean {

	private AccountDao accountDao;
	
	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	@Override
	public void create(Account account) {
		accountDao.create(account);
	}

	@Override
	public void modify(Account account) {
		int affected = accountDao.modify(account);
		if (affected == 0) {
			throw new WebTalkException("The account was not found");
		}
	}

	@Override
	public void delete(String userName) {
		int affected = accountDao.delete(userName);
		if (affected == 0) {
			throw new WebTalkException("The account was not found");
		}
	}

	@Override
	public Account findByUserName(String userName) {
		return accountDao.findByUserName(userName);
	}

	@Override
	public void changePassword(String userName, String oldPassword,
			String newPassword) {
		int affected = accountDao.changePassword(userName, oldPassword, newPassword);
		if (affected == 0) {
			throw new WebTalkException("The account was not found or the password did not match");
		}
	}

	@Override
	public Account authenticate(String userName, String password) {
		Account auth = accountDao.authenticate(userName, password);
		if (auth == null){
			throw new WebTalkException("Authentication failed");
		}
		return auth;
	}
}
