package com.github.sourguice.conversion.def;

import javax.annotation.CheckForNull;

import com.github.sourguice.conversion.Converter;
import com.google.inject.TypeLiteral;

/**
 * String converter that does nothing and simply returns the string
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class StringConverter implements Converter<String> {

	@Override @CheckForNull public String get(final TypeLiteral<? extends String> type, final String arg) {
		return arg;
	}
}
