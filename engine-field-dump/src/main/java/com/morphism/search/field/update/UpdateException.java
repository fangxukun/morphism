package com.morphism.search.field.update;

/**
 * User: xukun.fyp
 * Date: 17/4/13
 * Time: 14:06
 */
public class UpdateException extends Exception {
	public UpdateException() {
	}

	public UpdateException(String message) {
		super(message);
	}

	public UpdateException(String message, Throwable cause) {
		super(message, cause);
	}

	public UpdateException(Throwable cause) {
		super(cause);
	}

	public UpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
