package com.github.sourguice;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.annotation.CheckForNull;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.call.SGInvocationFactory;
import com.github.sourguice.call.impl.SGInvocationFactoryImpl;
import com.github.sourguice.conversion.ConversionService;
import com.github.sourguice.conversion.Converter;
import com.github.sourguice.conversion.def.BooleanConverter;
import com.github.sourguice.conversion.def.DoubleConverter;
import com.github.sourguice.conversion.def.EnumConverter;
import com.github.sourguice.conversion.def.FloatConverter;
import com.github.sourguice.conversion.def.IntegerConverter;
import com.github.sourguice.conversion.def.LongConverter;
import com.github.sourguice.conversion.def.ShortConverter;
import com.github.sourguice.conversion.def.StringConverter;
import com.github.sourguice.conversion.impl.ConversionServiceImpl;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.exception.ExceptionService;
import com.github.sourguice.exception.impl.ExceptionServiceImpl;
import com.github.sourguice.intercept.InterceptWithInterceptor;
import com.github.sourguice.intercept.InterceptWithMatcher;
import com.github.sourguice.provider.TypedProvider;
import com.github.sourguice.provider.TypedProviderSingleBindBuilder;
import com.github.sourguice.provider.GTPModuleFactory;
import com.github.sourguice.utils.SGCallInterceptSetter;
import com.github.sourguice.value.RequestMethod;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.RequestScoped;

/**
 * This is used by {@link SourGuice} to actually bind the implementations of SourGuices classes
 * This is needed because SourGuice builds two different jars : one for API and one for implementation
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@SuppressWarnings({"static-method", "PMD.TooManyMethods"})
public class SourGuiceModuleImpl extends AbstractModule implements SourGuiceModule {

	/**
	 * List of providers that need injection and that we created before calling {@link #module()}
	 */
	private @CheckForNull GTPModuleFactory gtpFactory = new GTPModuleFactory();

	/**
	 * Conversion service
	 */
	protected final ConversionServiceImpl conversionService = new ConversionServiceImpl();

	/**
	 * Exception service
	 */
	protected final ExceptionServiceImpl exceptionService = new ExceptionServiceImpl();

	/**
	 * Registers in guice the PrintWriter class to be binded to the request's response writer
	 *
	 * @param res The current HTTP response object
	 * @return The response writer
	 * @throws IOException If an input or output exception occurs
	 */
	@Provides @RequestScoped public PrintWriter getRequestPrintWriter(final HttpServletResponse res) throws IOException {
		return res.getWriter();
	}

	/**
	 * Registers in guice the Writer class to be binded to the request's response writer
	 *
	 * @param res The current HTTP response object
	 * @return The response writer
	 * @throws IOException If an input or output exception occurs
	 */
	@Provides @RequestScoped public Writer getRequestWriter(final HttpServletResponse res) throws IOException {
		return res.getWriter();
	}

	/**
	 * Registers in guice the ServletOutputStream class to be binded to the request's response output stream
	 *
	 * @param res The current HTTP response object
	 * @return The response stream
	 * @throws IOException If an input or output exception occurs
	 */
	@Provides @RequestScoped public ServletOutputStream getRequestServletOutputStream(final HttpServletResponse res) throws IOException {
		return res.getOutputStream();
	}

	/**
	 * Registers in guice the OutputStream class to be binded to the request's response output stream
	 *
	 * @param res The current HTTP response object
	 * @return The response stream
	 * @throws IOException If an input or output exception occurs
	 */
	@Provides @RequestScoped public OutputStream getRequestOutputStream(final HttpServletResponse res) throws IOException {
		return res.getOutputStream();
	}

	/**
	 * Registers in guice the RequestMethod enum to be binded to the request's method
	 *
	 * @param req The current HTTP request object
	 * @return The RequestMethod of the current request
	 */
	@Provides @RequestScoped public RequestMethod getRequestMethod(final HttpServletRequest req) {
		return RequestMethod.valueOf(req.getMethod());
	}

	@Override
	@SuppressWarnings({"PMD.AvoidUsingShortType"})
	protected final void configure() {

		// Registers default converters
		convertTo(boolean.class, Boolean.class).withInstance(new BooleanConverter());
		convertTo(short.class, Short.class).withInstance(new ShortConverter());
		convertTo(int.class, Integer.class).withInstance(new IntegerConverter());
		convertTo(long.class, Long.class).withInstance(new LongConverter());
		convertTo(float.class, Float.class).withInstance(new FloatConverter());
		convertTo(double.class, Double.class).withInstance(new DoubleConverter());
		convertTo(Enum.class).withInstance(new EnumConverter());
		convertTo(String.class).withInstance(new StringConverter());

		// Binds the services
		bind(ConversionService.class).toInstance(this.conversionService);
		bind(ExceptionService.class).toInstance(this.exceptionService);

		// Binds Intercept
		bind(SGCallInterceptSetter.class);

		// Binds interceptors
		final InterceptWithInterceptor interceptor = new InterceptWithInterceptor();
		requestInjection(interceptor);
		bindInterceptor(Matchers.any(), new InterceptWithMatcher(), interceptor);
		bindInterceptor(new InterceptWithMatcher(), Matchers.any(), interceptor);

		assert this.gtpFactory != null;
		this.gtpFactory.requestInjection(binder());

		this.gtpFactory = null;
	}

	@Override
	public final SingleBindBuilder<Converter<?>> convertTo(final Class<?> toType, final Class<?>... toTypes) {
		return new TypedProviderSingleBindBuilder<Converter<?>>(this.gtpFactory) {
			@Override protected void register(final TypedProvider<? extends Converter<?>> converter) {
				SourGuiceModuleImpl.this.conversionService.register(toType, converter);
				for (final Class<?> addToType : toTypes) {
					SourGuiceModuleImpl.this.conversionService.register(addToType, converter);
				}
			}
		};
	}

	@Override @SafeVarargs
	public final <T extends Exception> SingleBindBuilder<ExceptionHandler<T>> handleException(final Class<? extends T> exc, final Class<? extends T>... excs) {
		return new TypedProviderSingleBindBuilder<ExceptionHandler<T>>(this.gtpFactory) {
			@Override protected void register(final TypedProvider<? extends ExceptionHandler<T>> handler) {
				SourGuiceModuleImpl.this.exceptionService.register(exc, handler);
				for (final Class<? extends T> addExc : excs) {
					SourGuiceModuleImpl.this.exceptionService.register(addExc, handler);
				}
			}
		};
	}

	@Override
	public SGInvocationFactory newInvocationFactory(final Binder binder) {
		return new SGInvocationFactoryImpl(binder);
	}

	@Override
	public Module module() {
		return this;
	}
}
