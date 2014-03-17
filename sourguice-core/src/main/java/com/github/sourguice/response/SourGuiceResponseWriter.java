package com.github.sourguice.response;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.annotation.CheckForNull;

import com.github.sourguice.throwable.SourGuiceRuntimeException;
import com.github.sourguice.throwable.invocation.CacheTooLateException;

/**
 * Writer that will wrap the response's writer and intercept any write so that the cache (if any) can handle it
 */
public class SourGuiceResponseWriter extends PrintWriter {
	/**
	 * The writer of the cache, if any
	 */
	private @CheckForNull Writer cacheWriter = null;

	/**
	 * Whether or not something has already been written
	 */
	private boolean hasWritten = false;

	/**
	 * @see PrintWriter#PrintWriter(Writer)
	 */
	public SourGuiceResponseWriter(final Writer base) {
		super(base);
	}

	@Override
	public void write(final char cbuf[], final int off, final int len) {
		super.write(cbuf, off, len);
		this.hasWritten = true;
		if (this.cacheWriter != null) {
			try {
				this.cacheWriter.write(cbuf, off, len);
			}
			catch (IOException e) {
				throw new SourGuiceRuntimeException(e);
			}
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
}