package com.github.sourguice.provider;

import javax.annotation.CheckForNull;

import com.github.sourguice.MultipleBindBuilder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Basic implementation of {@link MultipleBindBuilder} alowing 5 methods to be bottlenecked into one
 *
 * @param <T> The type of instance to register
 */
@SuppressWarnings("unchecked")
public abstract class TypedProviderMultipleBindBuilder<T> implements MultipleBindBuilder<T> {

	/**
	 * {@link GuiceTypedProvider} factory (needed because those must be injected)
	 */
	private final GTPModuleFactory gtpFactory;

	/**
	 * Constructor
	 *
	 * @param gtpFactory {@link GuiceTypedProvider} factory (needed because those must be injected)
	 */
	public TypedProviderMultipleBindBuilder(final @CheckForNull GTPModuleFactory gtpFactory) {
		super();
		if (gtpFactory == null) {
			throw new UnsupportedOperationException("You cannot register anything after calling install(sourguice.module())");
		}
		this.gtpFactory = gtpFactory;
	}

	@Override
	public MultipleBindBuilder<T> with(final Key<? extends T> key, final Key<? extends T>... keys) {
		register(this.gtpFactory.create(key));
		for (final Key<? extends T> addKey : keys) {
			register(this.gtpFactory.create(addKey));
		}
		return this;
	}

	@Override
	public MultipleBindBuilder<T> with(final Class<? extends T> type, final Class<? extends T>... types) {
		with(Key.get(type));
		for (final Class<? extends T> addType : types) {
			with(Key.get(addType));
		}
		return this;
	}

	@Override
	public MultipleBindBuilder<T> with(final TypeLiteral<? extends T> type, final TypeLiteral<? extends T>... types) {
		with(Key.get(type));
		for (final TypeLiteral<? extends T> addType : types) {
			with(Key.get(addType));
		}
		return this;
	}

	@Override
	public MultipleBindBuilder<T> withInstance(final T instance, final T... instances) {
		register(new InstanceTypedProvider<>(instance));
		for (final T addInstance : instances) {
			register(new InstanceTypedProvider<>(addInstance));
		}
		return this;
	}

	@Override
	public MultipleBindBuilder<T> withInstance(final T instance, final TypeLiteral<T> type) {
		final TypedProvider<? extends T> getter = new InstanceTypedProvider<>(instance, type);
		register(getter);
		return this;
	}

	/**
	 * Makes the registration within the corresponding service.
	 *
	 * @param getter The {@link TypedProvider} to register within the service.
	 */
	abstract protected void register(TypedProvider<? extends T> getter);
}