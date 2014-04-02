package com.github.sourguice.throwable.converter;

/**
 * Exception thrown when the conversion service tries to convert from an object that is not a String nor a String Array
 *
 * This is a runtime exception because it is a programming error and therefore should only be caught in very specific circumstances.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class NotAStringException extends RuntimeException {

	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = -5682576910326097695L;

	/**
	 * @param cls the non-string type that we tried to convert from
	 */
	public NotAStringException(final Class<?> cls) {
		super("Only String, array of String, array of array of string, etc. can be converted from. This is not supported: " + cls.getCanonicalName());
	}

}
