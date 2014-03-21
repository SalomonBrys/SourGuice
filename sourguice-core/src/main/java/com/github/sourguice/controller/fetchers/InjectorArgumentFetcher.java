package com.github.sourguice.controller.fetchers;

import java.lang.annotation.Annotation;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Named;

import com.github.sourguice.controller.ArgumentFetcher;
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
public class InjectorArgumentFetcher<T> implements ArgumentFetcher<T> {

	/**
	 * A Guice {@link BindingAnnotation}, if there is one
	 */
	private Key<T> key;

	/**
	 * Guice injector, later used to retrieve instances
	 */
	@Inject
	private @CheckForNull Injector injector;

	/**
	 * @param type The type of the argument to fetch
	 * @param annotations Annotations that were found on the method's argument
	 */
	public InjectorArgumentFetcher(final TypeLiteral<T> type, final Annotation[] annotations) {
		super();

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
	public @CheckForNull T getPrepared() {
		assert this.injector != null;
		return this.injector.getInstance(this.key);
	}
}
