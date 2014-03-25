package com.github.sourguice.exception.def;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.throwable.controller.SGResponseException;
import com.google.inject.Singleton;

/**
 * Exception handler that handles {@link SGResponseException}
 * These exceptions execute treatment on the HttpServletResponse
 * like sendRedirect or sendError.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Singleton
public class SGResponseExceptionHandler implements ExceptionHandler<SGResponseException> {

	/**
	 * Provider for the current HTTP response on which the exception will be executed
	 */
	private final Provider<HttpServletResponse> responseProvider;

	/**
	 * Constructor
	 *
	 * @param responseProvider Provider for the current HTTP response on which the exception will be executed
	 */
	@Inject
	public SGResponseExceptionHandler(final Provider<HttpServletResponse> responseProvider) {
		super();
		this.responseProvider = responseProvider;
	}

	/**
	 * Executes the treatment to the HttpResponse and declare the exception as handled
	 */
	@Override
	public boolean handle(final SGResponseException exception) throws IOException {
		exception.execute(this.responseProvider.get());
		return true;
	}

}
