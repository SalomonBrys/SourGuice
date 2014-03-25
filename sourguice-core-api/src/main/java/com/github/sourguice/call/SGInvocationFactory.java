package com.github.sourguice.call;

import java.lang.reflect.Method;

import com.github.sourguice.annotation.controller.Callable;
import com.google.inject.TypeLiteral;

public interface SGInvocationFactory {

	public abstract SGInvocation newInvocation(TypeLiteral<?> controllerType, Method method, ArgumentFetcherFactory... factories);

}
