package com.github.sourguice;

import com.github.sourguice.call.SGInvocationFactory;
import com.github.sourguice.conversion.Converter;
import com.github.sourguice.exception.ExceptionHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

/**
 * Defines all methods needed to configure SourGuice MVC
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface SourGuiceModule {

	/**
	 * Interface used to permit the syntax like *(...).with(controller.class)
	 *
	 * @param <T> The type of instance to register
	 */
	public interface BindBuilder<T> {

		/**
		 * Registers a Guice key
		 *
		 * @param key The key to register
		 */
		public abstract void with(Key<? extends T> key);

		/**
		 * Registers a type to be retrieved with Guice
		 *
		 * @param type The type to register
		 */
		public abstract void with(Class<? extends T> type);

		/**
		 * Registers a type to be retrieved with Guice
		 *
		 * @param type The type to register
		 */
		public abstract void with(TypeLiteral<? extends T> type);

		/**
		 * Registers a pre-created instance
		 *
		 * @param instance The object to register
		 */
		public abstract void withInstance(T instance);

		/**
		 * Registers a pre-created instance
		 *
		 * @param instance The object to register
		 * @param type The exact type of the object to register
		 */
		public abstract void withInstance(T instance, TypeLiteral<T> type);

	}

	/**
	 * First method of the syntax convertTo(class).with(converter)
	 *
	 * @param toType The class to convert to using the later converter
	 * @param toTypes Any additional class to convert to
	 * @return BindBuilder on which with() or withInstance() must be called
	 */
	public abstract BindBuilder<Converter<?>> convertTo(Class<?> toType, Class<?>... toTypes);

	/**
	 * First method of the syntax handleException(class).with(handler)
	 *
	 * @param exc The exception class to handle using the later handler
	 * @param excs Any additional class to convert to
	 * @return BindBuilder on which with() or withInstance() must be called
	 */
	@SuppressWarnings("unchecked")
	public abstract <T extends Exception> BindBuilder<ExceptionHandler<T>> handleException(Class<? extends T> exc, Class<? extends T>... excs);

	/**
	 * Create a new {@link SGInvocationFactory} associated with the given module's {@link Binder}
	 *
	 * @param binder The binder that will be requested for injection
	 * @return The factory
	 */
	public abstract SGInvocationFactory newInvocationFactory(Binder binder);

	/**
	 * Module to install once you have fully configured SourGuice MVC.
	 * Should be used with {@link AbstractModule#install}: <pre>install(sourGuice.module())</pre>
	 *
	 * Careful: no other method of the SourGuice object should be called after calling this
	 *
	 * @return The module to install
	 */
	public Module module();
}
