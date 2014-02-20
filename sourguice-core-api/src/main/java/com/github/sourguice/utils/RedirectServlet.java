package com.github.sourguice.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;

	private String dest;

	public RedirectServlet(String dest) {
		super();
		this.dest = dest;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.sendRedirect(dest);
	}

}
