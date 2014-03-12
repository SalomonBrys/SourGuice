package com.github.sourguice.controller.fetchers;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.value.ValueConstants;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link RequestParam} annotated arguments
 *
 * @param <T> The type of the argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RequestParamArgumentFetcher<T> extends ArgumentFetcher<T> {

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private final RequestParam infos;

	/**
	 * If this fetcher is supposed to fetch a collection or a map, then it's a specialized delegate that will handle it
	 */
	private @CheckForNull ArgumentFetcher<T> delegate = null;

	/**
	 * @see ArgumentFetcher#ArgumentFetcher(TypeLiteral, Annotation[])
	 *
	 * @param type The type of the argument to fetch
	 * @param annotations Annotations that were found on the method's argument
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes"})
	public RequestParamArgumentFetcher(final TypeLiteral<T> type, final Annotation[] annotations, final RequestParam infos) {
		super(type, annotations);
		this.infos = infos;

		final Class<? super T> rawType = type.getRawType();
		if (Collection.class.isAssignableFrom(rawType)) {
			this.delegate = new RequestParamCollectionArgumentFetcher<>(type, annotations, infos);
		}
		else if (Map.class.isAssignableFrom(rawType)) {
			this.delegate = new RequestParamMapArgumentFetcher<>(type, annotations, infos);
		}
	}

	@Override
	protected @CheckForNull T getPrepared(final HttpServletRequest req, final @PathVariablesMap Map<String, String> pathVariables, final Injector injector) throws NoSuchRequestParameterException {
		// If there is a specialized delegate, let it handle the fetch
		if (this.delegate != null) {
			return this.delegate.getPrepared(req, pathVariables, injector);
		}

		// If the parameter does not exists, returns the default value or, if there are none, throw an exception
		if (req.getParameter(this.infos.value()) == null) {
			if (!this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
				return convert(injector, this.infos.defaultValue());
			}
			throw new NoSuchRequestParameterException(this.infos.value(), "request parameters");
		}
		// Returns the converted parameter value
		if (req.getParameterValues(this.infos.value()).length == 1) {
			return convert(injector, req.getParameter(this.infos.value()));
		}
		return convert(injector, req.getParameterValues(this.infos.value()));
	}
}
