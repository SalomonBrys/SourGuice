package com.github.sourguice.call;

import java.lang.annotation.Annotation;

import javax.annotation.CheckForNull;

import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.google.inject.TypeLiteral;

/**
 * Fetches additional arguments for method calling through {@link SGCaller}
 *
 * @param <T> Which type of argument. This is for type safety only, one CalltimeArgumentFetcher can handle multiple types.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public interface CalltimeArgumentFetcher<T> {
	/**
	 * @param type The type of the argument to fetch
	 * @param annos All annotations attached to the method's argument to bind
	 * @return Whether or not the needed argument of the method to be called is fetchable by this fetcher
	 */
	public boolean canGet(TypeLiteral<?> type, Annotation[] annos);

	/**
	 *
	 * @param type The type of the argument to fetch
	 * @param annos All annotations attached to the method's argument to bind
	 * @throws NoSuchRequestParameterException In case of a parameter asked and does not exists
	 * @return The object to bind to the method's argument
	 */
	public @CheckForNull T get(TypeLiteral<?> type, Annotation[] annos) throws NoSuchRequestParameterException;
}
