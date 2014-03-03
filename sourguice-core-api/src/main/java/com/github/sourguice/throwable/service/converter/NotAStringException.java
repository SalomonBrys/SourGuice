package com.github.sourguice.throwable.service.converter;

public class NotAStringException extends RuntimeException {

	private static final long serialVersionUID = -5682576910326097695L;

	public NotAStringException() {
		super("Only String, array of String, array of array of string, etc. can be converted from");
	}

}
