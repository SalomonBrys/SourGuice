package com.github.sourguice.call.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;

import com.github.sourguice.call.ArgumentFetcher;
import com.github.sourguice.call.SGInvocation;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.exception.ExceptionService;
import com.github.sourguice.throwable.invocation.HandledException;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;

/**
 * {@link SGInvocation} implementation.
 * Holds everything needed to call a method: The method itself and list of {@link ArgumentFetcher} to get its arguments.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public final class SGInvocationImpl implements SGInvocation {

	/**
	 * The exception service (taken from Guice) to handle any exception thrown by the method called
	 */
	@Inject
	private @CheckForNull Provider<ExceptionService> exceptionServiceProvider;

	/**
	 * All fetchers for each arguments of the method
	 */
	private final ArgumentFetcher<?>[] fetchers;

	/**
	 * The method of this Invocation
	 */
	private final Method method;

	/**
	 * Constructor
	 *
	 * @param method The method of this Invocation
	 * @param fetchers All fetchers for each arguments of the method
	 */
	public SGInvocationImpl(final Method method, final ArgumentFetcher<?>[] fetchers) {
		super();
		this.method = method;
		this.fetchers = fetchers;
	}

	@Override
	public @CheckForNull Object invoke(final Object controller, final boolean throwWhenHandled) throws NoSuchRequestParameterException, InvocationTargetException, HandledException, IOException {

		// Fetches all arguments
		Object[] params = new Object[this.fetchers.length];
		Object ret = null;
		for (int n = 0; n < this.fetchers.length; ++n) {
			params[n] = this.fetchers[n].getPrepared();
		}

		try {
			// Calls the method
			ret = this.method.invoke(controller, params);
		}
		catch (IllegalAccessException e) {
			throw new UnsupportedOperationException(e);
		}
		catch (InvocationTargetException exception) {
			handleException(exception, throwWhenHandled);
			return null;
		}

		// Returns whatever the method call returned
		return ret;
	}

	/**
	 * Handles any exception thrown by a method invocation
	 *
	 * @param invocException The exception to handle
	 * @param throwWhenHandled Whether to throw a HandledException when the exception was handled
	 * @throws HandledException If an exception was handled and throwWhenHandled is true
	 * @throws InvocationTargetException The given exception if it was not handled
	 * @throws IOException If an input or output exception occurs during response manipulation
	 */
	@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "unchecked"})
	private @CheckForNull void handleException(final InvocationTargetException invocException, final boolean throwWhenHandled) throws HandledException, InvocationTargetException, IOException {
		final Throwable thrown = invocException.getCause();
		if (!(thrown instanceof Exception)) {
			throw invocException;
		}
		if (thrown instanceof HandledException) {
			throw (HandledException) thrown;
		}
		final Exception exception = (Exception) thrown;
		assert this.exceptionServiceProvider != null;
		final ExceptionHandler<Exception> handler = (ExceptionHandler<Exception>) this.exceptionServiceProvider.get().getHandler(exception.getClass());
		if (handler != null && handler.handle(exception)) {
			if (throwWhenHandled) {
				throw new HandledException(exception);
			}
			return ;
		}
		throw invocException;
	}

}
