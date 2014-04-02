package com.github.sourguice.throwable.converter;


/**
 * Runtime exception thrown when you try to convert to a primitive array.
 *
 * This is a runtime exception because it is a programming error and therefore should only be caught in very specific circumstances.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class CannotConvertToPrimitiveException extends RuntimeException {
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 1985516629946168889L;

	/**
	 * @param cls The class of the primitive
	 */
	public CannotConvertToPrimitiveException(final Class<?> cls) {
		super("Array conversion does not support primitive type: " + cls.getName());
	}

}
