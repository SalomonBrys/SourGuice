package com.github.sourguice.cache.impl;

import java.io.IOException;
import java.io.Writer;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.cache.Cache;
import com.github.sourguice.cache.CacheService;
import com.github.sourguice.response.SourGuiceResponse;
import com.google.inject.Injector;

/**
 * Forwards the cache registration to the current {@link SourGuiceResponse}
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

	@SuppressWarnings("resource")
	@Override
	public <T extends Cache> T cacheRequest(final T cache) throws IOException {
		final HttpServletRequest request = this.injector.getInstance(HttpServletRequest.class);
		final SourGuiceResponse response = SourGuiceResponse.getSourGuice(this.injector.getInstance(HttpServletResponse.class));
		final Writer writer = cache.begin(request);
		response.setCache(cache, writer);
		return cache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Cache> T cacheRequest() throws IOException {
		return (T) cacheRequest(this.injector.getInstance(Cache.class));
	}

}
