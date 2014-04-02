package com.github.sourguice.provider;

import java.util.LinkedList;
import java.util.List;

import com.google.inject.Binder;
import com.google.inject.Key;

/**
 * Factory that creates new GuiceTypedProvider that will be injected with the given {@link Binder}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class GTPModuleFactory {

	/**
	 * List of providers that need injection and that we created before installing the module
	 */
	private final List<GuiceTypedProvider<?>> created = new LinkedList<>();

	/**
	 * Create a new {@link GuiceTypedProvider}
	 *
	 * @param key The Guice key to provide
	 * @return The new provider
	 */
	public <T> GuiceTypedProvider<T> create(final Key<T> key) {
		final GuiceTypedProvider<T> provider = new GuiceTypedProvider<>(key);
		this.created.add(provider);
		return provider;
	}

	/**
	 * Constructor
	 *
	 * @param binder Binder that will be requested for injection on the new providers
	 */
	public void requestInjection(final Binder binder) {
		for (final GuiceTypedProvider<?> provider : this.created) {
			binder.requestInjection(provider);
		}
	}
}
