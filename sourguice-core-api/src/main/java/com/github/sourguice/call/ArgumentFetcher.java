package com.github.sourguice.call;

import com.github.sourguice.conversion.ConversionService;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;

/**
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
public interface ArgumentFetcher<T> {

	/**
	 * This is where subclass fetch the argument
	 *
	 * @return The argument to be passed to the invocation
	 * @throws NoSuchRequestParameterException In case of a parameter asked from request argument or path variable that does not exists
	 */
	public abstract T getPrepared() throws NoSuchRequestParameterException;

}
