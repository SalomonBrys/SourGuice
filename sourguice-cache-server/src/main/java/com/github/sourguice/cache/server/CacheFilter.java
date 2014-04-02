package com.github.sourguice.cache.server;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.cache.server.response.SGResponse;
import com.google.inject.servlet.GuiceFilter;

/**
 * Cache Filter that handles SourGuice cache specificities.
 * Any web app that uses the {@link CacheService} must use this filter.
 * This filter must be registered BEFORE the {@link GuiceFilter}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Singleton
public class CacheFilter implements Filter {

	@Override
	public void doFilter(final ServletRequest _req, final ServletResponse _res, final FilterChain chain) throws IOException, ServletException {
		final SGResponse res = new SGResponse((HttpServletResponse) _res);

		chain.doFilter(_req, res);

		final Cache cache = res.getCache();
		if (cache != null) {
			cache.save(res);
		}
	}

	@Override public void init(FilterConfig filterConfig) throws ServletException {/* Nothing to do */}
	@Override public void destroy() {/* Nothing to do */}
}
