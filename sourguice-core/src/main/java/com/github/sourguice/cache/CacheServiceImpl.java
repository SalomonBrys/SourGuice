package com.github.sourguice.cache;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.request.Cache;
import com.github.sourguice.request.CacheService;
import com.github.sourguice.response.SourGuiceResponse;

/**
 * Forwards the cache registration to the current {@link SourGuiceResponse}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Singleton
public class CacheServiceImpl implements CacheService {

	/**
	 * Provides the cache. Guice will fail if no Cache is registered
	 */
	private final Provider<Cache> cacheProvider;

	/**
	 * Provides the response
	 */
	private final Provider<HttpServletResponse> responseProvider;

	/**
	 * Constructor
	 *
	 * @param cacheProvider Provides the cache
	 * @param responseProvider Provides the response
	 */
	@Inject
	public CacheServiceImpl(final Provider<Cache> cacheProvider, final Provider<HttpServletResponse> responseProvider) {
		super();
		this.cacheProvider = cacheProvider;
		this.responseProvider = responseProvider;
	}

	@Override
	public Cache cacheRequest(final Cache cache) {
		SourGuiceResponse.getSourGuice(this.responseProvider.get()).setCache(cache);
		return cache;
	}

	@Override
	public Cache cacheRequest() {
		return cacheRequest(this.cacheProvider.get());
	}

}
