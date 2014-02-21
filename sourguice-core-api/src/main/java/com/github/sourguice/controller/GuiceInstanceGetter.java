package com.github.sourguice.controller;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Used by the syntax control().with()
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T>
 */
public class GuiceInstanceGetter<T> implements InstanceGetter<T> {

	Key<T> key;

	@Inject @CheckForNull Injector injector;

	public GuiceInstanceGetter(Key<T> key) {
		super();
		this.key = key;
	}

	@Override
	public T getInstance() {
		assert injector != null;
		return injector.getInstance(key);
	}

	@Override
	public TypeLiteral<T> getTypeLiteral() {
		return key.getTypeLiteral();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GuiceInstanceGetter && key.equals(((GuiceInstanceGetter<?>)obj).key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

}
