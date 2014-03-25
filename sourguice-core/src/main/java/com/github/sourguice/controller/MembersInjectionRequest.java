package com.github.sourguice.controller;

import com.github.sourguice.call.ArgumentFetcher;

/**
 * Class responsible for injecting newly created {@link ArgumentFetcher}
 */
public interface MembersInjectionRequest {
	/**
	 * Injects (or requests injection) on the given instance
	 *
	 * @param instance The object to inject
	 */
	void requestMembersInjection(Object instance);
}