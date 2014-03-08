package com.github.sourguice.conversion.def;

import java.lang.reflect.Array;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;

import com.github.sourguice.conversion.Converter;
import com.github.sourguice.throwable.service.converter.CannotConvertToPrimitiveException;
import com.google.inject.TypeLiteral;
/**
 * Converts a coma separated array of string into an array of value of type T
 *
 * @param <T> The type to convert strings to
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class ArrayConverter<T> implements Converter<T[]> {

	/**
	 * The separator pattern that will separate the different values
	 * For example, it will recognise 21,42 as two different values separated by ','
	 */
	private static final Pattern SEPARATOR = Pattern.compile(", *");

	/**
	 * The original converter that converts a string to a T
	 * as this converter converts a string to a T[]
	 */
	private final Converter<T> converter;

	/**
	 * @param converter A converter that converts a String into a T
	 */
	public ArrayConverter(final Converter<T> converter) {
		this.converter = converter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public @CheckForNull T[] get(final TypeLiteral<? extends T[]> type, final String arg) {
		final Class<?> componentType = type.getRawType().getComponentType();

		if (componentType.isPrimitive()) {
			throw new CannotConvertToPrimitiveException(componentType);
		}

		final String[] args = SEPARATOR.split(arg);

		int length = args.length;
		if (arg.isEmpty()) {
			length = 0;
		}

		T[] ret = (T[])Array.newInstance(componentType, length);

		for (int i = 0; i < length; ++i) {
			ret[i] = this.converter.get(TypeLiteral.get((Class<? extends T>)componentType), args[i]);
		}

		return ret;
	}
}
