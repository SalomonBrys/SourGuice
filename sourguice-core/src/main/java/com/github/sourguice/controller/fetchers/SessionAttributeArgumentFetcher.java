package com.github.sourguice.controller.fetchers;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.github.sourguice.annotation.request.SessionAttribute;
import com.github.sourguice.call.ArgumentFetcher;
import com.github.sourguice.request.Attribute;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link SessionAttribute} annotated arguments
 *
 * @param <T> The type of the argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class SessionAttributeArgumentFetcher<T> implements ArgumentFetcher<T> {

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private final SessionAttribute infos;

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
	 * Attribute accessor specialization for Session Attributes
	 *
	 * @param <T> The type of the attribute
	 */
	public static class SessionAttributeAccessor<T> implements Attribute<T> {

		/**
		 * The session to read / write the attribute from
		 */
		private final HttpSession session;

		/**
		 * The attribute name
		 */
		private final String attribute;

		/**
		 * @param session The session to read / write the attribute from
		 * @param attribute The attribute name
		 */
		public SessionAttributeAccessor(final HttpSession session, final String attribute) {
			super();
			this.session = session;
			this.attribute = attribute;
		}

		@SuppressWarnings("unchecked")
		@Override public @CheckForNull T get() {
			return (T) this.session.getAttribute(this.attribute);
		}

		@Override public void set(final T value) {
			this.session.setAttribute(this.attribute, value);
		}

	}

	/**
	 * @param type The type of the argument to fetch
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	public SessionAttributeArgumentFetcher(final TypeLiteral<T> type, final SessionAttribute infos) {
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
			return (T) new SessionAttributeAccessor<>(req.getSession(true), this.infos.value());
		}

		return (T)req.getSession(true).getAttribute(this.infos.value());
	}
}
