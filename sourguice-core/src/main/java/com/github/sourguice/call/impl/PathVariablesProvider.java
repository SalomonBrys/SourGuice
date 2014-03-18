package com.github.sourguice.call.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.MatchResult;

import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.call.SGCaller;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;

/**
 * Guice provider for @{@link PathVariablesMap} map
 * It is a stack provider, as the path variables returned in guice should vary according to the call.
 * Basically, when a call through {@link SGCaller} is made, a new URIPathVariables is pushed
 * and is poped at the end of the call.
 * This class should only be used internally
 */
@RequestScoped
public final class PathVariablesProvider implements Provider<Map<String, String>> {

	/**
	 * The stack of URIPathVariables
	 * At the top of the stack is the path variable corresponding to the "current" call
	 */
	private final Stack<Map<String, String>> pathVariables = new Stack<>();

	/**
	 * Pushes a new URIPathVariables (which should correspond to the begining of a call)
	 *
	 * @param vars The URIPathVariables to push
	 */
	public void push(final @PathVariablesMap Map<String, String> vars) {
		this.pathVariables.push(vars);
	}

	/**
	 * Pops the "current" URIPathVariables (which should correspond to the end of a call)
	 */
	public void pop() {
		this.pathVariables.pop();
	}

	/**
	 * @param map The map to coerce
	 * @return The exact same map as given, except with \@PathVariablesMap type qualifier
	 */
	private static @PathVariablesMap Map<String, String> coercePathVariablesMap(final Map<String, String> map) { return map; }

	/**
	 * Guice provider method : gets the current URIPathVariables
	 */
	@Override
	public @PathVariablesMap Map<String, String> get() {
		if (this.pathVariables.isEmpty()) {
			/* This should never happen and cannot be tested */
			return coercePathVariablesMap(new HashMap<String, String>());
		}
		return coercePathVariablesMap(this.pathVariables.peek());
	}

	/**
	 * Creates a PathVariables map which maps a variable name to it's parsed value
	 *
	 * @param match The MatchResult resulting from the parsed URL for the current request
	 *              This matches the position of the match to their value
	 * @param ref The reference map which is created at startup time based on the given @{@link RequestMapping}
	 *            This maps the name of the variables to their position
	 * @return The path variables map
	 */
	@PathVariablesMap
	public static Map<String, String> fromMatch(final MatchResult match, final Map<String, Integer> ref) {
		final @PathVariablesMap Map<String, String> map = new HashMap<>();
		for (final String key : ref.keySet()) {
			if (match.groupCount() >= ref.get(key).intValue()) {
				map.put(key, match.group(ref.get(key).intValue()));
			}
		}
		return coercePathVariablesMap(map);
	}
}
