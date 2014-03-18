package com.github.sourguice.view;

import java.io.IOException;
import java.util.Map;

/**
 * All view renderer plugins must implement this
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public interface ViewRenderer {

	/**
	 * Asks the view renderer to render the given view.
	 * No action will be executed after this call SourGuice, so the renderer is free
	 * to do anything it needs, without caring about the context
	 *
	 * @param view Name of the view to render
	 * @param model Map that contains all variable names and values needed to construct the view
	 * @throws ViewRenderingException If anything went wrong during rendering
	 * @throws IOException IO failure
	 */
	public void render(String view, Map<String, Object> model) throws ViewRenderingException, IOException;
}
