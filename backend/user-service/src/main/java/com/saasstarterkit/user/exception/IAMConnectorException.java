package com.saasstarterkit.user.exception;

public class IAMConnectorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4376682081518928789L;

	public IAMConnectorException(String message) {
		this.message = message;
	}

	private String message;

	public String getMessage() {
		return message;
	}
}
