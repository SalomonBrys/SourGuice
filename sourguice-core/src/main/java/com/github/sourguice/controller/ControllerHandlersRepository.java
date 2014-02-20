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
	private Map<InstanceGetter<?>, ControllerHandler<?>> map = new HashMap<>();

	/**
	 * Gets the {@link ControllerHandler} for a given class and creates one if none is yet registered for this class
	 *
	 * @param clazz The class on which to get / create a {@link ControllerHandler}
	 * @return The handler for the given class
	 */
	@SuppressWarnings("unchecked")
	public <T> ControllerHandler<T> get(InstanceGetter<T> ig) {
		if (map.containsKey(ig))
			return (ControllerHandler<T>)map.get(ig);
		ControllerHandler<T> c = new ControllerHandler<>(ig);
		map.put(ig, c);
		return c;
	}
}
