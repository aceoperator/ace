/**
 * 
 */
package com.quikj.ace.db.webtalk;

/**
 * @author amit
 * 
 */
public class WebTalkException extends RuntimeException {

	private static final long serialVersionUID = 4674616604969804311L;

	public WebTalkException() {
	}

	public WebTalkException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebTalkException(String message) {
		super(message);
	}

	public WebTalkException(Throwable cause) {
		super(cause);
	}
}
