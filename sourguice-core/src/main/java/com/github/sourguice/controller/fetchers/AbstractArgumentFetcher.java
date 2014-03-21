package com.github.sourguice.controller.fetchers;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.github.sourguice.controller.ArgumentFetcher;
import com.github.sourguice.conversion.ConversionService;
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
public abstract class AbstractArgumentFetcher<T> implements ArgumentFetcher<T> {

	/**
	 * The type of the argument to fetch
	 */
	private final TypeLiteral<T> type;

	/**
	 * Conversion service provider
	 */
	@Inject
	protected @CheckForNull Provider<ConversionService> conversionServiceProvider;

	/**
	 * @param type The type of the argument to fetch
	 */
	protected AbstractArgumentFetcher(final TypeLiteral<T> type) {
		this.type = type;
	}

	/**
	 * Util for subclasses to convert an object to the type this fetcher is supposed to return
	 *
	 * @param value The value to convert
	 * @return The converted value
	 */
	protected @CheckForNull T convert(final Object value) {
		assert this.conversionServiceProvider != null;
		return this.conversionServiceProvider.get().convert(this.type, value);
	}
}
