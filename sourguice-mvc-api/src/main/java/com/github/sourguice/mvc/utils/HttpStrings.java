package com.github.sourguice.mvc.utils;

import com.github.sourguice.utils.Arrays;


/**
 * HTTP related utils
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public final class HttpStrings {

	/**
	 * This is a utility repository and cannot be instanciated
	 */
	private HttpStrings() {}

	/**
	 * Checks if the given accept header matches one of the given values
	 *
	 * @param acceptHeader The accept HTTP header string
	 * @param types An array of acceptable type string to compare to
	 * @return Whether or not the acceptHeader string matches one of the given types
	 */
	public static boolean acceptContains(final String acceptHeader, final String[] types) {
		final String[] accepts = acceptHeader.split(",");

		for (String accept : accepts) {
			if (accept.contains(";")) {
				accept = accept.substring(0, accept.indexOf(';'));
			}

			// TODO: Should evaluate for things like text/* or even */*
			if (Arrays.contains(types, accept.trim())) {
				return true;
			}
		}
		return false;
	}
}