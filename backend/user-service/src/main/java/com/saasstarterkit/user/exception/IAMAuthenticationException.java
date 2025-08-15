package com.saasstarterkit.user.exception;

public class IAMAuthenticationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4376682081518928789L;

	public IAMAuthenticationException(String message) {
		this.message = message;
	}

	private String message;

	public String getMessage() {
		return message;
	}
}
