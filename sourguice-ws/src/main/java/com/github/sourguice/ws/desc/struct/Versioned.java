package com.github.sourguice.ws.desc.struct;

import javax.annotation.CheckForNull;

/**
 * Base class for most elements in this description.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class Versioned {

	/**
	 * This states that the element is valid since a particular version
	 */
	public @CheckForNull Double since;

	/**
	 * This states that the element is valid until a particular version (including)
	 */
	public @CheckForNull Double until;

	/**
	 * The documentation of the element.
	 * Used by code generators to generate doc for methods / types / properties / arguments / etc.
	 */
	public @CheckForNull String[] doc;
}