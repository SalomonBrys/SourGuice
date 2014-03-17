package com.github.sourguice.cache.def;

import java.io.CharArrayWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.cache.Cache;
import com.google.inject.servlet.RequestScoped;

/**
 * This is a VERY simple cache that caches response data in memory.
 *
 * This cache is TOO SIMPLE too be used in production.
 * You should use caches that are based on real caching systems like Guava Cache, EHCache, Memcache, Cache2K, JCS, etc.
 *
 * This cache is based on a synchronized LRU {@link LinkedHashMap}. You must first call {@link #initialize(int)}.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@RequestScoped
public class InMemoryCache implements Cache {

	/**
	 * Date used to define that the expiration date has not been set
	 */
	protected static final Date EPOCH = new Date(0);

	/**
	 * The writer in which the response will be written
	 */
	private final CharArrayWriter writer = new CharArrayWriter();

	/**
	 * Current cache entry
	 */
	private final CacheEntry entry = new CacheEntry();

	/**
	 * The current request
	 */
	private @CheckForNull HttpServletRequest request = null;

	/**
	 * The LRU cache
	 */
	protected static @CheckForNull Map<String, Set<CacheEntry>> lruCache;

	/**
	 * A cache entry
	 */
	protected static class CacheEntry {
		/**
		 * Entry expiration date
		 */
		protected Date expires = EPOCH;

		/**
		 * Headers that are part of this cache entry
		 */
		protected final Map<String, String> headers = new HashMap<>();

		/**
		 * Entry data
		 */
		protected String data = "";

		@Override
		public int hashCode() {
			return this.headers.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final CacheEntry other = (CacheEntry) obj;

			if (!this.headers.equals(other.headers)) {
				return false;
			}

			return true;
		}


	}

	/**
	 * Initialize the cache
	 *
	 * @param maxSize Maximum number of request that can be cached
	 */
	@SuppressWarnings("serial")
	static public void initialize(final int maxSize) {
		lruCache = Collections.synchronizedMap(new LinkedHashMap<String, Set<CacheEntry>>(maxSize + 1, .75f, true) {
			@Override
			protected boolean removeEldestEntry(final Map.Entry<String, Set<CacheEntry>> eldest) {
				return size() > maxSize;
			}
		});
	}

	/**
	 * Constructor
	 *
	 * @throws UnsupportedOperationException If called while the cache has not been initialized
	 */
	@Inject
	public InMemoryCache() throws UnsupportedOperationException {
		if (lruCache == null) {
			throw new UnsupportedOperationException("InMemoryCache is not initialized");
		}
	}

	@Override
	public Writer begin(final HttpServletRequest req) {
		this.request = req;
		return this.writer;
	}

	/**
	 * @param expiration This request's cache expiration date
	 */
	public void setExpiration(final Date expiration) {
		this.entry.expires = expiration;
	}

	/**
	 * @param seconds This request's cache expiration from now in seconds
	 */
	public void setExpiration(final int seconds) {
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, seconds);
		setExpiration(calendar.getTime());
	}

	/**
	 * Add a header into the cache definition of thgis request.
	 *
	 * That means that this cache entry should hit on requests that have the same header
	 *
	 * @param header The header to add to definition
	 */
	public void putHeader(final String header) {
		if (this.request == null) {
			throw new UnsupportedOperationException("Cache has not been registered for this request");
		}
		if (this.request.getMethod().equalsIgnoreCase("POST")) {
			throw new UnsupportedOperationException("POST requests cannot be cached");
		}
		this.entry.headers.put(header, this.request.getHeader(header));
	}

	@Override
	public void save(final HttpServletResponse res) {
		if (lruCache == null) {
			throw new UnsupportedOperationException("InMemoryCache is not initialized");
		}
		if (this.entry.expires.equals(EPOCH)) {
			throw new UnsupportedOperationException("Expiration date is not set");
		}
		if (this.request == null) {
			throw new UnsupportedOperationException("Cache has not been registered for this request");
		}
		final String pathInfo = this.request.getPathInfo();
		Set<CacheEntry> set = lruCache.get(pathInfo);
		if (set == null) {
			set = new HashSet<>();
			lruCache.put(pathInfo, set);
		}
		set.add(this.entry);
		this.writer.close();
	}
}
