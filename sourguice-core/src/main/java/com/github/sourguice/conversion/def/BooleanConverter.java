package com.github.sourguice.conversion.def;

import java.util.regex.Pattern;

import javax.annotation.CheckForNull;

import com.github.sourguice.conversion.Converter;
import com.google.inject.TypeLiteral;

/**
 * Converts a String to Boolean and boolean
 * Return true when equals "true", "Y" or any non-zero number
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class BooleanConverter implements Converter<Boolean> {

	/**
	 * Pattern of a floating point number
	 */
	static private final Pattern NUMBER = Pattern.compile("[0-9]*\\.?[0-9]+");

	/**
	 * Pattern of a zero
	 */
	static private final Pattern ZERO = Pattern.compile("0?\\.?0+");

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @CheckForNull Boolean get(final TypeLiteral<? extends Boolean> clazz, final String arg) {
		if (	arg.equalsIgnoreCase("true")
			||	arg.equalsIgnoreCase("on")
			||	arg.equalsIgnoreCase("Y")
			||	arg.equalsIgnoreCase("yes")
			||	NUMBER.matcher(arg).matches() && !ZERO.matcher(arg).matches()) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
}
