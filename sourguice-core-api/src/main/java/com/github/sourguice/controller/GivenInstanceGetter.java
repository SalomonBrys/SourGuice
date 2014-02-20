package com.github.sourguice.controller;

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
	public Class<T> getInstanceClass() {
		return (Class<T>)instance.getClass();
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
