package com.github.sourguice.request.wrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This wrapper removes the jsessionid from the request Path Info as Guice does not removes it
 *
 * The jsessionid should be passed by cookie and not by url as much as possible but there are some cases in which it is not possible
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public final class NoJsessionidHttpRequest extends HttpServletRequestWrapper {

	/**
	 * Pattern to capture a jsessionid
	 */
	static private final Pattern JSESSID_PTRN = Pattern.compile(";jsessionid=[a-zA-Z0-9\\-\\._]+");

	/**
	 * The pathInfo with jessionid removed
	 */
	private final String pathInfo;

	/**
	 * @param request The current request to wrap
	 */
	public NoJsessionidHttpRequest(final HttpServletRequest request) {
		super(request);

		String path = request.getPathInfo();

		final Matcher matcher = JSESSID_PTRN.matcher(path);
		if (matcher.find()) {
			path = path.substring(0, matcher.start()) + path.substring(matcher.end());
		}
		this.pathInfo = path;
	}

	/**
	 * The pathInfo without the jessionid if it was previously present
	 */
	@Override
	public String getPathInfo() {
		return this.pathInfo;
	}

}
