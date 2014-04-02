package com.github.sourguice.cache.server.response;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.CheckForNull;

import com.github.sourguice.cache.server.throwable.CacheTooLateException;

/**
 * Writer that will wrap the response's writer and intercept any write so that the cache (if any) can handle it
 */
public class SGResponseWriter extends Writer {
	/**
	 * The base writer
	 */
	private final Writer base;

	/**
	 * The writer of the cache, if any
	 */
	private @CheckForNull Writer cacheWriter = null;

	/**
	 * Whether or not something has already been written
	 */
	private boolean hasWritten = false;

	/**
	 * Constructor
	 *
	 * @param base Base writer
	 */
	public SGResponseWriter(final Writer base) {
		super();
		this.base = base;
	}

	@Override
	public void write(final char cbuf[], final int off, final int len) throws IOException {
		this.base.write(cbuf, off, len);
		this.hasWritten = true;
		if (this.cacheWriter != null) {
			this.cacheWriter.write(cbuf, off, len);
		}
	}

	/**
	 * @param cacheWriter The writer of the cache
	 */
	public void setCacheWriter(final Writer cacheWriter) {
		if (this.hasWritten) {
			throw new CacheTooLateException();
		}
		this.cacheWriter = cacheWriter;
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