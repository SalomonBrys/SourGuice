package com.github.sourguice.ws.desc.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;

import com.github.sourguice.ws.annotation.WSConstant;
import com.github.sourguice.ws.desc.struct.WSDConstant;

final class _Constant {

	private _Constant() {}

	protected static boolean isConstant(final Field field) {
		return	Modifier.isStatic(field.getModifiers())
			&&	Modifier.isFinal(field.getModifiers())
			&&	field.getAnnotation(WSConstant.class) != null;
	}

	protected static void put(final Map<String, WSDConstant> constants, final Field field, final Type type, final DescriptionBuilder root) {
		Object value = null;
		field.setAccessible(true);
		try {
			value = field.get(null);
		}
		catch (IllegalAccessException e) {
			throw new UnsupportedOperationException(e);
		}
		finally {
			field.setAccessible(false);
		}

		final WSDConstant wsdConstant = new WSDConstant(_TypeReference.make(type, null, root), value);
		_Util.fillVersioned(wsdConstant, field);

		constants.put(_Util.getFieldName(field), wsdConstant);
	}

}
