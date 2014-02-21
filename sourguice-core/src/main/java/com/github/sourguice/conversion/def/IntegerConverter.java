/**
 * Defaults converters
 */
package com.github.sourguice.conversion.def;

import javax.annotation.CheckForNull;

import com.github.sourguice.conversion.Converter;
import com.google.inject.TypeLiteral;

/**
 * Converts a String to Integer and int
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class IntegerConverter implements Converter<Integer> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @CheckForNull Integer get(TypeLiteral<? extends Integer> type, String arg) {
		try {
			return Integer.valueOf(arg);
		}
		catch (NumberFormatException e) {
			if (type.getRawType().isPrimitive())
				return new Integer(0);
			return null;
		}
	}
}
