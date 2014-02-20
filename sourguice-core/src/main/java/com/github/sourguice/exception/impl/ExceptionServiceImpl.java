package com.github.sourguice.exception.impl;

import java.util.LinkedHashMap;

import javax.annotation.CheckForNull;
import javax.inject.Singleton;

import com.github.sourguice.controller.InstanceGetter;
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
	private LinkedHashMap<Class<? extends Exception>, InstanceGetter<? extends ExceptionHandler<? extends Exception>>> map = new LinkedHashMap<>();

	/**
	 * Registers an exception class and its corresponding exception handler
	 *
	 * @param clazz The exception class to handle
	 * @param handler The handler that handles the exception
	 * @throws UnreachableExceptionHandlerException When an ExceptionHandler will never be reached because a previous ExceptionHandler
	 *                                              has been registered that already handles this class of exception
	 */
	public <T extends Exception> void register(Class<? extends T> cls, InstanceGetter<? extends ExceptionHandler<T>> ig) throws UnreachableExceptionHandlerException {
		if (!map.containsKey(cls))
			for (Class<? extends Exception> sup : map.keySet()) {
				if (sup.isAssignableFrom(cls))
					throw new UnreachableExceptionHandlerException(cls, sup);
			}
		map.put(cls, ig);
	}

	/**
	 * Get the first handler that can handle a given exception class
	 * @param clazz The class of the exception to be handled
	 * @return The handler or null
	 */
	@SuppressWarnings("unchecked")
	@Override
	public @CheckForNull <T extends Exception> ExceptionHandler<? super T> getHandler(Class<T> clazz) {
		for (Class<? extends Exception> c : map.keySet()) {
			if (c.isAssignableFrom(clazz))
				return (ExceptionHandler<? super T>) map.get(c).getInstance();
		}
		return null;
	}
}
