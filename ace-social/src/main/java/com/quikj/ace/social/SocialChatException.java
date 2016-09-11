/**
 * 
 */
package com.quikj.ace.social;

/**
 * @author amit
 *
 */
public class SocialChatException extends RuntimeException {

	private static final long serialVersionUID = 1913069048653352712L;

	public SocialChatException() {
		super();
	}

	public SocialChatException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SocialChatException(String message, Throwable cause) {
		super(message, cause);
	}

	public SocialChatException(String message) {
		super(message);
	}

	public SocialChatException(Throwable cause) {
		super(cause);
	}
}
