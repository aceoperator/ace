/**
 * 
 */
package com.quikj.ace.web.shared;

import java.io.Serializable;

/**
 * @author amit
 *
 */
public class AceServerException extends Exception implements Serializable {

	private static final long serialVersionUID = -3432554480747747864L;
	
	private boolean recoverable = false;
	
	public AceServerException() {
		super();
	}

	public AceServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public AceServerException(String message) {
		super(message);
	}
	
	public AceServerException(String message, boolean recoverable) {
		super(message);
		this.recoverable = recoverable;
	}

	public AceServerException(Throwable cause) {
		super(cause);
	}
	
	public AceServerException(Throwable cause, boolean recoverable) {
		super(cause);
		this.recoverable = recoverable;
	}

	public boolean isRecoverable() {
		return recoverable;
	}

	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}
}
