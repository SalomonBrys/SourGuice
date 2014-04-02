package com.github.sourguice.mvc.view;

/**
 * Exception thrown by a view renderer
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class ViewRenderingException extends Exception {

	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 8113562035240251941L;

	/**
	 * @see Exception#Exception()
	 */
	public ViewRenderingException() {
		super();
	}

	/**
	 * @see Exception#Exception(String, Throwable, boolean, boolean)
	 */
	public ViewRenderingException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public ViewRenderingException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see Exception#Exception(String)
	 */
	public ViewRenderingException(final String message) {
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public ViewRenderingException(final Throwable cause) {
		super(cause);
	}

}
