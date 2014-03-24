package com.github.sourguice.view.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.github.sourguice.controller.TypedProvider;
import com.github.sourguice.view.NoViewRendererException;
import com.github.sourguice.view.ViewRenderer;
import com.github.sourguice.view.ViewRendererService;

/**
 * Holds all registered view renderers
 * Permits SourGuice to convert string from the HTTP request to any type needed
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class ViewRendererServiceImpl implements ViewRendererService {

	/**
	 * Map of registered view name patterns and their associated renderers
	 */
	private final Map<Pattern, TypedProvider<? extends ViewRenderer>> renderers = new HashMap<>();

	/**
	 * Register a view renderer to be associated with the given pattern
	 *
	 * @param pattern The view renderer to use for the given view name pattern
	 * @param renderer The view name pattern to associate the renderer with
	 */
	public void register(final Pattern pattern, final TypedProvider<? extends ViewRenderer> renderer) {
		this.renderers.put(pattern, renderer);
	}

	@Override public TypedProvider<? extends ViewRenderer> getRenderer(final String viewName) throws NoViewRendererException {
		for (final Map.Entry<Pattern, TypedProvider<? extends ViewRenderer>> entry : this.renderers.entrySet()) {
			if (entry.getKey().matcher(viewName).matches()) {
				return entry.getValue();
			}
		}
		throw new NoViewRendererException(viewName);
	}

}
