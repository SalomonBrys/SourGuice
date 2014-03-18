package com.github.sourguice.conversion.impl;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.inject.Singleton;

import com.github.sourguice.annotation.ConverterCanConstructChild;
import com.github.sourguice.controller.GivenInstanceGetter;
import com.github.sourguice.controller.InstanceGetter;
import com.github.sourguice.conversion.ConversionService;
import com.github.sourguice.conversion.Converter;
import com.github.sourguice.conversion.def.ArrayConverter;
import com.github.sourguice.throwable.service.converter.CannotConvertToPrimitiveException;
import com.github.sourguice.throwable.service.converter.NoConverterException;
import com.github.sourguice.throwable.service.converter.NotAStringException;
import com.google.inject.TypeLiteral;

/**
 * Holds all registered converters
 * Permits SourGuice to convert string from the HTTP request to any type needed
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@SuppressWarnings("unchecked")
@Singleton
public class ConversionServiceImpl implements ConversionService {

	/**
	 * Map of registered convertable classes and their associated converter
	 */
	private final Map<Class<?>, InstanceGetter<? extends Converter<?>>> converters = new HashMap<>();

	/**
	 * Register a converter to be associated with the given type
	 * If the type is of type array, than it will register the converter for the array type AND its subtype
	 *
	 * @param conv The converter to use when converting from String to the given type
	 * @param type The type to associate the converter with
	 */
	public void register(final Class<?> type, final InstanceGetter<? extends Converter<?>> conv) {
		synchronized (this.converters) {
			this.converters.put(type, conv);
		}
	}

	/**
	 * Utility to calculate the "distance" between a class and one of its parents
	 * if child == parent, distance = 0
	 * if child inherits directly parent, distance == 1
	 *
	 * @param child The child class
	 * @param parent The parent class
	 * @param level The recursive level of the search
	 * @return The distance between child and parent
	 */
	static private int classUtilDistance(final Class<?> child, final Class<?> parent, final int level) {
		if (child.equals(parent)) {
			return level;
		}

		int distance = Integer.MAX_VALUE;

		if (child.getSuperclass() != null) {
			final int superDistance = classUtilDistance(child.getSuperclass(), parent, level + 1);
			if (superDistance < distance) {
				distance = superDistance;
			}
		}
		for (final Class<?> intf : child.getInterfaces()) {
			final int superDistance = classUtilDistance(intf, parent, level + 1);
			if (superDistance < distance) {
				distance = superDistance;
			}
		}
		return distance;
	}

	/**
	 * Return the closest type to the given class that is registered as convertible
	 *
	 * @param cls The class to get its closest convertible type
	 * @return The closest convertible type
	 */
	private @CheckForNull Class<?> getClosestType(final Class<?> cls) {
		int closestDistance = Integer.MAX_VALUE;
		Class<?> closestType = null;
		for (final Map.Entry<Class<?>, InstanceGetter<? extends Converter<?>>> entry : this.converters.entrySet()) {
			if (cls.isAssignableFrom(entry.getKey())) {
				final int distance = classUtilDistance(entry.getKey(), cls, 0);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestType = entry.getKey();
				}
			}
			if (entry.getValue().getTypeLiteral().getRawType().isAnnotationPresent(ConverterCanConstructChild.class) && entry.getKey().isAssignableFrom(cls)) {
				final int distance = classUtilDistance(cls, entry.getKey(), 0);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestType = entry.getKey();
				}
			}
		}
		return closestType;
	}

	/**
	 * Gets the better converter for the given class
	 * If a converter is registered for the given class, returns it
	 * If not, tries to find the converter that can convert to a subclass of this class (and gets the closest).
	 * If none is found, returns null
	 *
	 * @param cls the class to convert to
	 * @return the converter to use or null if none were found
	 */
	@Override
	public @CheckForNull <T> Converter<T> getConverter(final Class<T> cls) {
		// Maybe it has already been set, so we check
		if (this.converters.containsKey(cls)) {
			return (Converter<T>) this.converters.get(cls).getInstance();
		}

		// If it has not been set, we need to set it only once, so we wait for lock
		synchronized (this.converters) {
			// Maybe it has been set while we waited for lock, so we check again
			if (this.converters.containsKey(cls)) {
				return (Converter<T>) this.converters.get(cls).getInstance();
			}

			final Class<?> closestType = getClosestType(cls);

			if (closestType != null) {
				final InstanceGetter<? extends Converter<?>> converter = this.converters.get(closestType);
				this.converters.put(cls, converter);
				return (Converter<T>)converter.getInstance();
			}

			return null;
		}
	}

	/**
	 * Converts an array of string into an array of value
	 * Only non-primitives types are allowed as java does not provide a way to create a primitive array with generics
	 *
	 * @param componentType The class to convert to (Only non-primitives types)
	 * @param from The array of string to convert
	 * @return The array of type converted from the strings
	 * @throws NoConverterException When no converter is found for the specific type (RuntimeException)
	 */
	@Override
	public <T> T[] convertArray(final TypeLiteral<T> componentType, final Object[] from) throws NoConverterException {
		if (componentType.getRawType().isPrimitive()) {
			throw new CannotConvertToPrimitiveException(componentType.getRawType());
		}
		Object[] ret = (Object[])Array.newInstance(componentType.getRawType(), from.length);
		for (int i = 0; i < from.length; ++i) {
			ret[i] = convert(componentType, from[i]);
		}
		return (T[])ret;
	}

	/**
	 * Return the first object of an unknown dimension array
	 *
	 * @param from The array
	 * @return The first object
	 */
	private static Object getFirstValue(Object from) {
		while (from.getClass().isArray()) {
			if (((Object[])from).length > 0) {
				from = ((Object[])from)[0];
			}
			else {
				/* This should never happen in a servlet environment and therefore cannot be tested in one */
				return "";
			}
		}
		return from;
	}

	/**
	 * Converts a string or an array of string into a value or an array of values
	 *
	 * @param toType The type to convert to.
	 *               If 'from' is an array, then primitive types are not allowed
	 * @param from The String or String[] to convert from (only String or String[])
	 * @return The value or array of values
	 * @throws NoConverterException When no converter is found for the specific type (RuntimeException)
	 */
	@Override
	public @CheckForNull <T> T convert(final TypeLiteral<T> toType, Object from) throws NoConverterException {
		if (from.getClass().isArray() && !toType.getRawType().isArray()) {
			from = getFirstValue(from);
		}
		if (from.getClass().equals(String.class)) {
			Converter<T> conv = (Converter<T>) this.getConverter(toType.getRawType());
			if (conv == null && toType.getRawType().isArray()) {
				final Converter<T> compConv = (Converter<T>) this.getConverter(toType.getRawType().getComponentType());
				if (compConv != null) {
					final GivenInstanceGetter<? extends Converter<?>> arrayConverter = new GivenInstanceGetter<>(new ArrayConverter<>(compConv));
					register(toType.getRawType(), arrayConverter);
					conv = (Converter<T>) arrayConverter.getInstance();
				}
			}
			if (conv == null) {
				throw new NoConverterException(toType);
			}
			return conv.get(toType, (String)from);
		}
		if (from.getClass().isArray()) {
			return (T) this.convertArray(TypeLiteral.get(toType.getRawType().getComponentType()), (Object[])from);
		}
		/* This should never happen in a servlet environment and therefore cannot be tested in one */
		throw new NotAStringException(from.getClass());
	}
}
