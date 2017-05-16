/**
 * 
 */
package com.quikj.server.framework;

/**
 * @author amit
 *
 */
public class AceRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -1109157251093277123L;

	public AceRuntimeException() {
	}

	public AceRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public AceRuntimeException(String message) {
		super(message);
	}

	public AceRuntimeException(Throwable cause) {
		super(cause);
	}
}
