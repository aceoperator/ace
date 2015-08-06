/**
 * 
 */
package com.quikj.server.app.adapter;

/**
 * @author amit
 * 
 */
public class AppServerAdapterException extends Exception {
	private static final long serialVersionUID = 2469972339947060093L;

	private boolean recoverable = false;

	public AppServerAdapterException() {
	}

	public AppServerAdapterException(String message, Throwable cause) {
		super(message, cause);
	}

	public AppServerAdapterException(String message) {
		super(message);
	}

	public AppServerAdapterException(String message, boolean recoverable) {
		super(message);
		this.recoverable = recoverable;
	}

	public AppServerAdapterException(String message, boolean recoverable,
			Throwable cause) {
		super(message, cause);
		this.recoverable = recoverable;
	}

	public AppServerAdapterException(Throwable cause) {
		super(cause);
	}

	public boolean isRecoverable() {
		return recoverable;
	}

	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}
}
