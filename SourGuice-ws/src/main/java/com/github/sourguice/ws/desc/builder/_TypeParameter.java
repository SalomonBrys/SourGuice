package com.github.sourguice.ws.desc.builder;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import com.github.sourguice.ws.desc.struct.WSDTypeParameter;

final class _TypeParameter {

	private _TypeParameter() {}

	protected static WSDTypeParameter make(final TypeVariable<?> typeVariable, final DescriptionBuilder root) {
		final WSDTypeParameter wsdTypeVariable = new WSDTypeParameter(typeVariable.getName());
		for (final Type bound : typeVariable.getBounds()) {
			if (!bound.equals(Object.class)) {
				wsdTypeVariable.getBounds().add(_TypeReference.make(bound, null, root));
			}
		}
		return wsdTypeVariable;
	}

}
