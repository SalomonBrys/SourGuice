package com.github.sourguice;

import com.github.sourguice.conversion.Converter;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.view.ViewRenderer;
import com.google.inject.AbstractModule;
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
	 * Interface returned by {@link SourGuice#redirect(String, String...)} to permit the syntax redirect(pattern).to(path)
	 */
	public static interface RedirectBuilder {
		/**
		 * Second method of the syntax redirect(pattern).to(path)
		 * Associates previously defined pattern to the path
		 *
		 * @param path The path on which redirect
		 */
		@SuppressWarnings("PMD.ShortMethodName")
		public void to(String path);
	}

	/**
	 * First method of the syntax control(pattern).with(controller.class)
	 *
	 * @param pattern The pattern to register for the later controller
	 * @param patterns Any additional patterns to register
	 * @return BindBuilder on which with() or withInstance() must be called
	 */
	public abstract BindBuilder<Object> control(String pattern, String... patterns);

	/**
	 * First method of the syntax redirect(pattern).to(path)
	 *
	 * @param pattern The pattern to redirect to the later path
	 * @param patterns Any additional patterns to redirect
	 * @return RedirectBuilder on which to() must be called
	 */
	public abstract RedirectBuilder redirect(String pattern, String... patterns);

	/**
	 * First method of the syntax renderViews(pattern).with(viewRenderer)
	 *
	 * @param regex The name regular expression to register to the later view renderer
	 * @param regexs Any additional name regular expression to register
	 * @return BindBuilder on which with() or withInstance() must be called
	 */
	public abstract BindBuilder<ViewRenderer> renderViews(String regex, String... regexs);

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
	 * Module to install once you have fully configured SourGuice MVC.
	 * Should be used with {@link AbstractModule#install}: <pre>install(sourGuice.module())</pre>
	 *
	 * Careful: no other method of the SourGuice object should be called after calling this
	 *
	 * @return The module to install
	 */
	public Module module();
}
