package com.github.sourguice;

import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * In some application servers, the filter / servlet API will always be called,
 * even if the asked URL points to an existing resource in the war.
 * This (optional) filter will prevent Guice-Servlet from running and force the
 * default system servlet for existing files if the URL points to an existing file.
 *
 * This servlet has two parameters (that can be passed from web.xml):
 *  - index-root: if true, allows root directory to be listed through default servlet (default: false)
 *  - index-dir: if true, allows any directory other than root to be listed through default servlet (default: false)
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class StaticIgnoreSourGuiceFilter extends SourGuiceFilter {

	/**
	 * Whether or not to allow any directory other than root to be listed
	 */
	private boolean indexDir = false;

	/**
	 * Whether or not to allow root directory to be listed
	 */
	private boolean indexRoot = false;

	/**
	 * The servlet config given at init
	 */
	private @CheckForNull ServletContext context;

	@Override
	@OverridingMethodsMustInvokeSuper
	public void doFilter(final ServletRequest _req, final ServletResponse _res, final FilterChain chain) throws IOException, ServletException {
		assert _req != null;
		assert chain != null;
		final HttpServletRequest req = (HttpServletRequest)_req;
		assert this.context != null;
		final File file = new File(this.context.getRealPath(req.getRequestURI()));
		if (file.exists() && (this.indexRoot || !req.getRequestURI().equals("/")) && (this.indexDir || !file.isDirectory())) {
			chain.doFilter(_req, _res);
			return ;
		}
		super.doFilter(_req, _res, chain);
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void init(final FilterConfig config) throws ServletException {
		super.init(config);

		assert config != null;

		this.context = config.getServletContext();

		final String dir = config.getInitParameter("index-dir");
		if (dir != null && dir.equals("true")) {
			this.indexDir = true;
		}

		final String root = config.getInitParameter("index-root");
		if (root != null && root.equals("true")) {
			this.indexRoot = true;
		}
	}
}
