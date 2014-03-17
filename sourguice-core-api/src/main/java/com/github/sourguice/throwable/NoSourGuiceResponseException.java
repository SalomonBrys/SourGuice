package com.github.sourguice.throwable;

/**
 * Thrown when a SourGuice response could not be found
 *
 * This usually happens when the user has forgotten to set the com.github.sourguice.SourGuiceFilter filter
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class NoSourGuiceResponseException extends RuntimeException {
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = -7673588736646558235L;

	/**
	 * @see RuntimeException#RuntimeException()
	 */
	public NoSourGuiceResponseException() {
		super("The current HTTPResponse is not SourGuice specific: did you forget to use the com.github.sourguice.SourGuiceFilter filter ?");
	}

}
