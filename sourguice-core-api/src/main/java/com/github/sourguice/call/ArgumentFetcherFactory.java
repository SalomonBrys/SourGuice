package com.github.sourguice.call;

import java.lang.reflect.Method;

import javax.annotation.CheckForNull;

import com.google.inject.TypeLiteral;

public interface ArgumentFetcherFactory {

	public @CheckForNull ArgumentFetcher<?> create(Method method, int position, TypeLiteral<?> argType);

}
