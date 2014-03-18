package com.github.sourguice.controller.fetchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.conversion.ConversionService;
import com.github.sourguice.throwable.SGRuntimeException;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.value.ValueConstants;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link RequestParam} annotated collection arguments
 *
 * @param <T> The type of the collection argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RequestParamCollectionArgumentFetcher<T> extends ArgumentFetcher<T> {

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private final RequestParam infos;

	/**
	 * Provider reponsible for creating the collection
	 */
	private @CheckForNull CollectionProvider<?> collectionProvider;

	/**
	 * Collection component type
	 */
	private @CheckForNull TypeLiteral<?> collectionComponentType;

	/**
	 * A collection provider is responsible to create the collection
	 *
	 * @param <T> The real type of the collection to create
	 */
	private static interface CollectionProvider<T extends Collection<?>> {
		/**
		 * Create the collection
		 *
		 * @param inCol The items to put inside the new collection
		 * @return The created collection
		 */
		T get(Collection<?> inCol);
	}

	/**
	 * Create the collection provider corresponding to the given type
	 *
	 * @param rawType The type of the collection to create
	 * @return The collection provider that can create collections of the specified type
	 * @throws NoSuchMethodException If no provider could be found and the given type has no default constructor
	 */
	@SuppressWarnings({"PMD.LooseCoupling", "PMD.CyclomaticComplexity"})
	private static CollectionProvider<?> inferCollectionProvider(final Class<?> rawType) throws NoSuchMethodException {
		if (rawType.isAssignableFrom(ArrayList.class)) {
			return new CollectionProvider<ArrayList<?>>() {
				@Override public ArrayList<?> get(final Collection<?> inCol) {
					return new ArrayList<>(inCol);
			}};
		}
		else if (rawType.isAssignableFrom(LinkedList.class)) {
			return new CollectionProvider<LinkedList<?>>() {
				@Override public LinkedList<?> get(final Collection<?> inCol) {
					return new LinkedList<>(inCol);
			}};
		}
		else if (rawType.isAssignableFrom(HashSet.class)) {
			return new CollectionProvider<HashSet<?>>() {
				@Override public HashSet<?> get(final Collection<?> inCol) {
					return new HashSet<>(inCol);
			}};
		}
		else if (rawType.isAssignableFrom(TreeSet.class)) {
			return new CollectionProvider<TreeSet<?>>() {
				@Override public TreeSet<?> get(final Collection<?> inCol) {
					return new TreeSet<>(inCol);
			}};
		}
		final Constructor<?> constructor = rawType.getConstructor(Collection.class);
		return new CollectionProvider<Collection<?>>() {
			@Override public Collection<?> get(final Collection<?> inCol) {
				try {
					return (Collection<?>) constructor.newInstance(inCol);
				}
				catch (Exception e) {
					throw new UnsupportedOperationException(e);
				}
		}};
	}

	/**
	 * @see ArgumentFetcher#ArgumentFetcher(TypeLiteral, Annotation[])
	 *
	 * @param type The type of the collection argument to fetch
	 * @param annotations Annotations that were found on the method's argument
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	public RequestParamCollectionArgumentFetcher(final TypeLiteral<T> type, final Annotation[] annotations, final RequestParam infos) {
		super(type, annotations);
		this.infos = infos;

		try {
			this.collectionComponentType = TypeLiteral.get(((ParameterizedType)type.getSupertype(Collection.class).getType()).getActualTypeArguments()[0]);
			this.collectionProvider = inferCollectionProvider(type.getRawType());
		}
		catch (NoSuchMethodException e) {
			throw new SGRuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected @CheckForNull T getPrepared(final HttpServletRequest req, final @PathVariablesMap Map<String, String> pathVariables, final Injector injector) throws NoSuchRequestParameterException {
		assert this.collectionComponentType != null;
		assert this.collectionProvider != null;
		Object[] objs;
		if (req.getParameterValues(this.infos.value()) == null || req.getParameterValues(this.infos.value()).length == 0) {
			// If there are no value and not default value, throws the exception
			if (this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
				throw new NoSuchRequestParameterException(this.infos.value(), "request parameters");
			}
			if (this.infos.defaultValue().isEmpty()) {
				return (T) this.collectionProvider.get(new ArrayList<>());
			}
			objs = this.infos.defaultValue().split(",");
		}
		else {
			// Gets converted array and returns it as list
			objs = injector.getInstance(ConversionService.class).convertArray(this.collectionComponentType, req.getParameterValues(this.infos.value()));
		}
		return (T) this.collectionProvider.get(Arrays.asList(objs));
	}
}
