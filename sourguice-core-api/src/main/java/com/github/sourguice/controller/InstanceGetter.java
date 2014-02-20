package com.github.sourguice.controller;

/**
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T>
 */
public interface InstanceGetter<T> {
	T getInstance();
	Class<T> getInstanceClass();
}
