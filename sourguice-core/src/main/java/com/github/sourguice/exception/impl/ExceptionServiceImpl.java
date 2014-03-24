package com.github.sourguice.exception.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.inject.Singleton;

import com.github.sourguice.controller.TypedProvider;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.exception.ExceptionService;
import com.github.sourguice.throwable.service.exception.UnreachableExceptionHandlerException;

/**
 * Exception service on which to register exception handlers
 * When an exception is handled by the service, it is registered by the first Exception handler that can handle the exception
 * (just like a regular try / catch)
 * Which means that the order in which the ExceptionHandlers are registered DOES matter
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Singleton
public class ExceptionServiceImpl implements ExceptionService {

	/**
	 * Map of Exception classes and their corresponding ExceptionHandlers
	 */
	private final Map<Class<? extends Exception>, TypedProvider<? extends ExceptionHandler<? extends Exception>>> map = new LinkedHashMap<>();

	/**
	 * Registers an exception class and its corresponding exception handler
	 *
	 * @param cls The exception class to handle
	 * @param handler The handler that handles the exception
	 * @throws UnreachableExceptionHandlerException When an ExceptionHandler will never be reached because a previous ExceptionHandler
	 *                                              has been registered that already handles this class of exception
	 */
	public <T extends Exception> void register(final Class<? extends T> cls, final TypedProvider<? extends ExceptionHandler<T>> handler) throws UnreachableExceptionHandlerException {
		if (!this.map.containsKey(cls)) {
			for (final Class<? extends Exception> sup : this.map.keySet()) {
				if (sup.isAssignableFrom(cls)) {
					throw new UnreachableExceptionHandlerException(cls, sup);
				}
			}
		}
		this.map.put(cls, handler);
	}

	/**
	 * Get the first handler that can handle a given exception class
	 * @param clazz The class of the exception to be handled
	 * @return The handler or null
	 */
	@SuppressWarnings("unchecked")
	@Override
	public @CheckForNull <T extends Exception> ExceptionHandler<? super T> getHandler(final Class<T> clazz) {
		for (final Class<? extends Exception> cls : this.map.keySet()) {
			if (cls.isAssignableFrom(clazz)) {
				return (ExceptionHandler<? super T>) this.map.get(cls).get();
			}
		}
		return null;
	}
}
