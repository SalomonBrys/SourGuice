package com.github.sourguice.cache.def;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.github.sourguice.cache.CacheService;

/**
 * Interceptor that makes SourGuice cache the {@link CacheInMemory} anotated method with the {@link InMemoryCache}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class InMemoryCacheInterceptor implements MethodInterceptor {

	/**
	 * Method annotation that indicates that this method's response should be cached
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Documented
	public @interface CacheInMemory {
		/**
		 * @return Duration of the cache
		 */
		int seconds();

		/**
		 * @return List of headers that are part of this cache's entry
		 */
		String[] headers() default {};
	}

	/**
	 * Guice provider for the CacheService (request scoped)
	 */
	private @CheckForNull @Inject Provider<CacheService> serviceProvider;

	/**
	 * Guice provider for the InMemoryCache (request scoped)
	 */
	private @CheckForNull @Inject Provider<InMemoryCache> cacheProvider;


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		if (this.serviceProvider == null || this.cacheProvider == null) {
			throw new UnsupportedOperationException("InMemoryCacheInterceptor has not been injected");
		}

		final Object ret = invocation.proceed();

		final CacheInMemory info = invocation.getMethod().getAnnotation(CacheInMemory.class);
		if (info != null) {
			final InMemoryCache cache = this.serviceProvider.get().cacheRequest(this.cacheProvider.get());
			cache.setExpiration(info.seconds());
			for (final String header : info.headers()) {
				cache.putHeader(header);
			}
		}
		return ret;
	}



}
