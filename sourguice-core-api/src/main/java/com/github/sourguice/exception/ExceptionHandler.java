package com.github.sourguice.exception;

import java.io.IOException;

/**
 * Base interface for class that handles an exception to be registered in {@link ExceptionService}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T> The type of the exception to handle
 */
public interface ExceptionHandler<T extends Exception> {
	/**
	 * Handles an exception
	 *
	 * @param exception The exception to be handled
	 * @return Whether the exception has been handled or not (if false is returned, the exception will be re-thrown)
	 * @throws IOException If an input or output exception occurs during response manipulation
	 */
	boolean handle(T exception) throws IOException;
}
