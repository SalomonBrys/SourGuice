package com.github.sourguice.mvc.view.def;

import java.io.IOException;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.mvc.request.ForwardableRequestFactory;
import com.github.sourguice.mvc.view.ViewRenderer;
import com.github.sourguice.mvc.view.ViewRenderingException;
import com.google.inject.Inject;

/**
 * Default view renderer plugin that renders JSP
 * Or more exactly that forwards requests to JSPs
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class JSPViewRenderer implements ViewRenderer {

	/**
	 * The current HTTP Response
	 */
	private final HttpServletResponse res;

	/**
	 * Will be used to forward request
	 */
	private final ForwardableRequestFactory fact;

	/**
	 * @param res The current HTTP Response
	 * @param fact Factory that will be used to make request forwarding
	 */
	@Inject
	public JSPViewRenderer(final HttpServletResponse res, final ForwardableRequestFactory fact) {
		this.res = res;
		this.fact = fact;
	}

	/**
	 * Displays the given JSP
	 *
	 * Loads all model key/value into the request and redirects to the JSP
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	public void render(final String view, final @CheckForNull Map<String, Object> model) throws ViewRenderingException, IOException {

		final HttpServletRequest req = this.fact.to(view);

		if (model != null) {
			for (final String key : model.keySet()) {
				req.setAttribute(key, model.get(key));
			}
		}

		final RequestDispatcher dispatcher = req.getRequestDispatcher(view);

		// Cannot be tested
		if (dispatcher == null) {
			this.res.sendError(404, view);
			return ;
		}

		try {
			req.getRequestDispatcher(view).forward(req, this.res);
		}
		catch (ServletException e) {
			throw new ViewRenderingException(e);
		}
	}

}
