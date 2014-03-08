package com.github.sourguice.throwable.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * Exception used to send an HTTP redirect from a controller's method
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class MvcResponseRedirect extends MvcResponseException {
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = -5613860675750051649L;

	/**
	 * The URL to redirect to
	 */
	private final String toUrl;

	/**
	 * @param toUrl The URL to redirect to
	 */
	public MvcResponseRedirect(final String toUrl) {
		super();
		this.toUrl = toUrl;
	}

	/**
	 * Sends the configured redirect to the HTTP response
	 */
	@Override
	public void execute(final HttpServletResponse res) throws IOException {
		res.sendRedirect(this.toUrl);
	}
}
