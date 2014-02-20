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
	private HttpServletRequest req;

	/**
	 * The response used to bind ServletResponse arguments
	 */
	private HttpServletResponse res;

	/**
	 * The repository containing all ControllerHandlers, used to get a ControllerHandler from a controller class
	 */
	private ControllerHandlersRepository repo;

	/**
	 * The exception service (taken from Guice) to handle any exception thrown by the method called
	 */
	private ExceptionService exceptionService;

	/**
	 * The Guice Injector from which to retrieve arguments
	 */
	private Injector injector;

	/**
	 * Invocation cache, used to access directly an invocation without researching it if it already has been searched
	 */
	private Map<String, MvcInvocation> invocationCache = new HashMap<>();

	/**
	 * Constructor with arguments to be injected by Guice
	 */
	@SuppressWarnings("javadoc")
	@Inject
	public MvcCallerImpl(HttpServletRequest req, HttpServletResponse res, Injector injector) {
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

	private MvcInvocation findInvocation(InstanceGetter<?> ig, String methodName) throws NoSuchMethodException {
		ControllerHandler<?> handler = repo.get(ig);

		MvcInvocation invoc = invocationCache.get(methodName);

		if (invoc == null)
			for (MvcInvocation i : handler.getInvocations())
				if (i.getMethod().getName().equals(methodName)) {
					invocationCache.put(methodName, i);
					invoc = i;
					break ;
				}

		if (invoc == null)
			throw new NoSuchMethodException("No such method @Callable " + ig.getInstanceClass().getCanonicalName() + "." + methodName);

		return invoc;
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
	public @CheckForNull Object call(Class<?> cls, String methodName, @CheckForNull @PathVariablesMap Map<String, String> pathVariables, boolean throwWhenHandled, CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchMethodException, NoSuchRequestParameterException, Throwable {
		if (cls.getSimpleName().contains("$$EnhancerByGuice$$"))
			cls = cls.getSuperclass();
		GuiceInstanceGetter<?> ig = new GuiceInstanceGetter<>(cls);
		injector.injectMembers(ig);
		return call(findInvocation(ig, methodName), pathVariables, throwWhenHandled, additionalFetchers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @CheckForNull Object call(Class<?> cls, Method method, @CheckForNull @PathVariablesMap Map<String, String> pathVariables, boolean throwWhenHandled, CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, Throwable {
		if (cls.getSimpleName().contains("$$EnhancerByGuice$$")) {
			cls = cls.getSuperclass();
			method = cls.getMethod(method.getName(), method.getParameterTypes());
		}
		GuiceInstanceGetter<?> ig = new GuiceInstanceGetter<>(cls);
		injector.injectMembers(ig);
		return call(findInvocation(ig, method.getName()), pathVariables, throwWhenHandled, additionalFetchers);
	}

	/**
	 * Executes a call to a given invocation
	 * This should only be used internally as users are not supposed to handle Invocation objects
	 *
	 * @param invoc The Invocation to invoke
	 * @see MvcCallerImpl#call(MvcInvocation, Map, boolean, CalltimeArgumentFetcher...)
	 */
	public @CheckForNull Object call(MvcInvocation invoc, @CheckForNull @PathVariablesMap Map<String, String> pathVariables, boolean throwWhenHandled, CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, Throwable {
		try {
			assert(req != null);
			assert(res != null);
			assert(injector != null);
			return invoc.Invoke(req, pathVariables, injector, additionalFetchers);
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
	public @CheckForNull Object call(MvcInvocation invoc, MatchResult urlMatch, boolean throwWhenHandled, CalltimeArgumentFetcher<?>... additionalFetchers) throws HandledException, NoSuchRequestParameterException, Throwable {
		try {
			return invoc.Invoke(req, urlMatch, injector, additionalFetchers);
		}
		catch (Exception exception) {
			return handleException(exception, throwWhenHandled);
		}
	}

	private @CheckForNull <T extends Exception> Object handleException(T exception, boolean throwWhenHandled) throws Exception {
		ExceptionHandler<T> handler = (ExceptionHandler<T>) exceptionService.getHandler(exception.getClass());
		if (handler != null && handler.handle(exception, req, res)) {
			if (throwWhenHandled)
				throw new HandledException(exception);
			return null;
		}
		throw exception;
	}
}
