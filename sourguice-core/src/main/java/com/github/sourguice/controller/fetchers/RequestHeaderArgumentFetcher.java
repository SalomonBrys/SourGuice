package com.github.sourguice.controller.fetchers;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.PathVariable;
import com.github.sourguice.annotation.request.RequestHeader;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.value.ValueConstants;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link PathVariable} annotated arguments
 *
 * @param <T> The type of the argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RequestHeaderArgumentFetcher<T> extends AbstractArgumentFetcher<T> {

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private final RequestHeader infos;

	/**
	 * Provider for the current HTTP request
	 */
	@Inject
	private @CheckForNull Provider<HttpServletRequest> requestProvider;

	/**
	 * @see AbstractArgumentFetcher#AbstractArgumentFetcher(TypeLiteral)
	 *
	 * @param type The type of the argument to fetch
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	public RequestHeaderArgumentFetcher(final TypeLiteral<T> type, final RequestHeader infos) {
		super(type);
		this.infos = infos;
	}

	@Override
	public @CheckForNull T getPrepared() throws NoSuchRequestParameterException {
		assert this.requestProvider != null;
		final HttpServletRequest req = this.requestProvider.get();

		if (req.getHeader(this.infos.value()) == null) {
			if (!this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
				return convert(this.infos.defaultValue());
			}
			throw new NoSuchRequestParameterException(this.infos.value(), "header");
		}

		return convert(req.getHeader(this.infos.value()));
	}
}
