package com.github.sourguice.ws;

import java.io.IOException;
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

	private final Map<String, TypedProvider<?>> controllers = new HashMap<>();

	public final WSDescription description = new WSDescription();

	public void add(final TypedProvider<?> controler) {
		this.controllers.put(controler.getTypeLiteral().getRawType().getName(), controler);
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
//		res.getWriter().write(req.getPathInfo());
		new Gson().toJson(this.description, res.getWriter());
	}

	public void initialize() {
		final DescriptionBuilder builder = new DescriptionBuilder(this.description);
		builder.build(this.controllers.values());
	}

}
