package com.github.sourguice.throwable.exception;


/**
 * Exception thrown when registering an exception handler that will never been used
 * because a previously registered exception handler already handles the configured exceptions
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class UnreachableExceptionHandlerException extends RuntimeException {
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = -2692200907318800518L;

	/**
	 * @param handlerFor The exception class that the handler was registered to handle
	 * @param interceptedBy The exception handler that already handles it
	 */
	public UnreachableExceptionHandlerException(final Class<? extends Exception> handlerFor, final Class<?> interceptedBy) {
		super("Handler for " + handlerFor.getCanonicalName() + " is unreachable : exceptions will be previously intercepted by " + interceptedBy.getCanonicalName());
	}

}
