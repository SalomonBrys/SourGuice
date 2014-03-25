package com.github.sourguice.call;

import java.lang.reflect.Method;

import com.google.inject.TypeLiteral;

/**
 * Factory that creates new {@link SGInvocation}s
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public interface SGInvocationFactory {

	/**
	 * Create a new invocation.
	 * An invocation should only be created once and then cached to be called as many times as needed
	 *
	 * @param controllerType The exact type of the object on which the invocation will take place
	 * @param method The method of the invocation
	 * @param factories Any additional {@link ArgumentFetcher} factory (optional)
	 * @return The created invocation
	 */
	public abstract SGInvocation newInvocation(TypeLiteral<?> controllerType, Method method, ArgumentFetcherFactory... factories);

}
