package com.github.sourguice.exception;

import javax.annotation.CheckForNull;

/**
 * Singleton service that handles exceptions thrown by the controllers that are registered into it.
 * This allows you to have a standard way of treating exceptions throughout all your controllers.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public interface ExceptionService {

	/**
	 * Get the first handler that can handle a given exception class
	 *
	 * @param clazz The class of the exception to be handled
	 * @return The handler or null
	 */
	public @CheckForNull
	abstract <T extends Exception>ExceptionHandler<? super T> getHandler(Class<T> clazz);

}
