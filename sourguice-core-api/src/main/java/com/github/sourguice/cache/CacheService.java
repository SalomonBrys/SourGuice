package com.github.sourguice.cache;

import java.io.IOException;

/**
 * The cache service allows you to cache a request.
 *
 * For your cache to work, you must registered you cache filter in your Web.xml
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public interface CacheService {

	/**
	 * Tells SouGuice to cache this request with a specific cache implementation
	 *
	 * @param cache The cache implementation to use
	 * @return The registered cache, to chain access
	 * @throws IOException Any IO exception that occured on cache request creation
	 */
	public <T extends Cache> T cacheRequest(T cache) throws IOException;

	/**
	 * Tells SouGuice to cache this request with the default cache implementation
	 *
	 * You must have registered in guice a {@link Cache} implementation
	 *
	 * @return The registered cache, to chain access
	 * @throws IOException Any IO exception that occured on cache request creation
	 * @throws ClassCastException If the default cache is not from the given type
	 */
	public <T extends Cache> T cacheRequest() throws IOException, ClassCastException;

}
