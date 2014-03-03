package com.github.sourguice.view.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.github.sourguice.controller.InstanceGetter;
import com.github.sourguice.view.NoViewRendererException;
import com.github.sourguice.view.ViewRenderer;
import com.github.sourguice.view.ViewRendererService;

public class ViewRendererServiceImpl implements ViewRendererService {

	private final Map<Pattern, InstanceGetter<? extends ViewRenderer>> renderers = new HashMap<>();

	public void register(final Pattern pattern, final InstanceGetter<? extends ViewRenderer> renderer) {
		this.renderers.put(pattern, renderer);
	}

	@Override public InstanceGetter<? extends ViewRenderer> getRenderer(final String viewName) throws NoViewRendererException {
		for (final Map.Entry<Pattern, InstanceGetter<? extends ViewRenderer>> entry : this.renderers.entrySet()) {
			if (entry.getKey().matcher(viewName).matches()) {
				return entry.getValue();
			}
		}
		throw new NoViewRendererException(viewName);
	}

}
