package com.github.sourguice.mvc.throwable.invocation;

import com.github.sourguice.mvc.annotation.request.PathVariable;
import com.github.sourguice.mvc.annotation.request.RequestMapping;

/**
 * Runtime exception that states that the request (through @{@link PathVariable} annotation) path variable
 * is not declared in the @{@link RequestMapping}
 *
 * This is a runtime exception because it is a programming error and therefore should only be caught in very specific circumstances.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class NoSuchPathVariableException extends RuntimeException {
	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = -8124867444876553230L;

	/**
	 * @param name The name of the undeclared path variable
	 * @param methodName The name of the method whose parameter could not be found
	 */
	public NoSuchPathVariableException(final String name, final String methodName) {
		super(	"@PathVariable " + name + " is not declared in @RequestMapping " + methodName);
	}
}
