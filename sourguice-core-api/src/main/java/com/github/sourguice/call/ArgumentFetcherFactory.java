package com.github.sourguice.call;

import java.lang.reflect.Method;

import javax.annotation.CheckForNull;

import com.google.inject.TypeLiteral;

/**
 * Interface responsible for creating {@link ArgumentFetcher} when creating new {@link SGInvocation}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public interface ArgumentFetcherFactory {

	/**
	 * Create (if it can) the argument fetcher for the position'nth argument of the method
	 *
	 * @param method The method of the argument to fetch
	 * @param position The position of the argument to fetch
	 * @param argType The exact type of the argument to fetch
	 * @return The argument fetcher for this argument or null if this argument is not to be handled by you
	 */
	public @CheckForNull ArgumentFetcher<?> create(Method method, int position, TypeLiteral<?> argType);

}
