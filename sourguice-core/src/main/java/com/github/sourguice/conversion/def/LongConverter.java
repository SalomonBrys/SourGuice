package com.github.sourguice.conversion.def;

import javax.annotation.CheckForNull;

import com.github.sourguice.conversion.Converter;
import com.google.inject.TypeLiteral;

/**
 * Converts a String to Long and long
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class LongConverter implements Converter<Long> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @CheckForNull Long get(final TypeLiteral<? extends Long> type, final String arg) {
		try {
			return Long.valueOf(arg);
		}
		catch (NumberFormatException e) {
			if (type.getRawType().isPrimitive()) {
				return Long.valueOf(0);
			}
			return null;
		}
	}
}
