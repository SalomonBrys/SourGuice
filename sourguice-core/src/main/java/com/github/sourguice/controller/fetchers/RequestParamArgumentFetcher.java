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
	private RequestParam infos;

	private static interface CollectionProvider<T extends Collection<?>> {
		T get(Collection<?> in);
	}
	private @CheckForNull CollectionProvider<?> collectionProvider;
	private @CheckForNull TypeLiteral<?> collectionComponentType;

	private @CheckForNull Provider<? extends Map<Object, Object>> mapProvider;
	private @CheckForNull TypeLiteral<?> mapKeyType;
	private @CheckForNull TypeLiteral<?> mapValueType;

	/**
	 * @see ArgumentFetcher#ArgumentFetcher(Type, int, Annotation[])
	 * @param type The type of the argument to fetch
	 * @param pos The position of the method's argument to fetch
	 * @param annotations Annotations that were found on the method's argument
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	@SuppressWarnings("unchecked")
	public RequestParamArgumentFetcher(TypeLiteral<T> type, Annotation[] annotations, RequestParam infos) {
		super(type, annotations);
		this.infos = infos;

		Class<? super T> rawType = type.getRawType();
		try {
			if (Collection.class.isAssignableFrom(rawType)) {
				collectionComponentType = TypeLiteral.get(((ParameterizedType)type.getSupertype(Collection.class).getType()).getActualTypeArguments()[0]);
				if (rawType.isInterface()) {
					if (rawType.isAssignableFrom(ArrayList.class))
						collectionProvider = new CollectionProvider<ArrayList<?>>() {
							@Override public ArrayList<?> get(Collection<?> in) {
								return new ArrayList<>(in);
						}};
					else if (rawType.isAssignableFrom(LinkedList.class))
						collectionProvider = new CollectionProvider<LinkedList<?>>() {
							@Override public LinkedList<?> get(Collection<?> in) {
								return new LinkedList<>(in);
						}};
					else if (rawType.isAssignableFrom(HashSet.class))
						collectionProvider = new CollectionProvider<HashSet<?>>() {
							@Override public HashSet<?> get(Collection<?> in) {
								return new HashSet<>(in);
						}};
					else if (rawType.isAssignableFrom(TreeSet.class))
						collectionProvider = new CollectionProvider<TreeSet<?>>() {
							@Override public TreeSet<?> get(Collection<?> in) {
								return new TreeSet<>(in);
						}};
					else
						throw new RuntimeException("Cannot find implementation of " + rawType);
				}
				else {
					final Constructor<?> constructor = rawType.getConstructor(Collection.class);
					collectionProvider = new CollectionProvider<Collection<?>>() {
						@Override public Collection<?> get(Collection<?> in) {
							try {
								return (Collection<?>) constructor.newInstance(in);
							}
							catch (Exception e) {
								throw new RuntimeException(e);
							}
					}};
				}
			}
			else if (Map.class.isAssignableFrom(rawType)) {
				ParameterizedType mapType = (ParameterizedType) type.getSupertype(Map.class).getType();
				mapKeyType = TypeLiteral.get(mapType.getActualTypeArguments()[0]);
				mapValueType = TypeLiteral.get(mapType.getActualTypeArguments()[1]);
				if (rawType.isInterface()) {
					if (rawType.isAssignableFrom(HashMap.class))
						mapProvider = new Provider<HashMap<Object, Object>>() {
							@Override public HashMap<Object, Object> get() {
								return new HashMap<>();
							}
						};
					else
						throw new RuntimeException("Cannot find implementation of " + rawType);
				}
				else {
					final Constructor<?> constructor = rawType.getConstructor();
					mapProvider = new Provider<Map<Object, Object>>() {
						@Override public Map<Object, Object> get() {
							try {
								return (Map<Object, Object>) constructor.newInstance();
							}
							catch (Exception e) {
								throw new RuntimeException(e);
							}
					}};
				}
			}
		}
		catch (NoSuchMethodException e) { throw new RuntimeException(e); }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	protected @CheckForNull T getPrepared(HttpServletRequest req, @PathVariablesMap Map<String, String> pathVariables, Injector injector) throws NoSuchRequestParameterException {
		// TODO: Handle Sets & concrete collection types
		// If a List is requested, gets an array and converts it to list
		if (collectionProvider != null) {
			assert collectionComponentType != null;
			Object[] objs;
			if (req.getParameterValues(this.infos.value()) == null || req.getParameterValues(this.infos.value()).length == 0) {
				// If there are no value and not default value, throws the exception
				if (this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE))
					throw new NoSuchRequestParameterException(this.infos.value(), "request parameters");
				if (this.infos.defaultValue().isEmpty())
					return (T) collectionProvider.get(new ArrayList<>());
				objs = this.infos.defaultValue().split(",");
			}
			else
				// Gets converted array and returns it as list
				objs = injector.getInstance(ConversionService.class).convertArray(collectionComponentType, req.getParameterValues(this.infos.value()));
			return (T) collectionProvider.get(Arrays.asList(objs));
		}
		// If a Map is requested, gets all name[key] or name:key request parameter and fills the map with converted values
		if (mapProvider != null) {
			assert mapKeyType != null;
			assert mapValueType != null;
			Map<Object, Object> ret = mapProvider.get();
			Enumeration<String> names = req.getParameterNames();
			ConversionService conversionService = injector.getInstance(ConversionService.class);
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				if (name.startsWith(infos.value() + ":"))
					ret.put(
						conversionService.convert(mapKeyType, name.substring(infos.value().length() + 1)),
						conversionService.convert(mapValueType, req.getParameter(name))
					);
				else if (name.startsWith(infos.value() + "[") && name.endsWith("]"))
					ret.put(
						conversionService.convert(mapKeyType, name.substring(infos.value().length() + 1, name.length() - 1)),
						conversionService.convert(mapValueType, req.getParameter(name))
					);
			}
			if (ret.size() == 0) {
				if (this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE))
					throw new NoSuchRequestParameterException(this.infos.value(), "request parameters");
				if (!this.infos.defaultValue().isEmpty()) {
					String[] objs = this.infos.defaultValue().split(",");
					for (String obj : objs) {
						String[] split = obj.split("=", 2);
						if (split.length == 2)
							ret.put(
								conversionService.convert(mapKeyType, split[0]),
								conversionService.convert(mapValueType, split[1])
							);
					}
				}

			}
			return (T)ret;
		}
		// If the parameter does not exists, returns the default value or, if there are none, throw an exception
		if (req.getParameter(this.infos.value()) == null) {
			if (!this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE))
				return convert(injector, infos.defaultValue());
			throw new NoSuchRequestParameterException(infos.value(), "request parameters");
		}
		// Returns the converted parameter value
		if (req.getParameterValues(this.infos.value()).length == 1)
			return convert(injector, req.getParameter(infos.value()));
		return convert(injector, req.getParameterValues(infos.value()));
	}
}
