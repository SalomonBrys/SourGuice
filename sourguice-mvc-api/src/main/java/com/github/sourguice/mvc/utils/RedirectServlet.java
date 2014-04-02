package com.github.sourguice.mvc.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple servlet that only redirects to pre-defined value
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RedirectServlet extends HttpServlet {

	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 7332681939959706932L;

	/**
	 * The destination URL to redirect to
	 */
	private final String dest;

	/**
	 * @param dest The destination URL to redirect to
	 */
	public RedirectServlet(final String dest) {
		super();
		this.dest = dest;
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		res.sendRedirect(this.dest);
	}

}
