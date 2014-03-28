package com.github.sourguice.throwable.invocation;

import com.github.sourguice.annotation.request.RequestParam;

/**
 * Exception thrown when a @{@link RequestParam} annotated parameter is not found in the request
 * and the annotation does not provide a default implementation
 *
 * This exception is caught by SourGuice and provides a default error page stating that the parameter is missing
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class NoSuchRequestParameterException extends Exception {
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 8013443036995570231L;

	/**
	 * The name of the missing parameter
	 */
	private final String name;

	/**
	 * The type of the missing parameter
	 */
	private final String type;

	/**
	 * @param name The name of the missing parameter
	 * @param type The type of the missing parameter
	 * @param methodName The name of the method whose parameter could not be found
	 */
	public NoSuchRequestParameterException(final String name, final String type, final String methodName) {
		super("Missing " + type + ": " + name + " in " + methodName);
		this.name = name;
		this.type = type;
	}

	/**
	 * @return The name of the missing parameter
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return The type of the missing parameter
	 */
	public String getType() {
		return this.type;
	}
}
