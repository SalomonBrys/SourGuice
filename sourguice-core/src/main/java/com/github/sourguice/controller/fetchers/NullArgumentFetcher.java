package com.github.sourguice.controller.fetchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import org.aopalliance.intercept.MethodInterceptor;

import com.github.sourguice.annotation.request.InterceptParam;
import com.github.sourguice.annotation.request.PathVariablesMap;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that will always return null
 * This is used for @{@link InterceptParam} annotated argument.
 * They return null but the actual argument will later be injected in a {@link MethodInterceptor}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class NullArgumentFetcher<T> extends ArgumentFetcher<T> {

	/**
	 * @see ArgumentFetcher#ArgumentFetcher(Type, int, Annotation[])
	 */
	public NullArgumentFetcher(final TypeLiteral<T> type, final Annotation[] annotations) {
		super(type, annotations);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected @CheckForNull T getPrepared(final HttpServletRequest req, final @PathVariablesMap Map<String, String> pathVariables, final Injector injector) {
		return null;
	}
}
