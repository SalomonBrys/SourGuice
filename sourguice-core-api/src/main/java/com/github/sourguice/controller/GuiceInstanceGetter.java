package com.github.sourguice.controller;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

import com.google.inject.Injector;

/**
 * Used by the syntax control().with()
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T>
 */
public class GuiceInstanceGetter<T> implements InstanceGetter<T> {

	Class<T> cls;

	@Inject @CheckForNull Injector injector;

	public GuiceInstanceGetter(Class<T> cls) {
		super();
		this.cls = cls;
	}

	@Override
	public T getInstance() {
		assert injector != null;
		return injector.getInstance(cls);
	}

	@Override
	public Class<T> getInstanceClass() {
		return cls;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GuiceInstanceGetter && cls.equals(((GuiceInstanceGetter<?>)obj).cls);
	}

	@Override
	public int hashCode() {
		return cls.hashCode();
	}

}
