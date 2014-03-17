package com.github.sourguice;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.annotation.CheckForNull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.github.sourguice.throwable.SourGuiceRuntimeException;
import com.github.sourguice.throwable.invocation.CacheTooLateException;
import com.google.inject.servlet.GuiceFilter;

/**
 * Guice Filter that handles SourGuice specificities
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class SourGuiceFilter extends GuiceFilter {

	/**
	 * Writer that will wrap the response's writer and intercept any write so that the cache (if any) can handle it
	 */
	public static class SourGuiceResponseWriter extends PrintWriter {
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

	/**
	 * Response wrapper that handles SourGuice specificities
	 */
	public static class SourGuiceResponse extends HttpServletResponseWrapper {

		/**
		 * The writer that intercept writes to duplicate it to cache if any
		 */
		private @CheckForNull SourGuiceResponseWriter writer = null;

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
			}
			return this.writer;
		}
	}

	@Override
	public void doFilter(final ServletRequest req, ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		res = new SourGuiceResponse((HttpServletResponse) res);
		super.doFilter(req, res, chain);
	}

}
