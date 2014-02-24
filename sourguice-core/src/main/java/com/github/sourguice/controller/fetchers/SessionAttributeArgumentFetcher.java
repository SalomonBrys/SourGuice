package com.github.sourguice.controller.fetchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.SessionAttribute;
import com.github.sourguice.request.Attribute;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link SessionAttribute} annotated arguments
 *
 * @param <T> The type of the argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class SessionAttributeArgumentFetcher<T> extends ArgumentFetcher<T> {

	public static class SessionAttributeAccessor<T> implements Attribute<T> {

		private HttpSession session;

		private String attribute;

		public SessionAttributeAccessor(HttpSession session, String attribute) {
			super();
			this.session = session;
			this.attribute = attribute;
		}

		@SuppressWarnings("unchecked")
		@Override public @CheckForNull T get() {
			return (T) session.getAttribute(attribute);
		}

		@Override public void set(T o) {
			session.setAttribute(attribute, o);
		}

	}

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private SessionAttribute infos;

	/**
	 * If the injection requires an {@link Attribute}, then this is true
	 */
	private boolean isAccessor = false;

	/**
	 * @see ArgumentFetcher#ArgumentFetcher(Type, int, Annotation[])
	 * @param type The type of the argument to fetch
	 * @param pos The position of the method's argument to fetch
	 * @param annotations Annotations that were found on the method's argument
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	public SessionAttributeArgumentFetcher(TypeLiteral<T> type, int pos, Annotation[] annotations, SessionAttribute infos) {
		super(type, pos, annotations);
		this.infos = infos;
		if (type.getRawType().equals(Attribute.class))
			isAccessor = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected @CheckForNull T getPrepared(HttpServletRequest req, @PathVariablesMap Map<String, String> pathVariables, Injector injector) {
		if (isAccessor)
			return (T) new SessionAttributeAccessor<>(req.getSession(true), infos.value());
		return (T)req.getSession(true).getAttribute(infos.value());
	}
}
