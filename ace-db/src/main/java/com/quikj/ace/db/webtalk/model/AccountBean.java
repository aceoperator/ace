/**
 * 
 */
package com.quikj.ace.db.webtalk.model;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.Account;

/**
 * @author amit
 * 
 */
public interface AccountBean {

	@Transactional(propagation = Propagation.REQUIRED)
	void create(Account account);

	@Transactional(propagation = Propagation.REQUIRED)
	void modify(Account account);

	@Transactional(propagation = Propagation.REQUIRED)
	void delete(String userName);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	Account findByUserName(String userName);

	@Transactional(propagation = Propagation.REQUIRED)
	void changePassword(String userName, String oldPassword,
			String newPassword);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	Account authenticate(String userName, String password);
}
