package com.github.sourguice.conversion.def;

import javax.annotation.CheckForNull;

import com.github.sourguice.conversion.Converter;
import com.google.inject.TypeLiteral;

/**
 * Converts a String to Float and float
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class FloatConverter implements Converter<Float> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @CheckForNull Float get(TypeLiteral<? extends Float> type, String arg) {
		try {
			return Float.valueOf(arg);
		}
		catch (NumberFormatException e) {
			if (type.getRawType().isPrimitive())
				return Float.valueOf(0);
			return null;
		}
	}
}
