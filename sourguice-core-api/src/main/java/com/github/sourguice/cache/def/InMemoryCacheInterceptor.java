package com.github.sourguice.cache.def;

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
