package com.github.sourguice.call.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.MatchResult;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.annotation.controller.Callable;
import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.call.CalltimeArgumentFetcher;
import com.github.sourguice.call.MvcCaller;
import com.github.sourguice.controller.ControllerHandlersRepository;
import com.github.sourguice.controller.GuiceInstanceGetter;
import com.github.sourguice.controller.MvcInvocation;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.exception.ExceptionService;
import com.github.sourguice.throwable.invocation.HandledException;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.RequestScoped;

/**
 * Permits to call any @{@link Callable} annotated method by fetching automatically all its arguments
 * Arguments of the method to call may be:
 *  - Fetchable by any given CalltimeArgumentFetcher
 *  - Annotated with anotations from the {@link com.github.sourguice.annotation.request} package
 *  - Retrivable from Guice
 * As all atempts to fetch and bind arguments are in this order, which means that if an argument is not fetchable by any means, Guice will raise an exception.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@RequestScoped
public final class MvcCallerImpl implements MvcCaller {

	/**
	 * The request used to bind ServletRequest related arguments
	 */
	private final HttpServletRequest req;

	/**
	 * The response used to bind ServletResponse arguments
	 */
	private final HttpServletResponse res;

	/**
	 * The repository containing all ControllerHandlers, used to get a ControllerHandler from a controller class
	 */
	private final ControllerHandlersRepository repo;

	/**
	 * The exception service (taken from Guice) to handle any exception thrown by the method called
	 */
	private final ExceptionService exceptionService;

	/**
	 * The Guice Injector from which to retrieve arguments
	 */
	private final Injector injector;

	/**
	 * Constructor with arguments to be injected by Guice
	 */
	@SuppressWarnings("javadoc")
	@Inject
	public MvcCallerImpl(final HttpServletRequest req, final HttpServletResponse res, final Injector injector) {
		this.req = req;
		this.res = res;
		this.injector = injector;
		this.repo = injector.getInstance(ControllerHandlersRepository.class);
		this.exceptionService = injector.getInstance(ExceptionService.class);
	}

	@Override
	@SuppressWarnings("PMD.EmptyCatchBlock")
	public @CheckForNull Object call(Class<?> cls, Method method, final @CheckForNull @PathVariablesMap Map<String, String> pathVariables, final boolean throwWhenHandled, final CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, InvocationTargetException, IOException {
		if (cls.getSimpleName().contains("$$EnhancerByGuice$$")) {
			cls = cls.getSuperclass();
			try {
				method = cls.getMethod(method.getName(), method.getParameterTypes());
			}
			catch (NoSuchMethodException e) {
				// Can be ignored: The method should stay itself
			}
		}
		final GuiceInstanceGetter<?> controller = new GuiceInstanceGetter<>(Key.get(cls));
		this.injector.injectMembers(controller);
		return call(this.repo.get(controller).getInvocations(method), pathVariables, throwWhenHandled, additionalFetchers);
	}

	/**
	 * Executes a call to a given invocation
	 * This should only be used internally as users are not supposed to handle Invocation objects
	 *
	 * @param invoc The Invocation to invoke
	 * @param pathVariables The Path Variables Map
	 * @param throwWhenHandled Whether to throw a {@link HandledException} when an exception has been caught and handled.
	 *                         This allows to cancel all future work until the {@link HandledException} has been caught (and ignored).
	 * @param additionalFetchers Any additional fetcher to use at call time
	 * @return Whatever the call returned
	 * @throws HandledException If an exception has been caught and handled. It is safe to ignore and used to cancel any depending work.
	 * @throws NoSuchRequestParameterException In case of a parameter asked from request argument or path variable that does not exists
	 * @throws InvocationTargetException Any exception thrown by the method call and that was not handled
	 * @throws IOException IO failure while writing the response
	 * @see MvcCallerImpl#call(MvcInvocation, Map, boolean, CalltimeArgumentFetcher...)
	 */
	public @CheckForNull Object call(final MvcInvocation invoc, final @CheckForNull @PathVariablesMap Map<String, String> pathVariables, final boolean throwWhenHandled, final CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, InvocationTargetException, IOException {
		try {
			assert(this.req != null);
			assert(this.res != null);
			assert(this.injector != null);
			return invoc.invoke(this.req, pathVariables, this.injector, additionalFetchers);
		}
		catch (InvocationTargetException exception) {
			handleException(exception, throwWhenHandled);
			return null;
		}
	}

	/**
	 * @see #call(MvcInvocation, Map, boolean, CalltimeArgumentFetcher...)
	 */
	@SuppressWarnings("javadoc")
	public @CheckForNull Object call(final MvcInvocation invoc, final MatchResult urlMatch, final boolean throwWhenHandled, final CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, InvocationTargetException, IOException {
		try {
			return invoc.invoke(this.req, urlMatch, this.injector, additionalFetchers);
		}
		catch (InvocationTargetException exception) {
			handleException(exception, throwWhenHandled);
			return null;
		}
	}

	/**
	 * Handles any exception thrown by an invocation
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
		final ExceptionHandler<Exception> handler = (ExceptionHandler<Exception>) this.exceptionService.getHandler(exception.getClass());
		if (handler != null && handler.handle(exception, this.req, this.res)) {
			if (throwWhenHandled) {
				throw new HandledException(exception);
			}
			return ;
		}
		throw invocException;
	}
}
