package com.github.sourguice.controller.fetchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.conversion.ConversionService;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.value.ValueConstants;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link RequestParam} annotated arguments
 *
 * @param <T> The type of the argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RequestParamArgumentFetcher<T> extends ArgumentFetcher<T> {

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private final RequestParam infos;

	private @CheckForNull CollectionProvider<?> collectionProvider;
	private @CheckForNull TypeLiteral<?> collectionComponentType;

	private @CheckForNull Provider<? extends Map<Object, Object>> mapProvider;
	private @CheckForNull TypeLiteral<?> mapKeyType;
	private @CheckForNull TypeLiteral<?> mapValueType;

	private static interface CollectionProvider<T extends Collection<?>> {
		T get(Collection<?> inCol);
	}

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
	 * @see ArgumentFetcher#ArgumentFetcher(Type, int, Annotation[])
	 * @param type The type of the argument to fetch
	 * @param pos The position of the method's argument to fetch
	 * @param annotations Annotations that were found on the method's argument
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	@SuppressWarnings({"unchecked", "PMD.AvoidThrowingRawExceptionTypes"})
	public RequestParamArgumentFetcher(final TypeLiteral<T> type, final Annotation[] annotations, final RequestParam infos) {
		super(type, annotations);
		this.infos = infos;

		final Class<? super T> rawType = type.getRawType();
		try {
			if (Collection.class.isAssignableFrom(rawType)) {
				this.collectionComponentType = TypeLiteral.get(((ParameterizedType)type.getSupertype(Collection.class).getType()).getActualTypeArguments()[0]);
				this.collectionProvider = inferCollectionProvider(rawType);
			}
			else if (Map.class.isAssignableFrom(rawType)) {
				final ParameterizedType mapType = (ParameterizedType) type.getSupertype(Map.class).getType();
				this.mapKeyType = TypeLiteral.get(mapType.getActualTypeArguments()[0]);
				this.mapValueType = TypeLiteral.get(mapType.getActualTypeArguments()[1]);
				if (rawType.isAssignableFrom(HashMap.class)) {
					this.mapProvider = new Provider<HashMap<Object, Object>>() {
						@Override public HashMap<Object, Object> get() {
							return new HashMap<>();
						}
					};
				}
				else {
					final Constructor<?> constructor = rawType.getConstructor();
					this.mapProvider = new Provider<Map<Object, Object>>() {
						@Override public Map<Object, Object> get() {
							try {
								return (Map<Object, Object>) constructor.newInstance();
							}
							catch (ReflectiveOperationException e) {
								throw new RuntimeException(e);
							}
					}};
				}
			}
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private @CheckForNull T getPreparedCollection(final HttpServletRequest req, final Injector injector) throws NoSuchRequestParameterException {
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

	private void fillMapWithDefaults(final Map<Object, Object> ret, final ConversionService conversionService) {
		if (!this.infos.defaultValue().isEmpty()) {
			final String[] objs = this.infos.defaultValue().split(",");
			for (final String obj : objs) {
				final String[] split = obj.split("=", 2);
				if (split.length == 2) {
					assert this.mapKeyType != null;
					assert this.mapValueType != null;
					ret.put(
						conversionService.convert(this.mapKeyType, split[0]),
						conversionService.convert(this.mapValueType, split[1])
					);
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	private T getPreparedMap(final HttpServletRequest req, final Injector injector) throws NoSuchRequestParameterException {
		assert this.mapKeyType != null;
		assert this.mapValueType != null;
		assert this.mapProvider != null;
		final Map<Object, Object> ret = this.mapProvider.get();
		final Enumeration<String> names = req.getParameterNames();
		final ConversionService conversionService = injector.getInstance(ConversionService.class);
		while (names.hasMoreElements()) {
			final String name = names.nextElement();
			if (name.startsWith(this.infos.value() + ":")) {
				ret.put(
					conversionService.convert(this.mapKeyType, name.substring(this.infos.value().length() + 1)),
					conversionService.convert(this.mapValueType, req.getParameter(name))
				);
			}
			else if (name.startsWith(this.infos.value() + "[") && name.endsWith("]")) {
				ret.put(
					conversionService.convert(this.mapKeyType, name.substring(this.infos.value().length() + 1, name.length() - 1)),
					conversionService.convert(this.mapValueType, req.getParameter(name))
				);
			}
		}
		if (ret.isEmpty()) {
			if (this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
				throw new NoSuchRequestParameterException(this.infos.value(), "request parameters");
			}
			fillMapWithDefaults(ret, conversionService);
		}
		return (T)ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected @CheckForNull T getPrepared(final HttpServletRequest req, final @PathVariablesMap Map<String, String> pathVariables, final Injector injector) throws NoSuchRequestParameterException {
		// If a List is requested, gets an array and converts it to list
		if (this.collectionProvider != null) {
			return getPreparedCollection(req, injector);
		}
		// If a Map is requested, gets all name[key] or name:key request parameter and fills the map with converted values
		if (this.mapProvider != null) {
			return getPreparedMap(req, injector);
		}
		// If the parameter does not exists, returns the default value or, if there are none, throw an exception
		if (req.getParameter(this.infos.value()) == null) {
			if (!this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
				return convert(injector, this.infos.defaultValue());
			}
			throw new NoSuchRequestParameterException(this.infos.value(), "request parameters");
		}
		// Returns the converted parameter value
		if (req.getParameterValues(this.infos.value()).length == 1) {
			return convert(injector, req.getParameter(this.infos.value()));
		}
		return convert(injector, req.getParameterValues(this.infos.value()));
	}
}
