package com.github.sourguice.cache;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
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
	 * Called by the {@link CacheService} when the cache should be initialize for a new request
	 *
	 * @param req The request to cache
	 * @return The writer that will receive the response data
	 * @throws IOException If an IO exception occured while creating the writer
	 */
	public Writer begin(HttpServletRequest req) throws IOException;

	/**
	 * Called by the {@link CacheService} when the response has fully been written
	 *
	 * @param res The response to cache
	 * @throws IOException If an IO exception occured while saving the cache
	 */
	public void save(HttpServletResponse res) throws IOException;

}
