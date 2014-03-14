package com.github.sourguice.controller;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.annotation.controller.HttpError;
import com.github.sourguice.annotation.request.Redirects;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.call.impl.MvcCallerImpl;
import com.github.sourguice.request.wrapper.NoJsessionidHttpRequest;
import com.github.sourguice.throwable.invocation.HandledException;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.value.ValueConstants;
import com.github.sourguice.view.NoViewRendererException;
import com.github.sourguice.view.ViewRenderingException;
import com.google.inject.Injector;

/**
 * Servlet that will handle a request and transmit it to the relevant controller's invocation
 * As each controller is registered to a URL pattern, for each URL pattern there is a  ControllersServlet.
 * One ControllersServlet may have multiple controllers if multiple controllers are registered on the same URL pattern.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public final class ControllersServer {

	/**
	 * List of {@link ControllerHandler}s registered for the path that this servlet handles
	 */
	private final List<ControllerHandler<?>> handlers = new LinkedList<>();

	/**
	 * The guice injector that will be used to get different SourGuice implementations
	 */
	@CheckForNull private Injector injector;

	/**
	 * Method for injecting the injector
	 *
	 * @param injector The guice injector that will be used to get different SourGuice implementations
	 */
	@Inject
	public void setInjector(final Injector injector) {
		this.injector = injector;
	}

	/**
	 * Adds a controller to this servlet's path
	 * This means that the given controller is registered on the same path as the servlet
	 *
	 * @param handler The controller to add to this servlet handlers
	 */
	public <T> void addController(final ControllerHandler<T> handler) {
		this.handlers.add(handler);
	}

	/**
	 * Check if the invocation being processed has a {@link HttpError} annotation and handles the request accordingly
	 *
	 * @param infos The infos of the invocation being processed
	 * @param ret Whatever the invocation has returned
	 * @param res The current HTTP response
	 * @return Whether there was an {@link HttpError} and it was handled, or not.
	 * @throws IOException IO failure while manipulating the response
	 */
	private static boolean checkHttpError(final ControllerInvocationInfos infos, final Object ret, final HttpServletResponse res) throws IOException {
		final HttpError sendsError = infos.invocation.getHttpError();
		if (sendsError == null) {
			return false;
		}

		int code = sendsError.value();
		String message = sendsError.message();
		if (ret != null) {
			if (ret instanceof Integer) {
				code = ((Integer)ret).intValue();
			}
			else {
				message = ret.toString();
			}
		}
		if (message.isEmpty()) {
			res.sendError(code);
		}
		else {
			res.sendError(code, message);
		}
		return true;
	}

	/**
	 * Check if the invocation being processed has a {@link Redirects} annotation and handles the request accordingly
	 *
	 * @param infos The infos of the invocation being processed
	 * @param ret Whatever the invocation has returned
	 * @param res The current HTTP response
	 * @return Whether there was an {@link Redirects} and it was handled, or not.
	 * @throws IOException IO failure while manipulating the response
	 */
	private static boolean checkRedirects(final ControllerInvocationInfos infos, final Object ret, final HttpServletResponse res) throws IOException {
		final Redirects redirectsTo = infos.invocation.getRedirects();
		if (redirectsTo == null) {
			return false;
		}

		String toPath = redirectsTo.value();
		if (ret != null) {
			if (toPath.isEmpty()) {
				toPath = ret.toString();
			}
			else if (toPath.contains("{}")) {
				toPath = toPath.replace("{}", ret.toString());
			}
		}
		if (!toPath.isEmpty()) {
			res.sendRedirect(toPath);
			return true;
		}
		return false;
	}

	/**
	 * Check if the invocation being processed has a {@link Writes} annotation and handles the request accordingly
	 *
	 * @param infos The infos of the invocation being processed
	 * @param ret Whatever the invocation has returned
	 * @param res The current HTTP response
	 * @return Whether there was an {@link Writes} and it was handled, or not.
	 * @throws IOException IO failure while manipulating the response
	 */
	private static boolean checkWrites(final ControllerInvocationInfos infos, Object ret, final HttpServletResponse res) throws IOException {
		final Writes writes = infos.invocation.getWrites();
		if (writes == null) {
			return false;
		}

		if (ret == null) {
			throw new UnsupportedOperationException("@Writes annotated method must NOT return null");
		}
		if (ret instanceof InputStream) {
			ret = new InputStreamReader((InputStream)ret);
		}
		if (ret instanceof Readable) {
			final Readable readable = (Readable)ret;
			final CharBuffer buffer = CharBuffer.allocate(writes.bufferSize());
			while (readable.read(buffer) >= 0) {
				buffer.flip();
				res.getWriter().append(buffer);
				buffer.clear();
			}
		}
		else {
			res.getWriter().write(ret.toString());
		}
		if (ret instanceof Closeable) {
			((Closeable)ret).close();
		}
		return true;
	}

	/**
	 * Excecutes the call on the given invocation
	 *
	 * @param infos The infos of the invocation to call
	 * @param res The current HTTP response
	 * @throws HandledException If an exception was thrown and handled
	 * @throws NoViewRendererException If the invocation gave a view that no view renderer could render
	 * @throws ViewRenderingException If the invocation gave a view that fail to render
	 * @throws IOException IO failure while manipulating the response
	 * @throws NoSuchRequestParameterException If a parameter could not be found
	 * @throws InvocationTargetException Any exception thrown by the method being called
	 */
	private void makeCall(final ControllerInvocationInfos infos, final HttpServletResponse res) throws HandledException, NoViewRendererException, ViewRenderingException, IOException, InvocationTargetException, NoSuchRequestParameterException {
		assert this.injector != null;
		assert infos.urlMatch != null;

		// Invoke the invocation using the MethodCaller registered in Guice
		final Object ret = this.injector.getInstance(MvcCallerImpl.class).call(infos.invocation, infos.urlMatch, true);

		// Sets the view to the default default view
		String view = infos.defaultView;

		if (	view == null
			&&	(	checkHttpError(infos, ret, res)
				||	checkRedirects(infos, ret, res)
				||	checkWrites(infos, ret, res)
				)
			) {
				return ;
		}

		// If the method returned a view, sets the view to it
		if (ret != null) {
			if (view != null && view.contains("{}")) {
				view = view.replace("{}", ret.toString());
			}
			else {
				view = ret.toString();
			}
		}

		// If there is a view to display
		if (view != null) {
			infos.invocation.getController().renderView(view, this.injector);
		}
	}

	/**
	 * Serves a request
	 * This will ask all its controllers for the best invocation and will select the best of them all
	 * It will than invoke the invocation and render the corresponding view if necessary
	 *
	 * @param req The HTTP Request
	 * @param res The HTTP Response
	 * @throws ServletException When an exception that was not handled by the MVC system is thrown
	 * @throws IOException If an input or output exception occurs
	 */
	@SuppressWarnings({"PMD.EmptyCatchBlock", "PMD.PreserveStackTrace"})
	protected void serve(HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {

		assert req != null;
		assert res != null;
		assert this.injector != null;

		// Removes JSESSIONID from the request path if it is there
		if (req.getPathInfo() != null) {
			req = new NoJsessionidHttpRequest(req);
		}

		// Gets the best invocation of all controller handlers
		ControllerInvocationInfos infos = null;
		for (final ControllerHandler<?> handler : this.handlers) {
			infos = ControllerInvocationInfos.getBest(infos, handler.getBestInvocation(req));
		}

		// If no invocation were found
		if (infos == null) {
			// Sends a 404 otherwise
			res.sendError(404);
			return ;
		}

		assert infos.urlMatch != null;
		// Stores the MatchResult into the RequestScoped Container so it can be later retrieved with guice injection
		req.setAttribute(ValueConstants.MATCH_RESULT_REQUEST_ATTRIBUTE, infos.urlMatch);

		try {
			makeCall(infos, res);
		}
		catch (NoSuchRequestParameterException e) {
			// If a parameter is missing from the request, sends a 400 error
			res.sendError(400, e.getMessage());
		}
		catch (HandledException e) {
			// Exception was handled by the MvcExceptionService, it is safe (and expected) to ignore
		}
		catch (InvocationTargetException e) {
			throw new ServletException(e.getCause());
		}
		catch (NoViewRendererException | ViewRenderingException e) {
			throw new ServletException(e);
		}
	}
}
