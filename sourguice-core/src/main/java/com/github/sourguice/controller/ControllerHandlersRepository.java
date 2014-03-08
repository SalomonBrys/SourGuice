package com.github.sourguice.controller;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;


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
	private final Map<InstanceGetter<?>, ControllerHandler<?>> map = new HashMap<>();

	/**
	 * Gets the {@link ControllerHandler} for a given class and creates one if none is yet registered for this class
	 *
	 * @param controller The controller getter on which to get / create a {@link ControllerHandler}
	 * @return The handler for the given class
	 */
	@SuppressWarnings("unchecked")
	public <T> ControllerHandler<T> get(final InstanceGetter<T> controller) {
		if (this.map.containsKey(controller)) {
			return (ControllerHandler<T>) this.map.get(controller);
		}
		final ControllerHandler<T> handler = new ControllerHandler<>(controller);
		this.map.put(controller, handler);
		return handler;
	}
}
