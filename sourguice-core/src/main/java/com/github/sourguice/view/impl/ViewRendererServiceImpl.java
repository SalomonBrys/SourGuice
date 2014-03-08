package com.github.sourguice.view.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.github.sourguice.controller.InstanceGetter;
import com.github.sourguice.view.NoViewRendererException;
import com.github.sourguice.view.ViewRenderer;
import com.github.sourguice.view.ViewRendererService;

/**
 * Holds all registered view renderers
 * Permits to the MVC system to convert string from the HTTP request to any type needed
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class ViewRendererServiceImpl implements ViewRendererService {

	/**
	 * Map of registered view name patterns and their associated renderers
	 */
	private final Map<Pattern, InstanceGetter<? extends ViewRenderer>> renderers = new HashMap<>();

	/**
	 * Register a view renderer to be associated with the given pattern
	 *
	 * @param pattern The view renderer to use for the given view name pattern
	 * @param renderer The view name pattern to associate the renderer with
	 */
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
