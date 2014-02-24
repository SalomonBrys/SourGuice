package com.github.sourguice.view;

import javax.annotation.CheckForNull;

import com.github.sourguice.throwable.service.converter.NoConverterException;

/**
 * Singleton service that handles view renderers
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public interface ViewRendererService {

	/**
	 * Gets the first view renderer that says it can render the view
	 * If none is found, returns null
	 *
	 * @param viewName the name of the view
	 * @return the renderer to use or null if none were found
	 */
	public @CheckForNull
	abstract ViewRenderer getRenderer(String viewName);

	/**
	 * Renders a view
	 *
	 * @param viewName The name of the view to render
	 * @throws NoConverterException When no converter is found for the specific type (RuntimeException)
	 */
	public @CheckForNull
	abstract void render(String viewName, Model model) throws NoViewRendererException, Throwable;

}
