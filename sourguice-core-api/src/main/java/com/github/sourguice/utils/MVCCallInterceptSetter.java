package com.github.sourguice.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
public class MVCCallInterceptSetter {

	private Map<Method, Map<String, Integer>> posCache = new HashMap<>();

	/**
	 * Util to set the value of an @{@link InterceptParam} annotated argument from an interceptor.
	 *
	 * @param invocation The invocation on which to set the argument
	 * @param key The value string given to the annotation of the parameter
	 * @param parameter The value of the parameter to set
	 */
	public void set(MethodInvocation invocation, String key, Object parameter) {

		Method method = invocation.getMethod();

		Map<String, Integer> map = posCache.get(method);
		if (map == null)
			synchronized (posCache) {
				map = posCache.get(method);
				if (map == null) {
					map = new HashMap<>();
					posCache.put(method, map);
				}
			}

		Integer pos = map.get(key);
		if (pos == null)
			synchronized (map) {
				pos = map.get(key);
				if (pos == null)
					for (int i = 0; i < method.getParameterTypes().length; ++i) {
						InterceptParam interceptParam = Annotations.GetOneRecursive(InterceptParam.class, method.getParameterAnnotations()[i]);
						if (interceptParam != null && key.equals(interceptParam.value())) {
							pos = Integer.valueOf(i);
							map.put(key, pos);
							break ;
						}
					}
			}

		if (pos != null)
			invocation.getArguments()[pos.intValue()] = parameter;
	}

}
