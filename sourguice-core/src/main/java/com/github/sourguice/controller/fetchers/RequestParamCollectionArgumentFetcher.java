package com.github.sourguice.controller.fetchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.conversion.ConversionService;
import com.github.sourguice.throwable.SGRuntimeException;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.value.ValueConstants;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link RequestParam} annotated collection arguments
 *
 * @param <T> The type of the collection argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RequestParamCollectionArgumentFetcher<T> implements RequestParamArgumentFetcher.Delegate<T> {

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
	 * The method whose argument we are fetching
	 */
	private final String methodName;

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
	 * @param type The type of the collection argument to fetch
	 * @param infos The annotations containing needed informations to fetch the argument
	 * @param methodName The name of the method whose argument we are fetching
	 */
	public RequestParamCollectionArgumentFetcher(final TypeLiteral<T> type, final RequestParam infos, final String methodName) {
		super();
		this.infos = infos;
		this.methodName = methodName;

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
	public @CheckForNull T getPrepared(final HttpServletRequest req, final ConversionService conversionService) throws NoSuchRequestParameterException {
		assert this.collectionComponentType != null;
		assert this.collectionProvider != null;
		Object[] objs;
		if (req.getParameterValues(this.infos.value()) == null || req.getParameterValues(this.infos.value()).length == 0) {
			// If there are no value and not default value, throws the exception
			if (this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
				throw new NoSuchRequestParameterException(this.infos.value(), "request parameters", this.methodName);
			}
			if (this.infos.defaultValue().isEmpty()) {
				return (T) this.collectionProvider.get(new ArrayList<>());
			}
			objs = this.infos.defaultValue().split(",");
		}
		else {
			// Gets converted array and returns it as list
			objs = conversionService.convertArray(this.collectionComponentType, req.getParameterValues(this.infos.value()));
		}
		return (T) this.collectionProvider.get(Arrays.asList(objs));
	}
}
