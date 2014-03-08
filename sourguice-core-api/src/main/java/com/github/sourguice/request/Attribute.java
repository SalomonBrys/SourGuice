package com.github.sourguice.request;

import javax.annotation.CheckForNull;

/**
 * This is injectable for any parameter annotated with @SessionAttribute or @RequestAttribute.
 * The system will injected the attribute encapsulated in an Attribute object.
 *
 * Using this is more semantic than to inject the session to read and write an attribute.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T> The type of the attribute to inject
 */
public interface Attribute<T> {

	/**
	 * @return The attribute
	 */
	public @CheckForNull T get();

	/**
	 * @param value The new value of the attribute
	 */
    public void set(T value);

}
