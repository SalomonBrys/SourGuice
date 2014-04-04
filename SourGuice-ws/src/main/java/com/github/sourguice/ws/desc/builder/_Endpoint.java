package com.github.sourguice.ws.desc.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import com.github.sourguice.utils.Annotations;
import com.github.sourguice.ws.annotation.WSClass;
import com.github.sourguice.ws.annotation.WSMethod;
import com.github.sourguice.ws.desc.struct.WSDEndpoint;
import com.google.inject.TypeLiteral;

final class _Endpoint {

	private _Endpoint() {}

	private static void putEndpointConstantsAndAddClasses(final WSDEndpoint wsdEndpoint, TypeLiteral<?> type, final DescriptionBuilder root) {
		while (type != null) {
			final WSClass lookInfos = type.getRawType().getAnnotation(WSClass.class);
			if (lookInfos != null) {
				for (final Class<?> addCls : lookInfos.addKnownClasses()) {
					if (root.description.objectTypes.containsKey(addCls.getName())) {
						root.addToPendingTypes(addCls);
					}
				}
			}

			for (final Field field : type.getRawType().getDeclaredFields()) {
				if (_Constant.isConstant(field)) {
					_Constant.put(wsdEndpoint.getConstants(), field, type.getFieldType(field).getType(), root);
				}
			}

			type = _Util.getSuperType(type);
		}
	}

	protected static void put(final TypeLiteral<?> type, final DescriptionBuilder root) {
		final WSDEndpoint wsdEndpoint = new WSDEndpoint();

		_Util.fillVersioned(wsdEndpoint, type.getRawType());

		String name = type.getRawType().getSimpleName();
		final WSClass infos = type.getRawType().getAnnotation(WSClass.class);
		if (infos != null && !infos.name().isEmpty()) {
			if (!Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", infos.name())) {
				throw new AssertionError("WebServices name must be a regular identifier");
			}
			name = infos.name();
		}

		root.description.endpoints.put(name, wsdEndpoint);

		putEndpointConstantsAndAddClasses(wsdEndpoint, type, root);

		for (final Method method : type.getRawType().getMethods()) {
			final WSMethod anno = Annotations.getOneRecursive(WSMethod.class, method.getAnnotations());
			if (anno != null) {
				_EMethod.put(wsdEndpoint, type, method, anno, root);
			}
		}
	}

}
