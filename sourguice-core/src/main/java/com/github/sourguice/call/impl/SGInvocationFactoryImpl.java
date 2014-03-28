package com.github.sourguice.call.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Singleton;

import com.github.sourguice.annotation.request.InterceptParam;
import com.github.sourguice.annotation.request.RequestAttribute;
import com.github.sourguice.annotation.request.RequestHeader;
import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.annotation.request.SessionAttribute;
import com.github.sourguice.call.ArgumentFetcher;
import com.github.sourguice.call.ArgumentFetcherFactory;
import com.github.sourguice.call.SGInvocation;
import com.github.sourguice.call.SGInvocationFactory;
import com.github.sourguice.controller.fetchers.InjectorArgumentFetcher;
import com.github.sourguice.controller.fetchers.NullArgumentFetcher;
import com.github.sourguice.controller.fetchers.RequestAttributeArgumentFetcher;
import com.github.sourguice.controller.fetchers.RequestHeaderArgumentFetcher;
import com.github.sourguice.controller.fetchers.RequestParamArgumentFetcher;
import com.github.sourguice.controller.fetchers.SessionAttributeArgumentFetcher;
import com.github.sourguice.utils.Annotations;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;

/**
 * Factory responsible for creating {@link SGInvocation}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Singleton
public final class SGInvocationFactoryImpl implements SGInvocationFactory {

	/**
	 * The guice binder
	 */
	private final Binder binder;

	/**
	 * Constructor with arguments to be injected by Guice
	 */
	@SuppressWarnings("javadoc")
	public SGInvocationFactoryImpl(final Binder binder) {
		this.binder = binder;
	}

	@Override
	public SGInvocation newInvocation(final TypeLiteral<?> controllerType, final Method method, final ArgumentFetcherFactory... factories) {
		final ArgumentFetcher<?>[] fetchers = new ArgumentFetcher<?>[method.getParameterTypes().length];
		final List<TypeLiteral<?>> argTypes = controllerType.getParameterTypes(method);
		for (int arg = 0; arg < method.getParameterTypes().length; ++arg) {
			final TypeLiteral<?> argType = argTypes.get(arg);
			for (final ArgumentFetcherFactory factory : factories) {
				fetchers[arg] = factory.create(method, arg, argType);
				if (fetchers[arg] != null) {
					break ;
				}
			}
			if (fetchers[arg] == null) {
				final String methodName = method.getDeclaringClass().getCanonicalName() + "." + method.getName();
				fetchers[arg] = createFetcher(argType, method.getParameterAnnotations()[arg], methodName);
			}
			this.binder.requestInjection(fetchers[arg]);
		}

		final SGInvocationImpl ret = new SGInvocationImpl(method, fetchers);
		this.binder.requestInjection(ret);
		return ret;
	}

	/**
	 * Creates the appropriate fetcher for the given argument
	 *
	 * @param type The argument's type
	 * @param annotations The argument's annotation
	 * @param methodName The name of the method whose argument we are fetching
	 * @return The appropriate argument fetcher
	 */
	private static <T> ArgumentFetcher<T> createFetcher(final TypeLiteral<T> type, final Annotation[] annotations, final String methodName) {
		final AnnotatedElement annos = Annotations.fromArray(annotations);

		final RequestParam requestParam = annos.getAnnotation(RequestParam.class);
		if (requestParam != null) {
			return new RequestParamArgumentFetcher<>(type, requestParam, methodName);
		}

		final RequestAttribute requestAttribute = annos.getAnnotation(RequestAttribute.class);
		if (requestAttribute != null) {
			return new RequestAttributeArgumentFetcher<>(type, requestAttribute);
		}

		final SessionAttribute sessionAttribute = annos.getAnnotation(SessionAttribute.class);
		if (sessionAttribute != null) {
			return new SessionAttributeArgumentFetcher<>(type, sessionAttribute);
		}

		final RequestHeader requestHeader = annos.getAnnotation(RequestHeader.class);
		if (requestHeader != null) {
			return new RequestHeaderArgumentFetcher<>(type, requestHeader, methodName);
		}

		final InterceptParam interceptParam = annos.getAnnotation(InterceptParam.class);
		if (interceptParam != null) {
			return new NullArgumentFetcher<>();
		}

		return new InjectorArgumentFetcher<>(type, annotations);
	}

}
