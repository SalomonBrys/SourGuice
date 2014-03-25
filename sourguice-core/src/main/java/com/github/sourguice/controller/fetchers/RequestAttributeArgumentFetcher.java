package com.github.sourguice.controller.fetchers;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.RequestAttribute;
import com.github.sourguice.call.ArgumentFetcher;
import com.github.sourguice.request.Attribute;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link RequestAttribute} annotated arguments
 *
 * @param <T> The type of the argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RequestAttributeArgumentFetcher<T> implements ArgumentFetcher<T> {

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private final RequestAttribute infos;

	/**
	 * If the injection requires an {@link Attribute}, then this is true
	 */
	private boolean isAccessor = false;

	/**
	 * Provider for the current HTTP request
	 */
	@Inject
	private @CheckForNull Provider<HttpServletRequest> requestProvider;

	/**
	 * Attribute accessor specialization for Request Attributes
	 *
	 * @param <T> The type of the attribute
	 */
	public static class RequestAttributeAccessor<T> implements Attribute<T> {

		/**
		 * The request to read / write the attribute from
		 */
		private final HttpServletRequest req;

		/**
		 * The attribute name
		 */
		private final String attribute;

		/**
		 * @param req The request to read / write the attribute from
		 * @param attribute The attribute name
		 */
		public RequestAttributeAccessor(final HttpServletRequest req, final String attribute) {
			super();
			this.req = req;
			this.attribute = attribute;
		}

		@SuppressWarnings("unchecked")
		@Override public @CheckForNull T get() {
			return (T) this.req.getAttribute(this.attribute);
		}

		@Override public void set(final T value) {
			this.req.setAttribute(this.attribute, value);
		}

	}

	/**
	 * @param type The type of the argument to fetch
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	public RequestAttributeArgumentFetcher(final TypeLiteral<T> type, final RequestAttribute infos) {
		super();
		this.infos = infos;
		if (type.getRawType().equals(Attribute.class)) {
			this.isAccessor = true;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public @CheckForNull T getPrepared() {
		assert this.requestProvider != null;
		final HttpServletRequest req = this.requestProvider.get();

		if (this.isAccessor) {
			return (T) new RequestAttributeAccessor<>(req, this.infos.value());
		}

		return (T)req.getAttribute(this.infos.value());
	}
}
