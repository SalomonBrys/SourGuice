package com.github.sourguice.response;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.CheckForNull;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.github.sourguice.cache.Cache;
import com.github.sourguice.throwable.NoSourGuiceResponseException;

/**
 * Response wrapper that handles SourGuice specificities
 */
public class SourGuiceResponse extends HttpServletResponseWrapper {

	/**
	 * Cache for this request
	 */
	private @CheckForNull Cache cache = null;

	/**
	 * The writer that intercepts writes to duplicate it to cache if any
	 */
	private @CheckForNull SourGuiceResponseWriter writer = null;

	/**
	 * The print writer for the response
	 */
	private @CheckForNull PrintWriter printWriter = null;

	/**
	 * The stream that intercepts writes to duplicate it to cache if any
	 */
	private @CheckForNull SourGuiceResponseStream stream = null;

	/**
	 * Find the {@link SourGuiceResponse} in the current HttpServletResponse
	 *
	 * @param res The current HttpServletResponse
	 * @return The found SourGuiceResponse
	 * @throws NoSourGuiceResponseException if no SourGuiceResponse could be found
	 */
	public static SourGuiceResponse getSourGuice(final HttpServletResponse res) throws NoSourGuiceResponseException {
		if (res instanceof SourGuiceResponse) {
			return (SourGuiceResponse) res;
		}
		if (res instanceof HttpServletResponseWrapper) {
			final ServletResponse base = ((HttpServletResponseWrapper) res).getResponse();
			if (base instanceof HttpServletResponse) {
				return getSourGuice((HttpServletResponse) base);
			}
		}

		throw new NoSourGuiceResponseException();
	}

	/**
	 * @see HttpServletResponseWrapper#HttpServletResponseWrapper(HttpServletResponse)
	 */
	public SourGuiceResponse(final HttpServletResponse response) {
		super(response);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (this.writer == null) {
			this.writer = new SourGuiceResponseWriter(super.getResponse().getWriter());
			if (this.cache != null) {
				this.writer.setCacheWriter(this.cache.getWriter());
			}
		}
		if (this.printWriter == null) {
			this.printWriter = new PrintWriter(this.writer);
		}
		return this.printWriter;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (this.stream == null) {
			this.stream = new SourGuiceResponseStream(super.getResponse().getOutputStream());
			if (this.cache != null) {
				this.stream.setCacheStream(this.cache.getStream());
			}
		}
		return this.stream;
	}

	/**
	 * Set a cache to be used on this request
	 * @param cache The cache responsible for saving this request
	 * @throws IOException If an input or output exception occurred
	 */
	public void setCache(final Cache cache) throws IOException {
		this.cache = cache;
		if (this.writer != null) {
			this.writer.setCacheWriter(cache.getWriter());
		}
		if (this.stream != null) {
			this.stream.setCacheStream(cache.getStream());
		}
	}

	/**
	 * @return The cache responsible for saving this request, if any
	 */
	public @CheckForNull Cache getCache() {
		return this.cache;
	}
}