package com.github.sourguice.ws.desc.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import com.github.sourguice.ws.annotation.DisregardParent;
import com.github.sourguice.ws.annotation.DisregardedParent;
import com.github.sourguice.ws.annotation.Exclude;
import com.github.sourguice.ws.annotation.WSSubclasses;
import com.github.sourguice.ws.desc.struct.WSDClass;
import com.github.sourguice.ws.desc.struct.WSDTypeReference;
import com.google.inject.TypeLiteral;

final class _Class {

	private _Class() {}

	private static void putFields(final WSDClass wsdClass, final Class<?> objClass, final DescriptionBuilder root) {
		for (final Field field : objClass.getDeclaredFields()) {
			if (field.getAnnotation(Exclude.class) != null) {
				continue ;
			}

			final String fieldName = _Util.getFieldName(field);

			if (_Constant.isConstant(field)) {
				_Constant.put(wsdClass.getConstants(), field, field.getGenericType(), root);
			}
			else if (!field.isSynthetic() && !Modifier.isTransient(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
				final WSDTypeReference typeRef = _TypeReference.make(field.getGenericType(), field, root);
				if (field.isAnnotationPresent(CheckForNull.class) || field.isAnnotationPresent(Nullable.class)) {
					typeRef.nullable = Boolean.TRUE;
				}
				wsdClass.properties.put(fieldName, typeRef);
			}
		}
	}

	public static void put(final Class<?> objClass, final DescriptionBuilder root) {
		final WSDClass wsdClass = new WSDClass();
		_Util.fillVersioned(wsdClass, objClass);
		root.description.objectTypes.put(objClass.getName(), wsdClass);

		for (final TypeVariable<?> typeVariable : objClass.getTypeParameters()) {
			wsdClass.getTypeVariables().add(_TypeParameter.make(typeVariable, root));
		}

		if (Modifier.isAbstract(objClass.getModifiers())) {
			wsdClass.isAbstract = Boolean.TRUE;
		}

		if (	objClass.getSuperclass() != null
			&&	!objClass.getSuperclass().equals(Object.class)
			&&	objClass.getAnnotation(DisregardParent.class) == null
			&&	objClass.getSuperclass().getAnnotation(DisregardedParent.class) == null
			) {
			final Type superType = TypeLiteral.get(objClass).getSupertype(objClass.getSuperclass()).getType();
			wsdClass.parent = _TypeReference.make(superType, null, root);
		}

		putFields(wsdClass, objClass, root);

		final WSSubclasses subclasses = objClass.getAnnotation(WSSubclasses.class);
		if (subclasses != null) {
			for (final Class<?> subclass : subclasses.value()) {
				_TypeReference.make(subclass, null, root);
			}
		}
	}



}
