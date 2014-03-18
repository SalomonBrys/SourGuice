package com.github.sourguice.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.github.sourguice.annotation.request.InterceptParam;
import com.google.inject.Singleton;

/**
 * Util class for {@link MethodInterceptor}s
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Singleton
public class SGCallInterceptSetter {

	/**
	 * Cache that associates a map of match association to a method
	 */
	private final Map<Method, Map<String, Integer>> posCache = new HashMap<>();

	/**
	 * Find the position of the {@link InterceptParam} annotated method parameter with the correct key.
	 * When found, put it in the cache map
	 *
	 * @param method The method whose parameter to scan
	 * @param key The key of the {@link InterceptParam} to look for
	 * @param map The cache map
	 * @return The position of the parameter or null if none found
	 */
	private @CheckForNull static Integer findInterceptParam(final Method method, final String key, final Map<String, Integer> map) {
		synchronized (map) {
			final Integer pos = map.get(key);
			if (pos != null) {
				return pos;
			}

			for (int i = 0; i < method.getParameterTypes().length; ++i) {
				final InterceptParam interceptParam = Annotations.getOneRecursive(InterceptParam.class, method.getParameterAnnotations()[i]);
				if (interceptParam != null && key.equals(interceptParam.value())) {
					final Integer ret = Integer.valueOf(i);
					map.put(key, ret);
					return ret;
				}
			}
			return null;
		}
	}

	/**
	 * Util to set the value of an @{@link InterceptParam} annotated argument from an interceptor.
	 *
	 * @param invocation The invocation on which to set the argument
	 * @param key The value string given to the annotation of the parameter
	 * @param parameter The value of the parameter to set
	 */
	public void set(final MethodInvocation invocation, final String key, final Object parameter) {

		final Method method = invocation.getMethod();

		Map<String, Integer> map = this.posCache.get(method);
		if (map == null) {
			synchronized (this.posCache) {
				map = this.posCache.get(method);
				if (map == null) {
					map = new HashMap<>();
					this.posCache.put(method, map);
				}
			}
		}

		Integer pos = map.get(key);
		if (pos == null) {
			pos = findInterceptParam(method, key, map);
		}

		if (pos != null) {
			invocation.getArguments()[pos.intValue()] = parameter;
		}
	}

}
