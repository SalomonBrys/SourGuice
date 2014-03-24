package com.github.sourguice.cache.impl;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.cache.Cache;
import com.github.sourguice.cache.CacheService;
import com.github.sourguice.response.SGResponse;
import com.google.inject.Injector;

/**
 * Forwards the cache registration to the current {@link SGResponse}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Singleton
public class CacheServiceImpl implements CacheService {

	/**
	 * Cache provider
	 */
	private final Provider<Cache> cacheProvider;

	/**
	 * Request provider
	 */
	private final Provider<HttpServletRequest> requestProvider;

	/**
	 * Response provider
	 */
	private final Provider<HttpServletResponse> responseProvider;

	/**
	 * Constructor
	 *
	 * @param injector Guice Injector
	 */
	@Inject
	public CacheServiceImpl(final Injector injector) {
		super();
		this.requestProvider = injector.getProvider(HttpServletRequest.class);
		this.responseProvider = injector.getProvider(HttpServletResponse.class);

		// We use this "proxy" provider because Cache may have not been bound in Guice.
		// It's OK as long as cacheRequest() (without argument) is not called.
		// This is why we can't ask directly for the real provider but wait for the first request.
		this.cacheProvider = new Provider<Cache>() {
			@CheckForNull Provider<Cache> realProvider = null;
			@Override public Cache get() {
				if (this.realProvider == null) {
					this.realProvider = injector.getProvider(Cache.class);
				}
				return this.realProvider.get();
			}
		};
	}

	@Override
	public <T extends Cache> T cacheRequest(final T cache) throws IOException {
		cache.begin(this.requestProvider.get());
		SGResponse.getSourGuice(this.responseProvider.get()).setCache(cache);
		return cache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Cache> T cacheRequest() throws IOException {
		return (T) cacheRequest(this.cacheProvider.get());
	}

}
