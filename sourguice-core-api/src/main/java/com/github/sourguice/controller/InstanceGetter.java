package com.github.sourguice.controller;

import com.google.inject.TypeLiteral;

/**
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T>
 */
public interface InstanceGetter<T> {
	T getInstance();
	TypeLiteral<T> getTypeLiteral();
}
