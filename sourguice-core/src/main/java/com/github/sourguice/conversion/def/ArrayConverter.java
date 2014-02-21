package com.github.sourguice.conversion.def;

import java.lang.reflect.Array;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;

import com.github.sourguice.conversion.Converter;
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
	private static final Pattern separator = Pattern.compile(", *");

	/**
	 * The original converter that converts a string to a T
	 * as this converter converts a string to a T[]
	 */
	Converter<T> converter;

	/**
	 * @param converter A converter that converts a String into a T
	 */
	public ArrayConverter(Converter<T> converter) {
		this.converter = converter;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public @CheckForNull T[] get(TypeLiteral<? extends T[]> type, String arg) {
		String[] args = separator.split(arg);

		Class<?> componentType = type.getRawType().getComponentType();

		if (componentType.isPrimitive())
			throw new RuntimeException("Array conversion does not support primitive types");

		int length = args.length;
		if (arg.isEmpty())
			length = 0;

		T[] ret = (T[])Array.newInstance(componentType, length);

		for (int i = 0; i < length; ++i) {
			ret[i] = converter.get(TypeLiteral.get((Class<? extends T>)componentType), args[i]);
		}

		return ret;
	}
}
