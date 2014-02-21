package com.github.sourguice.throwable.service.converter;

import com.google.inject.TypeLiteral;

/**
 * Exception thrown when a converter could not been found by a method that converts (not getConverter)
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class NoConverterException extends RuntimeException {
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 147021115407274546L;

	/**
	 * @param clazz The class for which the converter could not be found
	 */
	public NoConverterException(TypeLiteral<?> type) {
		super("Could not find converter for " + type.toString());
	}
}
