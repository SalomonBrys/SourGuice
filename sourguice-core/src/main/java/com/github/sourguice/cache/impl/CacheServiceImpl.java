package com.github.sourguice.cache.impl;

import java.io.IOException;

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
	 * Guice Injector.
	 *
	 * We use the injector rather than {@link Provider} because {@link Cache} may have not been bound
	 * and it's OK as long as {@link #cacheRequest()} (without argument) is not called
	 */
	private final Injector injector;

	/**
	 * Constructor
	 *
	 * @param injector Guice Injector
	 */
	@Inject
	public CacheServiceImpl(final Injector injector) {
		super();
		this.injector = injector;
	}

	@Override
	public <T extends Cache> T cacheRequest(final T cache) throws IOException {
		final HttpServletRequest request = this.injector.getInstance(HttpServletRequest.class);
		final SGResponse response = SGResponse.getSourGuice(this.injector.getInstance(HttpServletResponse.class));
		cache.begin(request);
		response.setCache(cache);
		return cache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Cache> T cacheRequest() throws IOException {
		return (T) cacheRequest(this.injector.getInstance(Cache.class));
	}

}
