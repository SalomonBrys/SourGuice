package com.github.sourguice.cache.server;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.cache.server.response.SGResponse;
import com.google.inject.Injector;

/**
 * The cache service allows you to cache a request.
 *
 * For your cache to work, you must registered you cache filter in your Web.xml
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Singleton
public class CacheService {

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
	public CacheService(final Injector injector) {
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

	/**
	 * Tells SouGuice to cache this request with a specific cache implementation
	 *
	 * @param cache The cache implementation to use
	 * @return The registered cache, to chain access
	 * @throws IOException Any IO exception that occured on cache request creation
	 */
	public <T extends Cache> T cacheRequest(final T cache) throws IOException {
		cache.begin(this.requestProvider.get());
		SGResponse.getSourGuice(this.responseProvider.get()).setCache(cache);
		return cache;
	}

	/**
	 * Tells SouGuice to cache this request with the default cache implementation
	 *
	 * You must have registered in guice a {@link Cache} implementation
	 *
	 * @return The registered cache, to chain access
	 * @throws IOException Any IO exception that occured on cache request creation
	 * @throws ClassCastException If the default cache is not from the given type
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cache> T cacheRequest() throws IOException {
		return (T) cacheRequest(this.cacheProvider.get());
	}

}
