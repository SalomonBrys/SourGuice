package com.github.sourguice.throwable.service.converter;

public class CannotConvertToPrimitiveException extends RuntimeException {

	private static final long serialVersionUID = 1985516629946168889L;

	public CannotConvertToPrimitiveException() {
		super("Array conversion does not support primitive types");
	}

}
