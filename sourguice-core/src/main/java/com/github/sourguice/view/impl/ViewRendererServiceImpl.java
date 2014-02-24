package com.github.sourguice.view.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.github.sourguice.controller.InstanceGetter;
import com.github.sourguice.view.NoViewRendererException;
import com.github.sourguice.view.ViewRenderer;
import com.github.sourguice.view.ViewRendererService;

public class ViewRendererServiceImpl implements ViewRendererService {

	private Map<Pattern, InstanceGetter<? extends ViewRenderer>> renderers = new HashMap<>();

	public void register(Pattern pattern, InstanceGetter<? extends ViewRenderer> renderer) {
		renderers.put(pattern, renderer);
	}

	@Override public InstanceGetter<? extends ViewRenderer> getRenderer(String viewName) throws NoViewRendererException {
		for (Map.Entry<Pattern, InstanceGetter<? extends ViewRenderer>> entry : renderers.entrySet()) {
			if (entry.getKey().matcher(viewName).matches()) {
				return entry.getValue();
			}
		}
		throw new NoViewRendererException(viewName);
	}

}
