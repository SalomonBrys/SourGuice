package com.github.sourguice;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.cache.Cache;
import com.github.sourguice.response.SGResponse;
import com.google.inject.servlet.GuiceFilter;

/**
 * Guice Filter that handles SourGuice specificities
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class SourGuiceFilter extends GuiceFilter {

	@Override
	public void doFilter(final ServletRequest _req, final ServletResponse _res, final FilterChain chain) throws IOException, ServletException {
		final SGResponse res = new SGResponse((HttpServletResponse) _res);
		super.doFilter(_req, res, chain);

		final Cache cache = res.getCache();
		if (cache != null) {
			cache.save(res);
		}
	}

}
