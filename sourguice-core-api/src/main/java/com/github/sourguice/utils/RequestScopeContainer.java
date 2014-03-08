package com.github.sourguice.utils;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import com.google.inject.servlet.RequestScoped;

/**
 * This is a Request scoped container that is used to store / provide request scoped objects.
 * As the RequestScopeContainer is in @{@link RequestScoped} scope, anything you store into it
 * will be available for the time of the current request.
 * You can store one item per class as items are stored according to their classes.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@RequestScoped
public final class RequestScopeContainer {
	/**
	 * The map containing the items
	 */
	private final Map<Class<?>, Object> map = new HashMap<>();

	/**
	 * @param cls The class on which to register the object
	 * @param obj Stores this object for the time of the request
	 */
	public <T> void store(final Class<? super T> cls, final T obj) {
		this.map.put(cls, obj);
	}

	/**
	 * Gets an object if it has been stored previously during this request
	 *
	 * @param cls The class of the object to fetch
	 * @return The object if it was previously registered or null
	 */
	@SuppressWarnings("unchecked")
	public @CheckForNull <T> T get(final Class<T> cls) {
		return (T) this.map.get(cls);
	}
}
