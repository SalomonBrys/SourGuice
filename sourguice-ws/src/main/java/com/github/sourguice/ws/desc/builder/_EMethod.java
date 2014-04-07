package com.github.sourguice.ws.desc.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.List;

import com.github.sourguice.utils.Annotations;
import com.github.sourguice.ws.annotation.WSException;
import com.github.sourguice.ws.annotation.WSMethod;
import com.github.sourguice.ws.annotation.WSParam;
import com.github.sourguice.ws.desc.struct.WSDEMethod;
import com.github.sourguice.ws.desc.struct.WSDEndpoint;
import com.google.inject.TypeLiteral;

final class _EMethod {

	private _EMethod() {}

	protected static void put(final WSDEndpoint wsdEndpoint, final TypeLiteral<?> type, final Method method, final WSMethod anno, final DescriptionBuilder root) {

		final WSDEMethod wsdeMethod = new WSDEMethod(_TypeReference.make(type.getReturnType(method).getType(), null, root));

		_Util.fillVersioned(wsdeMethod, method);

		String methodName = anno.name();
		if (methodName.isEmpty()) {
			methodName = method.getName();
		}
		wsdEndpoint.methods.put(methodName, wsdeMethod);

		final Annotation[][] paramAnnos = method.getParameterAnnotations();
		final List<TypeLiteral<?>> paramTypes = type.getParameterTypes(method);
		for (int i = 0; i < paramTypes.size(); ++i) {
			final WSParam wsParam = Annotations.getOneRecursive(WSParam.class, paramAnnos[i]);
			if (wsParam != null) {
				_EMParam.put(wsdeMethod, wsParam, i, paramTypes.get(i), Annotations.fromArray(paramAnnos[i]), root);
			}
		}

		final TypeVariable<Method>[] methodTypeParameters = method.getTypeParameters();
		for (final TypeVariable<Method> typeVariable : methodTypeParameters) {
			wsdeMethod.getTypeVariables().add(_TypeParameter.make(typeVariable, root));
		}

		for (final TypeLiteral<?> exceptionType : type.getExceptionTypes(method)) {
			if (Annotations.getOneTreeRecursive(WSException.class, exceptionType.getRawType()) != null) {
				wsdeMethod.getExceptions().add(_TypeReference.make(exceptionType.getType(), null, root));
			}
		}
	}

}
