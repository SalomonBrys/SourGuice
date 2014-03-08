package com.github.sourguice.controller;

import java.lang.annotation.Annotation;

import com.google.inject.TypeLiteral;

/**
 * This is a standard object that will give an instance of T when asked.
 * It has the same role as a {@link javax.inject.Provider} or a {@link com.google.inject.Provider}
 * but with the ability to get a {@link TypeLiteral} too
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T> The type of the instance this will get
 */
public interface InstanceGetter<T> {

	/**
	 * @return an instance of type T
	 */
	T getInstance();

	/**
	 * @return {@link Annotation} exact representation of the type of the instance
	 */
	TypeLiteral<T> getTypeLiteral();
}
