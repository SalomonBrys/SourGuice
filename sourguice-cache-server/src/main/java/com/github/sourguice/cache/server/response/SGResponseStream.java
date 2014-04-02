package com.github.sourguice.cache.server.response;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.CheckForNull;
import javax.servlet.ServletOutputStream;

import com.github.sourguice.cache.server.throwable.CacheTooLateException;

/**
 * Stream that will wrap the response's stream and intercept any write so that the cache (if any) can handle it
 */
public class SGResponseStream extends ServletOutputStream {
	/**
	 * The base stream
	 */
	private final OutputStream base;

	/**
	 * The stream of the cache, if any
	 */
	private @CheckForNull OutputStream cacheStream = null;

	/**
	 * Whether or not something has already been written
	 */
	private boolean hasWritten = false;

	/**
	 * Constructor
	 *
	 * @param base Base stream
	 */
	public SGResponseStream(final OutputStream base) {
		super();
		this.base = base;
	}

	@Override
	public void write(final int byt) throws IOException {
		this.base.write(byt);
		this.hasWritten = true;
		if (this.cacheStream != null) {
			this.cacheStream.write(byt);
		}
	}

	/**
	 * @param cacheStream The stream of the cache
	 */
	public void setCacheStream(final OutputStream cacheStream) {
		if (this.hasWritten) {
			throw new CacheTooLateException();
		}
		this.cacheStream = cacheStream;
	}

	@Override
	public void flush() throws IOException {
		this.base.flush();
	}

	@Override
	public void close() throws IOException {
		this.base.close();
	}
}