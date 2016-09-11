/**
 * 
 */
package com.quikj.ace.social;

import java.security.Principal;

/**
 * @author tomcat
 *
 */
public class SocialPrincipal implements Principal {

	public enum AuthType {
		PASSWORD,
		OATH
	}
	
	private String name;
	
	private Object credential;
	
	private AuthType authType = AuthType.PASSWORD;
	
	public SocialPrincipal(String name, Object credential, AuthType authType) {
		this.name = name;
		this.credential = credential;
		this.authType = authType;
	}

	public AuthType getAuthType() {
		return authType;
	}

	public Object getCredential() {
		return credential;
	}

	@Override
	public String getName() {
		return name;
	}
}
