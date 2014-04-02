package com.github.sourguice;

import com.github.sourguice.call.SGInvocationFactory;
import com.github.sourguice.conversion.Converter;
import com.github.sourguice.exception.ExceptionHandler;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

/**
 * This class is where you can configure your MVC bindings
 * To configure a SourGuice MVC module, create a usual {@link ServletModule}
 * in its {@link ServletModule#configureServlets} method,
 * create a SourGuice object, configure it, then install it's module.
 *
 <pre>
	public static class DemoModule extends ServletModule {
		@Override
		protected void configureServlets() {
			SourGuice sg = new SourGuice();
			sg.control("/*", MyController.class);
			install(sg.module);
		}
	}
 </pre>
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public class SourGuice implements SourGuiceModule {

	/**
	 * The helper proxy on which every implementation call will be forwarded
	 * Found by reflexivity in the core jar.
	 */
	private final SourGuiceModule implementation;

	/**
	 * @param implementation The SourGuice core implementation
	 */
	public SourGuice(final SourGuice implementation) {
		super();
		this.implementation = implementation;
	}

	/**
	 * Usual constructor
	 *
	 * This will check that the implementation jar is actually in the classpath and find the implementation
	 */
	public SourGuice() {
		super();
		try {
			this.implementation = (SourGuiceModule)
					Class
						.forName("com.github.sourguice.SourGuiceModuleImpl")
						.getConstructor()
						.newInstance();
		}
		catch (ReflectiveOperationException e) {
			throw new UnsupportedOperationException("Cannot find SourGuice Implementation, make sure it is deployed with your application", e);
		}
	}

	@Override
	public final BindBuilder<Converter<?>> convertTo(final Class<?> toType, final Class<?>... toTypes) {
		return this.implementation.convertTo(toType, toTypes);
	}

	@Override
	@SafeVarargs
	public final <T extends Exception> BindBuilder<ExceptionHandler<T>> handleException(final Class<? extends T> exc, final Class<? extends T>... excs) {
		return this.implementation.handleException(exc, excs);
	}

	@Override
	public SGInvocationFactory newInvocationFactory(final Binder binder) {
		return this.implementation.newInvocationFactory(binder);
	}

	@Override
	public Module module() {
		return this.implementation.module();
	}

}
