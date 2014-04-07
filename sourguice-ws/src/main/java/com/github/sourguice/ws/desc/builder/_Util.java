package com.github.sourguice.ws.desc.builder;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

import javax.annotation.CheckForNull;

import com.github.sourguice.ws.annotation.WSDoc;
import com.github.sourguice.ws.desc.struct.Versioned;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import com.google.inject.TypeLiteral;

final class _Util {

	private _Util() {}

	protected static String getFieldName(final Field field) {
		String fieldName = field.getName();
		final SerializedName serializedName = field.getAnnotation(SerializedName.class);
		if (serializedName != null) {
			fieldName = serializedName.value();
		}
		return fieldName;
	}

	protected static <T extends Versioned> T fillVersioned(final T versioned, final @CheckForNull AnnotatedElement annos) {
		if (annos == null) {
			return versioned;
		}

		final Since elSince = annos.getAnnotation(Since.class);
		if (elSince != null) {
			versioned.since = Double.valueOf(elSince.value());
		}
		final Until elUntil = annos.getAnnotation(Until.class);
		if (elUntil != null) {
			versioned.until = Double.valueOf(elUntil.value());
		}
		final WSDoc elDoc = annos.getAnnotation(WSDoc.class);
		if (elDoc != null) {
			versioned.doc = elDoc.value();
		}
		return versioned;
	}

	protected static @CheckForNull TypeLiteral<?> getSuperType(final TypeLiteral<?> type) {
		final Class<?> parent = type.getRawType().getSuperclass();
		if (parent != null && !parent.equals(Object.class)) {
			return type.getSupertype(parent);
		}
		return null;
	}

}
