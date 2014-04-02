package com.github.sourguice.mvc.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;

import com.github.sourguice.mvc.annotation.request.PathVariablesMap;
import com.github.sourguice.mvc.annotation.request.RequestMapping;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;

/**
 * Guice provider for @{@link PathVariablesMap} map
 * Basically, when an HTTP request is made on a {@link RequestMapping} method, a new URIPathVariables is set
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@RequestScoped
public final class PathVariablesHolder implements Provider<Map<String, String>> {

	/**
	 * The stack of URIPathVariables
	 * At the top of the stack is the path variable corresponding to the "current" call
	 */
	private @PathVariablesMap Map<String, String> pathVariables;

	/**
	 * Constructor
	 */
	public PathVariablesHolder() {
		this.pathVariables = coerce(new HashMap<String, String>());
	}

	/**
	 * set a new URIPathVariables (which should correspond to the begining of a call)
	 *
	 * @param match The MatchResult resulting from the parsed URL for the current request
	 *              This matches the position of the match to their value
	 * @param ref The reference map which is created at startup time based on the given @{@link RequestMapping}
	 *            This maps the name of the variables to their position
	 */
	public void set(final MatchResult match, final Map<String, Integer> ref) {
		this.pathVariables = coerce(new HashMap<String, String>());
		for (final String key : ref.keySet()) {
			if (match.groupCount() >= ref.get(key).intValue()) {
				this.pathVariables.put(key, match.group(ref.get(key).intValue()));
			}
		}
	}

	/**
	 * @param map The map to coerce
	 * @return The exact same map as given, except with \@PathVariablesMap type qualifier
	 */
	private static @PathVariablesMap Map<String, String> coerce(final Map<String, String> map) { return map; }

	/**
	 * Guice provider method : gets the current URIPathVariables
	 */
	@Override
	public @PathVariablesMap Map<String, String> get() {
		return this.pathVariables;
	}
}
