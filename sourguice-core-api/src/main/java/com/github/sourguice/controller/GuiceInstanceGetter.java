package com.github.sourguice.controller;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Used by the syntax control().with()
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T> The type of the instance it will ask guice
 */
public class GuiceInstanceGetter<T> implements TypedProvider<T> {

	/**
	 * The key to retrieve an instance in Guice
	 */
	private final Key<T> key;

	/**
	 * Guice injector
	 */
	private @CheckForNull Provider<T> provider;

	/**
	 * @param key The key to retrieve an instance in Guice
	 */
	public GuiceInstanceGetter(final Key<T> key) {
		super();
		this.key = key;
	}

	/**
	 * @param injector Guice injector to get {@link #provider}
	 */
	@Inject
	public void setInjector(final Injector injector) {
		this.provider = injector.getProvider(this.key);
	}

	@Override
	public T get() {
		assert this.provider != null;
		return this.provider.get();
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
