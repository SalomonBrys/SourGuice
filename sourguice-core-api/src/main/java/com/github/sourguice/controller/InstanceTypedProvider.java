package com.github.sourguice.controller;

import com.google.inject.TypeLiteral;

/**
 * Used by the syntax *().withInstance()
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T> The type of the instance this will hold
 */
public class InstanceTypedProvider<T> implements TypedProvider<T> {

	/**
	 * The instance to hold and give back when asked
	 */
	private final T instance;

	/**
	 * The instance type
	 */
	private final TypeLiteral<T> type;

	/**
	 * Constructor
	 *
	 * @param instance The instance to hold and give back when asked
	 */
	@SuppressWarnings("unchecked")
	public InstanceTypedProvider(final T instance) {
		super();
		this.instance = instance;
		this.type = (TypeLiteral<T>) TypeLiteral.get(this.instance.getClass());
	}

	/**
	 * Constructor
	 *
	 * @param instance The instance to hold and give back when asked
	 * @param type The exact type of the instance
	 */
	public InstanceTypedProvider(final T instance, final TypeLiteral<T> type) {
		super();
		this.instance = instance;
		this.type = type;
	}

	@Override
	public T get() {
		return this.instance;
	}

	@Override
	public TypeLiteral<T> getTypeLiteral() {
		return this.type;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof InstanceTypedProvider && this.instance.equals(((InstanceTypedProvider<?>)obj).instance);
	}

	@Override
	public int hashCode() {
		return this.instance.hashCode();
	}

}
