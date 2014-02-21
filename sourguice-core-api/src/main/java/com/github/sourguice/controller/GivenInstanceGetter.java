package com.github.sourguice.controller;

import com.google.inject.TypeLiteral;

/**
 * Used by the syntax control().withInstance()
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T>
 */
public class GivenInstanceGetter<T> implements InstanceGetter<T> {

	T instance;

	public GivenInstanceGetter(T instance) {
		super();
		this.instance = instance;
	}

	@Override
	public T getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypeLiteral<T> getTypeLiteral() {
		return (TypeLiteral<T>) TypeLiteral.get(instance.getClass());
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GivenInstanceGetter && instance.equals(((GivenInstanceGetter<?>)obj).instance);
	}

	@Override
	public int hashCode() {
		return instance.hashCode();
	}

}
