package com.github.sourguice.controller.fetchers;

import java.util.Map;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.PathVariable;
import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.RequestHeader;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.value.ValueConstants;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link PathVariable} annotated arguments
 *
 * @param <T> The type of the argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RequestHeaderArgumentFetcher<T> extends ArgumentFetcher<T> {

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private final RequestHeader infos;

	/**
	 * @see ArgumentFetcher#ArgumentFetcher(TypeLiteral)
	 *
	 * @param type The type of the argument to fetch
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	public RequestHeaderArgumentFetcher(final TypeLiteral<T> type, final RequestHeader infos) {
		super(type);
		this.infos = infos;
	}

	@Override
	public @CheckForNull T getPrepared(final HttpServletRequest req, final @PathVariablesMap Map<String, String> pathVariables, final Injector injector) throws NoSuchRequestParameterException {
		if (req.getHeader(this.infos.value()) == null) {
			if (!this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
				return convert(injector, this.infos.defaultValue());
			}
			throw new NoSuchRequestParameterException(this.infos.value(), "header");
		}
		return convert(injector, req.getHeader(this.infos.value()));
	}
}
