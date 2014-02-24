package com.github.sourguice.view.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;

import com.github.sourguice.controller.InstanceGetter;
import com.github.sourguice.view.Model;
import com.github.sourguice.view.NoViewRendererException;
import com.github.sourguice.view.ViewRenderer;
import com.github.sourguice.view.ViewRendererService;

public class ViewRendererServiceImpl implements ViewRendererService {

	private Map<Pattern, InstanceGetter<? extends ViewRenderer>> renderers = new HashMap<>();

	public void register(Pattern pattern, InstanceGetter<? extends ViewRenderer> renderer) {
		renderers.put(pattern, renderer);
	}

	@Override public @CheckForNull ViewRenderer getRenderer(String viewName) {
		for (Map.Entry<Pattern, InstanceGetter<? extends ViewRenderer>> entry : renderers.entrySet()) {
			if (entry.getKey().matcher(viewName).matches()) {
				return entry.getValue().getInstance();
			}
		}
		return null;
	}

	@Override public void render(String viewName, Model model) throws NoViewRendererException, Throwable {
		ViewRenderer renderer = getRenderer(viewName);
		if (renderer == null)
			throw new NoViewRendererException(viewName);
		renderer.render(viewName, model.asMap());
	}

}
