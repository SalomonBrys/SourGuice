package com.github.sourguice.cache.server.def;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.cache.server.def.InMemoryCache.CacheEntry;

/**
 * Filter that must be use to serve request that where cached whit {@link InMemoryCache}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class InMemoryCacheFilter implements Filter {

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		// Nothing to do
	}

	@Override
	public void destroy() {
		// Nothing to do
	}

	/**
	 * Check whether all requested headers are in the request
	 *
	 * @param req The request
	 * @param cacheHeaders The headers and their values
	 * @return True if headers match, fale if not
	 */
	private static boolean checkHeaders(final HttpServletRequest req, final Map<String, String> cacheHeaders) {
		for (final Map.Entry<String, String> cacheHeader : cacheHeaders.entrySet()) {
			final String reqHeaderValue = req.getHeader(cacheHeader.getKey());
			if (reqHeaderValue == null || !reqHeaderValue.equals(cacheHeader.getValue())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void doFilter(final ServletRequest _req, final ServletResponse _res, final FilterChain chain) throws IOException, ServletException {
		if (InMemoryCache.lruCache == null) {
			chain.doFilter(_req, _res);
			return ;
		}

		final HttpServletRequest req = (HttpServletRequest) _req;
		final Set<CacheEntry> set = InMemoryCache.lruCache.get(req.getRequestURI());

		if (set == null) {
			chain.doFilter(_req, _res);
			return ;
		}

		final Date now = new Date();

		final Iterator<CacheEntry> iter = set.iterator();
		while (iter.hasNext()) {
			final CacheEntry cacheEntry = iter.next();
			if (cacheEntry.expires.after(now) && checkHeaders(req, cacheEntry.headers)) {
				if (cacheEntry.charData != null) {
					_res.getWriter().write(cacheEntry.charData);
				}
				else if (cacheEntry.byteData != null) {
					_res.getOutputStream().write(cacheEntry.byteData);
				}
				return ;
			}
		}

		chain.doFilter(_req, _res);
	}

}
