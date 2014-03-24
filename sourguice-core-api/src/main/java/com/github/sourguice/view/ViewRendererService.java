package com.github.sourguice.view;

import com.github.sourguice.controller.TypedProvider;


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
	 * @throws NoViewRendererException If no renderer was found for this view
	 */
	public abstract TypedProvider<? extends ViewRenderer> getRenderer(String viewName) throws NoViewRendererException;

}
