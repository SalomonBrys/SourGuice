package com.github.sourguice.request;

import javax.annotation.CheckForNull;

public interface Attribute<T> {

	public @CheckForNull T get();
    public void set(T o);

}
