package com.github.sourguice.exception.def;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.throwable.controller.SGResponseException;

/**
 * Exception handler that handles {@link SGResponseException}
 * These exceptions execute treatment on the HttpServletResponse
 * like sendRedirect or sendError.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class SGResponseExceptionHandler implements ExceptionHandler<SGResponseException> {

	/**
	 * Executes the treatment to the HttpResponse and declare the exception as handled
	 */
	@Override
	public boolean handle(final SGResponseException exception, final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		exception.execute(res);
		return true;
	}

}
