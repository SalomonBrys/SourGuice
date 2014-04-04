package com.github.sourguice.provider;

import javax.annotation.CheckForNull;

import com.github.sourguice.SingleBindBuilder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Basic implementation of {@link SingleBindBuilder} alowing 5 methods to be bottlenecked into one
 *
 * @param <T> The type of instance to register
 */
public abstract class TypedProviderSingleBindBuilder<T> implements SingleBindBuilder<T> {

	/**
	 * {@link GuiceTypedProvider} factory (needed because those must be injected)
	 */
	private final GTPModuleFactory gtpFactory;

	/**
	 * Constructor
	 *
	 * @param gtpFactory {@link GuiceTypedProvider} factory (needed because those must be injected)
	 */
	public TypedProviderSingleBindBuilder(final @CheckForNull GTPModuleFactory gtpFactory) {
		super();
		if (gtpFactory == null) {
			throw new UnsupportedOperationException("You cannot register anything after calling install(sourguice.module())");
		}
		this.gtpFactory = gtpFactory;
	}

	@Override
	public void with(final Key<? extends T> key) {
		register(this.gtpFactory.create(key));
	}

	@Override
	public void with(final Class<? extends T> type) {
		with(Key.get(type));
	}

	@Override
	public void with(final TypeLiteral<? extends T> type) {
		with(Key.get(type));
	}

	@Override
	public void withInstance(final T instance) {
		final TypedProvider<? extends T> getter = new InstanceTypedProvider<>(instance);
		register(getter);
	}

	@Override
	public void withInstance(final T instance, final TypeLiteral<T> type) {
		final TypedProvider<? extends T> getter = new InstanceTypedProvider<>(instance, type);
		register(getter);
	}

	/**
	 * Makes the registration within the corresponding service.
	 *
	 * @param getter The {@link TypedProvider} to register within the service.
	 */
	abstract protected void register(TypedProvider<? extends T> getter);
}