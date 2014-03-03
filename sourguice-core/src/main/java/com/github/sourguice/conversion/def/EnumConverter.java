package com.github.sourguice.conversion.def;

import javax.annotation.CheckForNull;

import com.github.sourguice.annotation.ConverterCanConstructChild;
import com.github.sourguice.conversion.Converter;
import com.google.inject.TypeLiteral;

/**
 * Converts a String to an Enum value
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@SuppressWarnings("rawtypes")
@ConverterCanConstructChild
public class EnumConverter implements Converter<Enum> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "unchecked", "cast" })
	public @CheckForNull Enum get(final TypeLiteral<? extends Enum> type, final String arg) {
		try {
			return (Enum) Enum.valueOf((Class<? extends Enum>) type.getRawType(), arg);
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}
}
