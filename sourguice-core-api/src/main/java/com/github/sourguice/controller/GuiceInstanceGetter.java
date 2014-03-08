package com.github.sourguice.controller;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Used by the syntax control().with()
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T> The type of the instance it will ask guice
 */
public class GuiceInstanceGetter<T> implements InstanceGetter<T> {

	/**
	 * The key to retrieve an instance in Guice
	 */
	private final Key<T> key;

	/**
	 * Guice injector
	 */
	private @Inject @CheckForNull Injector injector;

	/**
	 * @param key The key to retrieve an instance in Guice
	 */
	public GuiceInstanceGetter(final Key<T> key) {
		super();
		this.key = key;
	}

	@Override
	public T getInstance() {
		assert this.injector != null;
		return this.injector.getInstance(this.key);
	}

	@Override
	public TypeLiteral<T> getTypeLiteral() {
		return this.key.getTypeLiteral();
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof GuiceInstanceGetter && this.key.equals(((GuiceInstanceGetter<?>)obj).key);
	}

	@Override
	public int hashCode() {
		return this.key.hashCode();
	}

}
