package com.github.sourguice.controller.fetchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
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

	/**
	 * @see ArgumentFetcher#ArgumentFetcher(Type, int, Annotation[])
	 * @param type The type of the argument to fetch
	 * @param pos The position of the method's argument to fetch
	 * @param annotations Annotations that were found on the method's argument
	 * @param infos The annotations containing needed informations to fetch the argument
	 */
	public RequestParamArgumentFetcher(TypeLiteral<T> type, int pos, Annotation[] annotations, RequestParam infos) {
		super(type, pos, annotations);
		this.infos = infos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	protected @CheckForNull T getPrepared(HttpServletRequest req, @PathVariablesMap Map<String, String> pathVariables, Injector injector) throws NoSuchRequestParameterException {
		ConversionService conversionService = injector.getInstance(ConversionService.class);
		// TODO: Handle Sets & concrete collection types
		// If a List is requested, gets an array and converts it to list
		if (type.getRawType().equals(List.class)) {
			Object[] objs;
			if (req.getParameterValues(this.infos.value()) == null || req.getParameterValues(this.infos.value()).length == 0) {
				// If there are no value and not default value, throws the exception
				if (this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE))
					throw new NoSuchRequestParameterException(this.infos.value(), "request parameters");
				if (this.infos.defaultValue().isEmpty())
					return (T) new ArrayList<>();
				objs = this.infos.defaultValue().split(",");
			}
			else
				// Gets converted array and returns it as list
				objs = conversionService.convertArray(TypeLiteral.get(((ParameterizedType)type.getSupertype(List.class).getType()).getActualTypeArguments()[0]).getRawType(), req.getParameterValues(this.infos.value()));
			return (T)Arrays.asList(objs);
		}
		// If a Map is requested, gets all name[key] or name:key request parameter and fills the map with converted values
		if (type.getRawType().equals(Map.class)) {
			Map<Object, Object> ret = new HashMap<>();
			Enumeration<String> names = req.getParameterNames();
			ParameterizedType mapType = (ParameterizedType) type.getSupertype(Map.class).getType();
			Class<?> keyClass = TypeLiteral.get(mapType.getActualTypeArguments()[0]).getRawType();
			Class<?> valueClass = TypeLiteral.get(mapType.getActualTypeArguments()[1]).getRawType();
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				if (name.startsWith(infos.value() + ":"))
					ret.put(
						conversionService.convert(keyClass, name.substring(infos.value().length() + 1)),
						conversionService.convert(valueClass, req.getParameter(name))
					);
				else if (name.startsWith(infos.value() + "[") && name.endsWith("]"))
					ret.put(
						conversionService.convert(keyClass, name.substring(infos.value().length() + 1, name.length() - 1)),
						conversionService.convert(valueClass, req.getParameter(name))
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
								conversionService.convert(keyClass, split[0]),
								conversionService.convert(valueClass, split[1])
							);
					}
				}

			}
			return (T)ret;
		}
		// If the parameter does not exists, returns the default value or, if there are none, throw an exception
		if (req.getParameter(this.infos.value()) == null) {
			if (!this.infos.defaultValue().equals(ValueConstants.DEFAULT_NONE))
				return (T) conversionService.convert(type.getRawType(), infos.defaultValue());
			throw new NoSuchRequestParameterException(infos.value(), "request parameters");
		}
		// Returns the converted parameter value
		if (req.getParameterValues(this.infos.value()).length == 1)
			return (T) conversionService.convert(type.getRawType(), req.getParameter(infos.value()));
		return (T) conversionService.convert(type.getRawType(), req.getParameterValues(infos.value()));
	}
}
