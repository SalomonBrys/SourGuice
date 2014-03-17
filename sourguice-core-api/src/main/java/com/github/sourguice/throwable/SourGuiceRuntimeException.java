package com.github.sourguice.throwable;

/**
 * Exception that encapusulates an exception has been thrown by java and that could not be handled by SourGuice
 *
 * {@link #getCause()} will never return null
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class SourGuiceRuntimeException extends RuntimeException {

	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 4077437307080980456L;

	/**
	 * @see RuntimeException#RuntimeException(Throwable)
	 */
	public SourGuiceRuntimeException(final Throwable cause) {
		super(cause);
	}

}
