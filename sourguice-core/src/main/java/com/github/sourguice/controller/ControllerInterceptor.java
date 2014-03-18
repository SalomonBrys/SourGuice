package com.github.sourguice.controller;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.CheckForNull;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.github.sourguice.annotation.controller.InterceptWith;
import com.github.sourguice.utils.Annotations;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * This interceptor will get all @{@link InterceptWith} annotations on the given method.
 * It will then construct a recursive MethodInvocation tree.
 * Each node defers its execution to the interceptor given in the annotation.
 * That way, if an interceptor chooses to stop the invocation and *not* call {@link MethodInvocation#proceed()},
 * the contained interceptors will not be called.
 * The tree is constructed with {@link Annotations#getAllTreeRecursive(Class, java.lang.reflect.AnnotatedElement)}
 * which means that the "closest" annotation will be used first.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Singleton
public class ControllerInterceptor implements MethodInterceptor {

	/**
	 * Injector used to fetch actual Interceptors
	 */
	@Inject protected @CheckForNull Injector injector;

	/**
	 * Cache that binds each method to its InterceptWith list so each list has to be computed only once.
	 */
	private final Map<Method, List<InterceptWith>> interceptCache = new ConcurrentHashMap<>();

	/**
	 * Wrapper around {@link MethodInvocation} to allow the invocation to be intercepted
	 * by a guice registered MethodInterceptor
	 *
	 * @author Salomon BRYS <salomon.brys@gmail.com>
	 */
	private class InterceptorInvocation implements MethodInvocation {

		/**
		 * The original MethodInvocation
		 */
		private final MethodInvocation invocation;

		/**
		 * The class of the interceptor. An instance will be asked to guice when proceeding
		 */
		private final Class<? extends MethodInterceptor> interceptorClass;

		/**
		 * @param invocation The original MethodInvocation
		 * @param interceptor The class of the interceptor. An instance will be asked to guice when proceeding
		 */
		InterceptorInvocation(final MethodInvocation invocation, final Class<? extends MethodInterceptor> interceptor) {
			super();
			this.invocation = invocation;
			this.interceptorClass = interceptor;
		}

		/**
		 * Wrapper proxy
		 */
		@Override public AccessibleObject getStaticPart() { return this.invocation.getStaticPart(); }

		/**
		 * Wrapper proxy
		 */
		@Override public Object getThis() { return this.invocation.getThis(); }

		/**
		 * Wrapper proxy
		 */
		@Override public Object[] getArguments() { return this.invocation.getArguments(); }

		/**
		 * Wrapper proxy
		 */
		@Override public Method getMethod() { return this.invocation.getMethod(); }

		/**
		 * Will delay the actual invocation to the guice fetched MethodInterceptor
		 */
		@Override
		public Object proceed() throws Throwable {
			assert ControllerInterceptor.this.injector != null;
			return ControllerInterceptor.this.injector.getInstance(this.interceptorClass).invoke(this.invocation);
		}

	}

	/**
	 * Constructs the {@link MethodInvocation} tree and launches the execution of the found interceptors.
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {

		final Method method = invocation.getMethod();

		// We first check if this has already been computed
		List<InterceptWith> interceptAnnos = this.interceptCache.get(method);
		// If it has not been, then we need a lock
		if (interceptAnnos == null) {
			synchronized (method) {
				// Maybe it has been computed while we waited for the lock, so we check again
				interceptAnnos = this.interceptCache.get(method);
				// It has not, so let's compute it!
				if (interceptAnnos == null) {
					interceptAnnos = Annotations.getAllTreeRecursive(InterceptWith.class, method);
					this.interceptCache.put(method, interceptAnnos);
				}
			}
		}

		for (final InterceptWith interceptWith : interceptAnnos) {
			for (final Class<? extends MethodInterceptor> interceptor : interceptWith.value()) {
				invocation = new InterceptorInvocation(invocation, interceptor);
			}
		}

		return invocation.proceed();
	}
}
