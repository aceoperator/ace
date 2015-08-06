package com.quikj.server.framework;

public class AceException extends Exception {

	private static final long serialVersionUID = 2383027903160722158L;
	private boolean recoverable = false;

	public AceException() {
		super();
	}

	public AceException(String message, Throwable cause) {
		super(message, cause);
	}

	public AceException(String message) {
		super(message);
	}

	public AceException(String message, boolean recoverable) {
		super(message);
		this.recoverable = recoverable;
	}

	public AceException(String message, boolean recoverable, Throwable cause) {
		super(message, cause);
		this.recoverable = recoverable;
	}

	public AceException(Throwable cause) {
		super(cause);
	}

	public boolean isRecoverable() {
		return recoverable;
	}

	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}
}
