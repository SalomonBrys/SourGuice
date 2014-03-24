package com.github.sourguice.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that will handle a request and transmit it to the relevant controller's invocation
 * As each controller is registered to a URL pattern, for each URL pattern there is a  ControllersServlet.
 * One ControllersServlet may have multiple controllers if multiple controllers are registered on the same URL pattern.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public final class ControllersServlet extends HttpServlet {
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = -7900861299267188869L;

	/**
	 * The SourGuice Server, where all the magic happens \o/
	 */
	private final ControllersServer server = new ControllersServer();

	/**
	 * Constructor
	 *
	 * @param membersInjector Responsible for injecting newly created {@link ControllersServer}
	 */
	public ControllersServlet(final MembersInjectionRequest membersInjector) {
		super();
		membersInjector.requestMembersInjection(this.server);
	}

	/**
	 * Adds a controller to this servlet's path
	 * This means that the given controller is registered on the same path as the servlet
	 *
	 * @param handler The controller to add to this servlet handlers
	 */
	public <T> void addController(final ControllerHandler<T> handler) {
		this.server.addController(handler);
	}

	@Override protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		this.server.serve(req, res);
	}

	@Override protected void doPost(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		this.server.serve(req, res);
	}

	@Override protected void doPut(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		this.server.serve(req, res);
	}

	@Override protected void doDelete(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		this.server.serve(req, res);
	}

	@Override protected void doHead(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		this.server.serve(req, res);
	}

	@Override protected void doOptions(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		this.server.serve(req, res);
	}

	@Override protected void doTrace(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		this.server.serve(req, res);
	}
}
