package com.github.sourguice.cache.server.def;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
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

import com.github.sourguice.cache.server.Cache;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;

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
	private @CheckForNull CharArrayWriter writer = null;

	/**
	 * The writer in which the response will be written
	 */
	private @CheckForNull ByteArrayOutputStream stream = null;

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
		 * Entry char data
		 */
		protected @CheckForNull char[] charData = null;

		/**
		 * Entry byte data
		 */
		protected @CheckForNull byte[] byteData = null;

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
	 * Initialize the cache and creates the module to install in Guice
	 *
	 * @param maxSize Maximum number of request that can be cached
	 * @param registerFilter Whether or not to register the {@link InMemoryCacheFilter} in Guice. If true, the filter will be register for /*
	 * @return The module to install
	 */
	@SuppressWarnings("serial")
	static public ServletModule initialize(final int maxSize, final boolean registerFilter) {
		lruCache = Collections.synchronizedMap(new LinkedHashMap<String, Set<CacheEntry>>(maxSize + 1, .75f, true) {
			@Override
			protected boolean removeEldestEntry(final Map.Entry<String, Set<CacheEntry>> eldest) {
				return size() > maxSize;
			}
		});

		return new ServletModule() {
			@Override
			protected void configureServlets() {
				super.configureServlets();

				bind(Cache.class).to(InMemoryCache.class);
				if (registerFilter) {
					filter("/*").through(InMemoryCacheFilter.class);
				}
				final InMemoryCacheInterceptor interceptor = new InMemoryCacheInterceptor();
				requestInjection(interceptor);
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(CacheInMemory.class), interceptor);
			}
		};
	}

	/**
	 * Initialize the cache and creates the module to install in Guice with the {@link InMemoryCacheFilter} registered for all requests
	 *
	 * @param maxSize Maximum number of request that can be cached
	 * @return The module to install
	 */
	static public ServletModule initialize(final int maxSize) {
		return initialize(maxSize, false);
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
	public void begin(final HttpServletRequest req) {
		this.request = req;
	}

	@Override
	public Writer getWriter() {
		if (this.writer == null) {
			this.writer = new CharArrayWriter();
		}
		return this.writer;
	}

	@Override
	public @CheckForNull OutputStream getStream() {
		if (this.stream == null) {
			this.stream = new ByteArrayOutputStream();
		}
		return this.stream;
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
	public void save(final HttpServletResponse res) throws IOException {
		if (lruCache == null) {
			throw new UnsupportedOperationException("InMemoryCache is not initialized");
		}
		if (this.entry.expires.equals(EPOCH)) {
			throw new UnsupportedOperationException("Expiration date is not set");
		}
		if (this.request == null) {
			throw new UnsupportedOperationException("Cache has not been registered for this request");
		}
		final String requestURI = this.request.getRequestURI();
		Set<CacheEntry> set = lruCache.get(requestURI);
		if (set == null) {
			set = new HashSet<>();
			lruCache.put(requestURI, set);
		}
		if (this.writer != null) {
			this.entry.charData = this.writer.toCharArray();
			this.writer.close();
		}
		else if (this.stream != null) {
			this.entry.byteData = this.stream.toByteArray();
			this.stream.close();
		}
		set.add(this.entry);
	}

	/**
	 * Removes the cache for a specific URI
	 *
	 * @param uri The URI to remove from the cache
	 */
	public static void remove(final String uri) {
		if (lruCache != null) {
			lruCache.remove(uri);
		}
	}
}
