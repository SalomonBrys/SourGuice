package com.github.sourguice.controller.fetchers;

import java.util.Map;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.conversion.ConversionService;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Base class for argument fetchers
 * An argument fetcher is an algorythm that is preconfigured (selected) to retrieve an argument of a method
 * The fetcher is selected / configured when the invocation is created, which means at launch time
 * <p>
 * The fetcher is responsible of getting the right type for the argument and therefore to use {@link ConversionService} if necessary
 *
 * @param <T> The type of the argument that this class will fetch
 *            This is for type safety only as one fetcher can handle multiple types
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public abstract class ArgumentFetcher<T> {

	/**
	 * The type of the argument to fetch
	 */
	private final TypeLiteral<T> type;

	/**
	 * @param type The type of the argument to fetch
	 */
	protected ArgumentFetcher(final TypeLiteral<T> type) {
		this.type = type;
	}

	/**
	 * This is where subclass fetch the argument
	 *
	 * @param req The current HTTP request
	 * @param pathVariables Variables that were parsed from request URL
	 * @param injector Guice injector
	 * @return The argument to be passed to the invocation
	 * @throws NoSuchRequestParameterException In case of a parameter asked from request argument or path variable that does not exists
	 */
	public abstract @CheckForNull T getPrepared(HttpServletRequest req, @PathVariablesMap Map<String, String> pathVariables, Injector injector) throws NoSuchRequestParameterException;

	/**
	 * Util for subclasses to convert an object to the type this fetcher is supposed to return
	 *
	 * @param injector The Guice injector
	 * @param value The value to convert
	 * @return The converted value
	 */
	protected @CheckForNull T convert(final Injector injector, final Object value) {
		return injector.getInstance(ConversionService.class).convert(this.type, value);
	}
}
