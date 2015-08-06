/**
 * 
 */
package com.quikj.ace.web.client;

/**
 * @author amit
 *
 */
public class AceClientException extends RuntimeException {

	private static final long serialVersionUID = -5113645864178159471L;

	/**
	 * 
	 */
	public AceClientException() {
	}

	/**
	 * @param message
	 */
	public AceClientException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AceClientException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AceClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
