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
	public @CheckForNull Short get(TypeLiteral<? extends Short> type, String arg) {
		try {
			return Short.valueOf(arg);
		}
		catch (NumberFormatException e) {
			if (type.getRawType().isPrimitive())
				return new Short((short)0);
			return null;
		}
	}
}
