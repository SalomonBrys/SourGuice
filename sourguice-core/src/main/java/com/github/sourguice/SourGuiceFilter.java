package com.github.sourguice;

import java.io.CharArrayWriter;
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
		 * The writer of the caching system, if any
		 */
		private @CheckForNull Writer cacheWriter = null;
		private boolean hasWritten = false;

		public SourGuiceResponseWriter(Writer base) {
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
					throw new RuntimeException(e);
				}
			}
		}

		public void setCacheWriter(CharArrayWriter cacheWriter) {
			if (this.hasWritten) {
				throw new CacheTooLateException();
			}
			this.cacheWriter = cacheWriter;
		}
	}

	public static class SourGuiceResponse extends HttpServletResponseWrapper {

		SourGuiceResponseWriter writer;

		public SourGuiceResponse(HttpServletResponse response) {
			super(response);
			try {
				this.writer = new SourGuiceResponseWriter(response.getWriter());
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			return this.writer;
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		res = new SourGuiceResponse((HttpServletResponse) res);
		super.doFilter(req, res, chain);
	}

}
