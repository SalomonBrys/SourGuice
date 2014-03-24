package com.github.sourguice.controller;

import com.google.inject.TypeLiteral;

/**
 * Used by the syntax *().withInstance()
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T> The type of the instance this will hold
 */
public class GivenInstanceGetter<T> implements TypedProvider<T> {

	/**
	 * The instance to hold and give back when asked
	 */
	private final T instance;

	/**
	 * @param instance The instance to hold and give back when asked
	 */
	public GivenInstanceGetter(final T instance) {
		super();
		this.instance = instance;
	}

	@Override
	public T get() {
		return this.instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypeLiteral<T> getTypeLiteral() {
		return (TypeLiteral<T>) TypeLiteral.get(this.instance.getClass());
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof GivenInstanceGetter && this.instance.equals(((GivenInstanceGetter<?>)obj).instance);
	}

	@Override
	public int hashCode() {
		return this.instance.hashCode();
	}

}
