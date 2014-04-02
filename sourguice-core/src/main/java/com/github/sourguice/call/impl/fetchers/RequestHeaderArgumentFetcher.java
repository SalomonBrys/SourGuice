package com.github.sourguice.call.impl.fetchers;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.RequestHeader;
import com.github.sourguice.call.ConvertArgumentFetcher;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.value.ValueConstants;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link RequestHeader} annotated arguments
 *
 * @param <T> The type of the argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RequestHeaderArgumentFetcher<T> extends ConvertArgumentFetcher<T> {

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
	 * The method whose argument we are fetching
	 */
	private final String methodName;

	/**
	 * @see ConvertArgumentFetcher#ConvertArgumentFetcher(TypeLiteral)
	 *
	 * @param type The type of the argument to fetch
	 * @param infos The annotations containing needed informations to fetch the argument
	 * @param methodName The name of the method whose argument we are fetching
	 */
	public RequestHeaderArgumentFetcher(final TypeLiteral<T> type, final RequestHeader infos, final String methodName) {
		super(type);
		this.infos = infos;
		this.methodName = methodName;
	}

	@Override
	public @CheckForNull T getPrepared() throws NoSuchRequestParameterException {
		assert this.requestProvider != null;
		final HttpServletRequest req = this.requestProvider.get();

		if (req.getHeader(this.infos.value()) == null) {
			if (!this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
				return convert(this.infos.defaultValue());
			}
			throw new NoSuchRequestParameterException(this.infos.value(), "header", this.methodName);
		}

		return convert(req.getHeader(this.infos.value()));
	}
}
