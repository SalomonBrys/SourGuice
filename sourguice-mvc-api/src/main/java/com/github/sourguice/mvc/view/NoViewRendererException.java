package com.github.sourguice.mvc.view;


/**
 * Exception thrown when a renderer could not been found
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class NoViewRendererException extends RuntimeException {
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 147021115407274546L;

	/**
	 * @param viewName The name of the view for which no renderer could be found
	 */
	public NoViewRendererException(final String viewName) {
		super("Could not find renderer for " + viewName);
	}
}
