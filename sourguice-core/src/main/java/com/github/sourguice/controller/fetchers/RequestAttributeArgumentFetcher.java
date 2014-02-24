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

	public static class RequestAttributeAccessor<T> implements Attribute<T> {

		private HttpServletRequest req;

		private String attribute;

		public RequestAttributeAccessor(HttpServletRequest req, String attribute) {
			super();
			this.req = req;
			this.attribute = attribute;
		}

		@SuppressWarnings("unchecked")
		@Override public @CheckForNull T get() {
			return (T) req.getAttribute(attribute);
		}

		@Override public void set(T o) {
			req.setAttribute(attribute, o);
		}

	}

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private RequestAttribute infos;

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
	public RequestAttributeArgumentFetcher(TypeLiteral<T> type, int pos, Annotation[] annotations, RequestAttribute infos) {
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
			return (T) new RequestAttributeAccessor<>(req, infos.value());
		return (T)req.getAttribute(infos.value());
	}
}
