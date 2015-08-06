/**
 * 
 */
package com.quikj.ace.db.webtalk.dao;

import org.apache.ibatis.annotations.Param;

import com.quikj.ace.db.core.webtalk.vo.Account;

/**
 * @author amit
 * 
 */
public interface AccountDao {

	public void create(Account account);

	public int modify(Account account);

	public int delete(String userName);

	public Account findByUserName(String userName);

	public int changePassword(@Param("userName") String userName,
			@Param("oldPassword") String oldPassword,
			@Param("newPassword") String newPassword);

	public Account authenticate(@Param("userName") String userName,
			@Param("password") String password);
}
