package com.github.sourguice.request;

import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

/**
 * A cache must implement this interface.
 *
 * A cache system is in 2 parts :
 * <ul>
 *   <li> The cache backend that will receive response data and save it (this interface) </li>
 *   <li> The filter that will write the cache request and prevent SourGuice from running </li>
 * </ul>
 */
public interface Cache {

	/**
	 * @return The writer that will receive the response data
	 */
	public Writer getWriter();

	/**
	 * Called by the {@link CacheService} when the response has fully been written
	 * @param res The response to cache
	 */
	public void save(HttpServletResponse res);

}
