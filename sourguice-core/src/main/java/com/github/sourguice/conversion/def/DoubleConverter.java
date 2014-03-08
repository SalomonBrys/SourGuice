package com.github.sourguice.conversion.def;

import javax.annotation.CheckForNull;

import com.github.sourguice.conversion.Converter;
import com.google.inject.TypeLiteral;

/**
 * Converts a String to Double and double
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class DoubleConverter implements Converter<Double> {

	@Override
	public @CheckForNull Double get(final TypeLiteral<? extends Double> type, final String arg) {
		try {
			return Double.valueOf(arg);
		}
		catch (NumberFormatException e) {
			if (type.getRawType().isPrimitive()) {
				return Double.valueOf(0);
			}
			return null;
		}
	}
}
