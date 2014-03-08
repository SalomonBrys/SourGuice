package com.github.sourguice.call;

import java.lang.annotation.Annotation;

import javax.annotation.CheckForNull;

import com.google.inject.TypeLiteral;

/**
 * Fetches additional arguments for method calling through {@link MvcCaller}
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
	 * @return The object to bind to the method's argument
	 * @throws Throwable If anything went wrong while fetching
	 */
	public @CheckForNull T get(TypeLiteral<?> type, Annotation[] annos) throws Throwable;
}
