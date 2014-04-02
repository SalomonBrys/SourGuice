package com.github.sourguice.mvc.controller.fetchers;

import java.util.Map;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.github.sourguice.call.ConvertArgumentFetcher;
import com.github.sourguice.mvc.annotation.request.PathVariable;
import com.github.sourguice.mvc.annotation.request.PathVariablesMap;
import com.github.sourguice.mvc.throwable.invocation.NoSuchPathVariableException;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link PathVariable} annotated arguments
 *
 * @param <T> The type of the argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class PathVariableArgumentFetcher<T> extends ConvertArgumentFetcher<T> {

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private final PathVariable infos;

	/**
	 * The method whose argument we are fetching
	 */
	private final String methodName;

	/**
	 * The provider for the path variable map, from which the result will be found
	 */
	@Inject
	private @CheckForNull @PathVariablesMap Provider<Map<String, String>> pathVariablesProvider;

	/**
	 * @param type The type of the argument to fetch
	 * @param infos The annotations containing needed informations to fetch the argument
	 * @param ref The reference map that links path variable name to their index when a url matches
	 * @param methodName The name of the method whose argument we are fetching
	 */
	public PathVariableArgumentFetcher(final TypeLiteral<T> type, final PathVariable infos, final Map<String, Integer> ref, final String methodName) {
		super(type);
		this.infos = infos;
		this.methodName = methodName;
		if (!ref.containsKey(infos.value())) {
			throw new NoSuchPathVariableException(infos.value(), methodName);
		}
	}

	@Override
	public @CheckForNull T getPrepared() throws NoSuchRequestParameterException {
		assert this.pathVariablesProvider != null;
		final Map<String, String> pathVariables = this.pathVariablesProvider.get();
		if (pathVariables == null || pathVariables.get(this.infos.value()) == null) {
			// This should never happen (I can't see a way to test it) since
			//   1- Existence of the pathvariable key has been checked in constructor
			//   2- If we are here, it means that the URL has matched the regex with the corresponding key
			throw new NoSuchRequestParameterException(this.infos.value(), "path variables", this.methodName);
		}
		return convert(pathVariables.get(this.infos.value()));
	}
}
