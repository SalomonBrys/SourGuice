package com.github.sourguice.call.impl.fetchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.conversion.ConversionService;
import com.github.sourguice.throwable.SGRuntimeException;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.value.ValueConstants;
import com.google.inject.TypeLiteral;

/**
 * Fetcher that handles @{@link RequestParam} annotated map arguments
 *
 * @param <T> The type of the map argument to fetch
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class RequestParamMapArgumentFetcher<T> implements RequestParamArgumentFetcher.Delegate<T> {

	/**
	 * The annotations containing needed informations to fetch the argument
	 */
	private final RequestParam infos;

	/**
	 * Provider is reponsible for creating the map
	 */
	private @CheckForNull Provider<? extends Map<Object, Object>> mapProvider;

	/**
	 * Map key type
	 */
	private @CheckForNull TypeLiteral<?> mapKeyType;

	/**
	 * Map value type
	 */
	private @CheckForNull TypeLiteral<?> mapValueType;

	/**
	 * The method whose argument we are fetching
	 */
	private final String methodName;

	/**
	 * Create the map provider corresponding to the given type
	 *
	 * @param rawType The type of the map to create
	 * @return The map provider that can create map of the specified type
	 * @throws NoSuchMethodException If no provider could be found and the given type has no default constructor
	 */
	@SuppressWarnings("PMD.LooseCoupling")
	private static Provider<? extends Map<Object, Object>> inferMapProvider(final Class<?> rawType) throws NoSuchMethodException {
		if (rawType.isAssignableFrom(HashMap.class)) {
			return new Provider<HashMap<Object, Object>>() {
				@Override public HashMap<Object, Object> get() {
					return new HashMap<>();
				}
			};
		}
		final Constructor<?> constructor = rawType.getConstructor();
		return new Provider<Map<Object, Object>>() {
			@SuppressWarnings("unchecked")
			@Override public Map<Object, Object> get() {
				try {
					return (Map<Object, Object>) constructor.newInstance();
				}
				catch (ReflectiveOperationException e) {
					throw new UnsupportedOperationException(e);
				}
			}
		};
	}

	/**
	 * @param type The type of the map argument to fetch
	 * @param infos The annotations containing needed informations to fetch the argument
	 * @param methodName The name of the method whose argument we are fetching
	 */
	public RequestParamMapArgumentFetcher(final TypeLiteral<T> type, final RequestParam infos, final String methodName) {
		super();
		this.infos = infos;
		this.methodName = methodName;

		try {
			final ParameterizedType mapType = (ParameterizedType) type.getSupertype(Map.class).getType();
			this.mapKeyType = TypeLiteral.get(mapType.getActualTypeArguments()[0]);
			this.mapValueType = TypeLiteral.get(mapType.getActualTypeArguments()[1]);
			this.mapProvider = inferMapProvider(type.getRawType());
		}
		catch (NoSuchMethodException e) {
			throw new SGRuntimeException(e);
		}
	}

	/**
	 * Fills the given map the with the request content
	 *
	 * @param ret The map to fill
	 * @param conversionService The conversion service to use
	 */
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

	@SuppressWarnings("unchecked")
	@Override
	public @CheckForNull T getPrepared(final HttpServletRequest req, final ConversionService conversionService) throws NoSuchRequestParameterException {
		assert this.mapKeyType != null;
		assert this.mapValueType != null;
		assert this.mapProvider != null;
		final Map<Object, Object> ret = this.mapProvider.get();
		final Enumeration<String> names = req.getParameterNames();
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
				throw new NoSuchRequestParameterException(this.infos.value(), "request parameters", this.methodName);
			}
			fillMapWithDefaults(ret, conversionService);
		}
		return (T)ret;
	}
}
