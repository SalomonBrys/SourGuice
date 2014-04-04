package com.github.sourguice;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Interface used to permit the syntax like *(...).with(...).with(...)
 *
 * @param <T> The type of instance to register
 */
@SuppressWarnings("unchecked")
public interface MultipleBindBuilder<T> {

	/**
	 * Registers a Guice key
	 *
	 * @param key The key to register
	 * @param keys Any additional key to register
	 * @return Itself to chain calls
	 */
	public abstract MultipleBindBuilder<T> with(Key<? extends T> key, Key<? extends T>... keys);

	/**
	 * Registers a type to be retrieved with Guice
	 *
	 * @param type The type to register
	 * @param types Any additional type to register
	 * @return Itself to chain calls
	 */
	public abstract MultipleBindBuilder<T> with(Class<? extends T> type, Class<? extends T>... types);

	/**
	 * Registers a type to be retrieved with Guice
	 *
	 * @param type The type to register
	 * @param types Any additional type to register
	 * @return Itself to chain calls
	 */
	public abstract MultipleBindBuilder<T> with(TypeLiteral<? extends T> type, TypeLiteral<? extends T>... types);

	/**
	 * Registers a pre-created instance
	 *
	 * @param instance The object to register
	 * @param instances Any additional object to register
	 * @return Itself to chain calls
	 */
	public abstract MultipleBindBuilder<T> withInstance(T instance, T... instances);

	/**
	 * Registers a pre-created instance
	 *
	 * @param instance The object to register
	 * @param type The exact type of the object to register
	 * @return Itself to chain calls
	 */
	public abstract MultipleBindBuilder<T> withInstance(T instance, TypeLiteral<T> type);

}