
package com.github.sourguice.conversion;

import javax.annotation.CheckForNull;

/**
 * Base interface for all converters
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T> The class to convert to.
 *            This is only for type safety as a converter can handle multiple types
 */
public interface Converter<T> {
	/**
	 * Convert the given string to the given type
	 *
	 * @param assign The type of the variable that the construction will be assigned to
	 * @param arg The string to convert
	 * @return The conversion result
	 */
	public @CheckForNull T get(Class<? extends T> assign, String arg);
}
