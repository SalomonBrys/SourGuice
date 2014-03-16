package com.github.sourguice.throwable.invocation;

/**
 * Exception thrown when the cache is configured too late
 * (after something has already been written to the response)
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class CacheTooLateException extends RuntimeException {
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 3346569667081089710L;

	/**
	 * @see Exception#Exception()
	 */
	public CacheTooLateException() {
		super("You cannot configure the cache after having already written to the request");
	}

}
