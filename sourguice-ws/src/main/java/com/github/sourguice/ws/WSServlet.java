package com.github.sourguice.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.provider.TypedProvider;
import com.github.sourguice.ws.desc.builder.DescriptionBuilder;
import com.github.sourguice.ws.desc.struct.WSDescription;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class WSServlet extends HttpServlet {

	public static final String REST = "/rest/";

	private final Map<String, TypedProvider<?>> controllers = new HashMap<>();

	public final WSDescription description = new WSDescription();

	public void add(final TypedProvider<?> controller) {
		this.controllers.put(controller.getTypeLiteral().getRawType().getName(), controller);
	}

	private void doRest(final String pathInfo, final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		res.getWriter().write("REST");
	}

	private void doExplorer(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		res.getWriter().write("Explorer");
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		final String pathInfo = req.getPathInfo();

		if (pathInfo == null || pathInfo.equals("/")) {
			doExplorer(req, res);
		}
		else if (pathInfo.equals("/schema")) {
			res.setHeader("Access-Control-Allow-Origin", "*");
			res.setContentType("application/json");
			new Gson().toJson(this.description, res.getWriter());
		}
		else if (pathInfo.startsWith(REST)) {
			doRest(req.getPathInfo().substring(REST.length()), req, res);
		}
		else {
			super.doGet(req, res);
		}
	}

	public void initialize() {
		final DescriptionBuilder builder = new DescriptionBuilder(this.description);
		builder.build(this.controllers.values());
	}

}
