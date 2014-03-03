package com.github.sourguice.conversion.def;

import javax.annotation.CheckForNull;

import com.github.sourguice.conversion.Converter;
import com.google.inject.TypeLiteral;

/**
 * Converts a String to Short and short
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class ShortConverter implements Converter<Short> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public @CheckForNull Short get(final TypeLiteral<? extends Short> type, final String arg) {
		try {
			return Short.valueOf(arg);
		}
		catch (NumberFormatException e) {
			if (type.getRawType().isPrimitive()) {
				return Short.valueOf((short)0);
			}
			return null;
		}
	}
}
