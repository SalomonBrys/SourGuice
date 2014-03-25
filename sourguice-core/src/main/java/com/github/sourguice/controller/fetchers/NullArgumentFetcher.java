package com.github.sourguice.controller.fetchers;

import javax.annotation.CheckForNull;

import org.aopalliance.intercept.MethodInterceptor;

import com.github.sourguice.annotation.request.InterceptParam;
import com.github.sourguice.call.ArgumentFetcher;

/**
 * Fetcher that will always return null
 * This is used for @{@link InterceptParam} annotated argument.
 * They return null but the actual argument will later be injected in a {@link MethodInterceptor}
 *
 * @param <T> The type of the object being asked (ignored since null will always be returned)
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class NullArgumentFetcher<T> implements ArgumentFetcher<T> {

	@Override
	public @CheckForNull T getPrepared() {
		return null;
	}
}
