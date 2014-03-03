package com.github.sourguice.controller.fetchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.RequestAttribute;
import com.github.sourguice.request.Attribute;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link RequestAttribute} annotated arguments
 *
 * @param <T> The type of the argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RequestAttributeArgumentFetcher<T> extends ArgumentFetcher<T> {

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private final RequestAttribute infos;

	/**
	 * If the injection requires an {@link Attribute}, then this is true
	 */
	private boolean isAccessor = false;


	public static class RequestAttributeAccessor<T> implements Attribute<T> {

		private final HttpServletRequest req;

		private final String attribute;

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
	 * @see ArgumentFetcher#ArgumentFetcher(Type, int, Annotation[])
	 * @param type The type of the argument to fetch
	 * @param pos The position of the method's argument to fetch
	 * @param annotations Annotations that were found on the method's argument
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	public RequestAttributeArgumentFetcher(final TypeLiteral<T> type, final Annotation[] annotations, final RequestAttribute infos) {
		super(type, annotations);
		this.infos = infos;
		if (type.getRawType().equals(Attribute.class)) {
			this.isAccessor = true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected @CheckForNull T getPrepared(final HttpServletRequest req, final @PathVariablesMap Map<String, String> pathVariables, final Injector injector) {
		if (this.isAccessor) {
			return (T) new RequestAttributeAccessor<>(req, this.infos.value());
		}
		return (T)req.getAttribute(this.infos.value());
	}
}
