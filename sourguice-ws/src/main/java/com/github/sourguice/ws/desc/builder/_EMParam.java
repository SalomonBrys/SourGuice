package com.github.sourguice.ws.desc.builder;

import java.lang.reflect.AnnotatedElement;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import com.github.sourguice.ws.annotation.WSParam;
import com.github.sourguice.ws.desc.struct.WSDEMParam;
import com.github.sourguice.ws.desc.struct.WSDEMethod;
import com.google.inject.TypeLiteral;

final class _EMParam {

	private _EMParam() {}

	protected static void put(final WSDEMethod wsdeMethod, final WSParam wsParam, final int position, final TypeLiteral<?> type, final AnnotatedElement annos, final DescriptionBuilder root) {

		final WSDEMParam wsdemParam = new WSDEMParam(position, _TypeReference.make(type.getType(), annos, root));

		_Util.fillVersioned(wsdemParam, annos);

		if (annos.isAnnotationPresent(CheckForNull.class) || annos.isAnnotationPresent(Nullable.class)) {
			wsdemParam.type.nullable = Boolean.TRUE;
		}

		wsdeMethod.getParams().put(wsParam.value(), wsdemParam);
	}


}
