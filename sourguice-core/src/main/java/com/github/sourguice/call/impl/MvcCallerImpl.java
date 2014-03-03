package com.github.sourguice.call.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.annotation.controller.Callable;
import com.github.sourguice.annotation.request.PathVariable;
import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.call.CalltimeArgumentFetcher;
import com.github.sourguice.call.MvcCaller;
import com.github.sourguice.controller.ControllerHandler;
import com.github.sourguice.controller.ControllerHandlersRepository;
import com.github.sourguice.controller.GuiceInstanceGetter;
import com.github.sourguice.controller.InstanceGetter;
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
	 * Invocation cache, used to access directly an invocation without researching it if it already has been searched
	 */
	private final Map<String, MvcInvocation> invocationCache = new HashMap<>();

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

//	/**
//	 * @return The request that is registered for this caller
//	 */
//	@Override
//	public HttpServletRequest getReq() {
//		return req;
//	}
//
//	/**
//	 * Sets the request registered for this caller
//	 * This can be used to register a HttpServletRequestWrapper
//	 * This WILL NOT replace the HttpServletRequest returned by Guice
//	 * BUT all HttpServletRequest required by @{@link RequestMapping} annotated method will get this request instead of the one registered in Guice
//	 * @param req The Request to set
//	 */
//	@Override
//	public void setReq(HttpServletRequest req) {
//		this.req = req;
//	}
//
//	/**
//	 * @return The response that is registered for this caller
//	 */
//	@Override
//	public HttpServletResponse getRes() {
//		return res;
//	}
//
//	/**
//	 * Sets the response registered for this caller
//	 * This can be used to register a HttpServletResponseWrapper
//	 * This WILL NOT replace the HttpServletResponse returned by Guice
//	 * BUT all HttpServletResponse required by @{@link RequestMapping} annotated method will get this response instead of the one registered in Guice
//	 * @param res The Response to set
//	 */
//	@Override
//	public void setRes(HttpServletResponse res) {
//		this.res = res;
//	}

	private MvcInvocation findInvocation(final InstanceGetter<?> controller, final String methodName) throws NoSuchMethodException {
		final ControllerHandler<?> handler = this.repo.get(controller);

		MvcInvocation invocation = this.invocationCache.get(methodName);

		if (invocation == null) {
			for (final MvcInvocation check : handler.getInvocations()) {
				if (check.getMethod().getName().equals(methodName)) {
					this.invocationCache.put(methodName, check);
					invocation = check;
					break ;
				}
			}
		}

		if (invocation == null) {
			throw new NoSuchMethodException("No such method @Callable " + controller.getTypeLiteral().getRawType().getCanonicalName() + "." + methodName);
		}

		return invocation;
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public @CheckForNull Object call(InstanceGetter<?> ig, String methodName, @CheckForNull @PathVariablesMap Map<String, String> pathVariables, boolean throwWhenHandled, CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, Throwable {
//		return call(findInvocation(ig, methodName), pathVariables, throwWhenHandled, additionalFetchers);
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public @CheckForNull Object call(InstanceGetter<?> ig, Method method, @CheckForNull @PathVariablesMap Map<String, String> pathVariables, boolean throwWhenHandled, CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, Throwable {
//		return call(findInvocation(ig, method.getName()), pathVariables, throwWhenHandled, additionalFetchers);
//	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @CheckForNull Object call(Class<?> cls, final String methodName, final @CheckForNull @PathVariablesMap Map<String, String> pathVariables, final boolean throwWhenHandled, final CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchMethodException, NoSuchRequestParameterException, Throwable {
		if (cls.getSimpleName().contains("$$EnhancerByGuice$$")) {
			cls = cls.getSuperclass();
		}
		final GuiceInstanceGetter<?> controller = new GuiceInstanceGetter<>(Key.get(cls));
		this.injector.injectMembers(controller);
		return call(findInvocation(controller, methodName), pathVariables, throwWhenHandled, additionalFetchers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @CheckForNull Object call(Class<?> cls, Method method, final @CheckForNull @PathVariablesMap Map<String, String> pathVariables, final boolean throwWhenHandled, final CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, Throwable {
		if (cls.getSimpleName().contains("$$EnhancerByGuice$$")) {
			cls = cls.getSuperclass();
			method = cls.getMethod(method.getName(), method.getParameterTypes());
		}
		final GuiceInstanceGetter<?> controller = new GuiceInstanceGetter<>(Key.get(cls));
		this.injector.injectMembers(controller);
		return call(findInvocation(controller, method.getName()), pathVariables, throwWhenHandled, additionalFetchers);
	}

	/**
	 * Executes a call to a given invocation
	 * This should only be used internally as users are not supposed to handle Invocation objects
	 *
	 * @param invoc The Invocation to invoke
	 * @see MvcCallerImpl#call(MvcInvocation, Map, boolean, CalltimeArgumentFetcher...)
	 */
	public @CheckForNull Object call(final MvcInvocation invoc, final @CheckForNull @PathVariablesMap Map<String, String> pathVariables, final boolean throwWhenHandled, final CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, Throwable {
		try {
			assert(this.req != null);
			assert(this.res != null);
			assert(this.injector != null);
			return invoc.invoke(this.req, pathVariables, this.injector, additionalFetchers);
		}
		catch (Exception exception) {
			return handleException(exception, throwWhenHandled);
		}
	}

	/**
	 * Executes a call to a given invocation with the MatchResult to transform to PathVariables according to @{@link RequestMapping} and @{@link PathVariable} annotations
	 * This should only be used internally as users are not supposed to handle Invocation objects
	 *
	 * @param invoc The Invocation to invoke
	 * @see MvcCallerImpl#call(MvcInvocation, Map, boolean, CalltimeArgumentFetcher...)
	 */
	public @CheckForNull Object call(final MvcInvocation invoc, final MatchResult urlMatch, final boolean throwWhenHandled, final CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, Throwable {
		try {
			return invoc.invoke(this.req, urlMatch, this.injector, additionalFetchers);
		}
		catch (Exception exception) {
			return handleException(exception, throwWhenHandled);
		}
	}

	@SuppressWarnings({"unchecked", "PMD.SignatureDeclareThrowsException"})
	private @CheckForNull <T extends Exception> Object handleException(final T exception, final boolean throwWhenHandled) throws Exception {
		final ExceptionHandler<T> handler = (ExceptionHandler<T>) this.exceptionService.getHandler(exception.getClass());
		if (handler != null && handler.handle(exception, this.req, this.res)) {
			if (throwWhenHandled) {
				throw new HandledException(exception);
			}
			return null;
		}
		throw exception;
	}
}
