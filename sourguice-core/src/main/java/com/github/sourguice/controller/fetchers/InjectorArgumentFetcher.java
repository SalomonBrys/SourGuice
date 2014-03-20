package com.github.sourguice.controller.fetchers;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.utils.Annotations;
import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles argument that are not annotated with an annotation handled by previous fetchers
 * This will fetch the argument from Guice
 *
 * @param <T> The type of object to fetch from Guice
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class InjectorArgumentFetcher<T> extends ArgumentFetcher<T> {

	/**
	 * A Guice {@link BindingAnnotation}, if there is one
	 */
	private Key<T> key;

	/**
	 * @see ArgumentFetcher#ArgumentFetcher(TypeLiteral)
	 *
	 * @param type The type of the argument to fetch
	 * @param annotations Annotations that were found on the method's argument
	 */
	public InjectorArgumentFetcher(final TypeLiteral<T> type, final Annotation[] annotations) {
		super(type);

		Annotation bindingAnnotation = Annotations.getOneAnnotated(BindingAnnotation.class, annotations);
		if (bindingAnnotation == null) {
			bindingAnnotation = Annotations.fromArray(annotations).getAnnotation(Named.class);
		}

		if (bindingAnnotation != null) {
			this.key = Key.get(type, bindingAnnotation);
		}
		else {
			this.key = Key.get(type);
		}
	}

	@Override
	public @CheckForNull T getPrepared(final HttpServletRequest req, final @PathVariablesMap Map<String, String> pathVariables, final Injector injector) {
		return injector.getInstance(this.key);
	}
}
