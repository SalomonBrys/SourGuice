package com.github.sourguice.mvc.controller;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.github.sourguice.call.SGInvocationFactory;
import com.github.sourguice.provider.TypedProvider;
import com.google.inject.Binder;


/**
 * Class that holds all {@link ControllerHandler} existing in this server instance
 * This is to ensure that each controller class has one and only one handler
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Singleton
public final class ControllerHandlersRepository {

	/**
	 * All classes registered and their corresponding {@link ControllerHandler}
	 */
	private final Map<TypedProvider<?>, ControllerHandler<?>> map = new HashMap<>();

	/**
	 * Gets the {@link ControllerHandler} for a given class and creates one if none is yet registered for this class
	 *
	 * @param controller The controller getter on which to get / create a {@link ControllerHandler}
     * @param binder used to request injection on newly created objects
     * @param invocationFactory The factory responsible for creating new invocations
	 * @return The handler for the given class
	 */
	@SuppressWarnings("unchecked")
	public <T> ControllerHandler<T> get(final TypedProvider<T> controller, final Binder binder, final SGInvocationFactory invocationFactory) {
		if (this.map.containsKey(controller)) {
			return (ControllerHandler<T>) this.map.get(controller);
		}
		final ControllerHandler<T> handler = new ControllerHandler<>(controller, binder, invocationFactory);
		this.map.put(controller, handler);
		return handler;
	}
}
