package com.github.sourguice;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Interface used to permit the syntax like *(...).with(...)
 *
 * @param <T> The type of instance to register
 */
public interface SingleBindBuilder<T> {

	/**
	 * Registers a Guice key
	 *
	 * @param key The key to register
	 */
	public abstract void with(Key<? extends T> key);

	/**
	 * Registers a type to be retrieved with Guice
	 *
	 * @param type The type to register
	 */
	public abstract void with(Class<? extends T> type);

	/**
	 * Registers a type to be retrieved with Guice
	 *
	 * @param type The type to register
	 */
	public abstract void with(TypeLiteral<? extends T> type);

	/**
	 * Registers a pre-created instance
	 *
	 * @param instance The object to register
	 */
	public abstract void withInstance(T instance);

	/**
	 * Registers a pre-created instance
	 *
	 * @param instance The object to register
	 * @param type The exact type of the object to register
	 */
	public abstract void withInstance(T instance, TypeLiteral<T> type);

}