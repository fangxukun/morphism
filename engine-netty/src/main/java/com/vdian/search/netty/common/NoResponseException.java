package com.vdian.search.netty.common;

/**
 * User: xukun.fyp
 * Date: 17/3/30
 * Time: 16:32
 */
public class NoResponseException extends Exception {
	public NoResponseException() {
	}

	public NoResponseException(String message) {
		super(message);
	}

	public NoResponseException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoResponseException(Throwable cause) {
		super(cause);
	}

	public NoResponseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
