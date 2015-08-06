/**
 * 
 */
package com.quikj.application.communicator.framework;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import com.quikj.ace.db.core.webtalk.vo.Account;
import com.quikj.ace.db.webtalk.model.AccountBean;
import com.quikj.server.framework.AceLogger;

/**
 * @author Amit Chatterjee
 * 
 */
public class AceAdminAuthenticationProvider implements AuthenticationProvider {

	private AccountBean account;

	public void setAccount(AccountBean account) {
		this.account = account;
	}

	@Override
	public Authentication authenticate(Authentication auth)
			throws AuthenticationException {

		try {
			 Account user = account.authenticate(auth.getName(),
					(String) auth.getCredentials());
			List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
						
			roles.add(new GrantedAuthorityImpl("admin"));
			
			AceLogger.Instance().log(AceLogger.INFORMATIONAL,
					AceLogger.USER_LOG,
					"User " + auth.getName() + " logged in");

			org.springframework.security.core.userdetails.User u = new org.springframework.security.core.userdetails.User(
					user.getUserName(), (String) auth.getCredentials(), true, true,
					true, true, roles);
			return new UsernamePasswordAuthenticationToken(u,
					auth.getCredentials(), roles);
		} catch (Exception e) {
			throw new BadCredentialsException("Authentication Failed!", e);
		}
	}

	@Override
	public boolean supports(Class<? extends Object> authObj) {
		if (authObj.isAssignableFrom(UsernamePasswordAuthenticationToken.class)) {
			return true;
		}
		return false;
	}
}
