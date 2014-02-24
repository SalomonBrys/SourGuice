package com.github.sourguice.view;

import com.github.sourguice.controller.InstanceGetter;


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
	public abstract InstanceGetter<? extends ViewRenderer> getRenderer(String viewName) throws NoViewRendererException;

}
