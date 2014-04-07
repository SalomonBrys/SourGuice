package com.github.sourguice.ws.desc.builder;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.annotation.CheckForNull;

import com.github.sourguice.ws.desc.struct.WSDType;
import com.github.sourguice.ws.desc.struct.WSDTypeReference;
import com.google.inject.TypeLiteral;

final class _TypeReference {

	private _TypeReference() {}

	@SuppressWarnings({ "PMD.AvoidUsingShortType", "PMD.CyclomaticComplexity" })
	private static @CheckForNull WSDTypeReference makeTypeReferenceForSimpleClass(final Class<?> raw) {
		if (raw.equals(void.class) || raw.equals(Void.class)) {
			return new WSDTypeReference(WSDType.VOID);
		}
		else if (raw.equals(boolean.class) || Boolean.class.isAssignableFrom(raw)) {
			return new WSDTypeReference(WSDType.BOOL);
		}
		else if (raw.equals(byte.class) || Byte.class.isAssignableFrom(raw)) {
			return new WSDTypeReference(WSDType.BYTE);
		}
		else if (raw.equals(char.class) || Character.class.isAssignableFrom(raw)) {
			return new WSDTypeReference(WSDType.CHAR);
		}
		else if (raw.equals(short.class) || Short.class.isAssignableFrom(raw)) {
			return new WSDTypeReference(WSDType.SHORT);
		}
		else if (raw.equals(int.class) || Integer.class.isAssignableFrom(raw)) {
			return new WSDTypeReference(WSDType.INT);
		}
		else if (raw.equals(float.class) || Float.class.isAssignableFrom(raw)) {
			return new WSDTypeReference(WSDType.FLOAT);
		}
		else if (raw.equals(double.class) || Double.class.isAssignableFrom(raw)) {
			return new WSDTypeReference(WSDType.DOUBLE);
		}
		else if (String.class.isAssignableFrom(raw)) {
			return new WSDTypeReference(WSDType.STRING);
		}
		else if (Date.class.isAssignableFrom(raw)) {
			return new WSDTypeReference(WSDType.DATE);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static @CheckForNull WSDTypeReference makeTypeReferenceForTransformedClass(final TypeLiteral<?> type, final DescriptionBuilder root) {
		if (type.getRawType().isEnum()) {
			final String typeName = type.getRawType().getName();

			final WSDTypeReference reference = new WSDTypeReference(WSDType.ENUM);
			reference.ref = typeName;

			if (!root.description.enumTypes.containsKey(typeName)) {
				root.addToPendingEnums((Class<Enum<?>>) type.getRawType());
			}
			return reference;
		}

		else if (type.getRawType().isArray()) {
			final WSDTypeReference reference = new WSDTypeReference(WSDType.COLLECTION);
			final Class<?> compType = type.getRawType().getComponentType();
			reference.getParameterTypes().put("E", make(compType, null, root));
			return reference;
		}

		else if (Collection.class.isAssignableFrom(type.getRawType())) {
			final WSDTypeReference reference = new WSDTypeReference(WSDType.COLLECTION);
			final ParameterizedType colType = (ParameterizedType) type.getSupertype(Collection.class).getType();
			reference.getParameterTypes().put("E", make(colType.getActualTypeArguments()[0], null, root));
			return reference;
		}

		else if (Map.class.isAssignableFrom(type.getRawType())) {
			final WSDTypeReference reference = new WSDTypeReference(WSDType.MAP);

			final ParameterizedType mapType = (ParameterizedType) type.getSupertype(Map.class).getType();
			final Type keyType = mapType.getActualTypeArguments()[0];
			final Type valType = mapType.getActualTypeArguments()[1];

			reference.getParameterTypes().put("K", make(keyType, null, root));
			reference.getParameterTypes().put("V", make(valType, null, root));
			return reference;
		}
		return null;
	}

	private static WSDTypeReference makeTypeReferenceForClass(final Type type, final DescriptionBuilder root) {
		final TypeLiteral<?> typeLitteral = TypeLiteral.get(type);

		WSDTypeReference reference = makeTypeReferenceForSimpleClass(typeLitteral.getRawType());
		if (reference == null) {
			reference = makeTypeReferenceForTransformedClass(typeLitteral, root);
		}
		if (reference == null) {
			reference = new WSDTypeReference(WSDType.OBJECT);
			final String typeName = typeLitteral.getRawType().getName();
			if (!root.description.objectTypes.containsKey(typeName)) {
				root.addToPendingTypes(typeLitteral.getRawType());
			}
			reference.ref = typeName;
			if (type instanceof ParameterizedType) {
				final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
				final TypeVariable<?>[] typeParameters = typeLitteral.getRawType().getTypeParameters();
				for (int i = 0; i < typeParameters.length; ++i) {
					reference.getParameterTypes().put(typeParameters[i].getName(), make(typeArguments[i], null, root));
				}
			}
		}
		return reference;
	}

	@SuppressWarnings({ "PMD.AvoidUsingShortType" })
	protected static WSDTypeReference make(final Type type, final @CheckForNull AnnotatedElement annos, final DescriptionBuilder root) {

		WSDTypeReference reference = null;

		if (type instanceof TypeVariable) {
			reference = new WSDTypeReference(WSDType.TYPE_VARIABLE);
			reference.ref = ((TypeVariable<?>)type).getName();
		}

		else if (type instanceof WildcardType) {
			final WildcardType wildcard = (WildcardType)type;
			reference = new WSDTypeReference(WSDType.WILDCARD_TYPE);
			if (!wildcard.getUpperBounds()[0].equals(Object.class)) {
				reference.upperBound = make(wildcard.getUpperBounds()[0], null, root);
			}
			if (wildcard.getLowerBounds().length > 0) {
				reference.lowerBound = make(wildcard.getLowerBounds()[0], null, root);
			}
		}
		else if (type instanceof Class || type instanceof ParameterizedType || type instanceof GenericArrayType) {
			reference = makeTypeReferenceForClass(type, root);
		}
		else {
			throw new UnsupportedOperationException("Unknown type " + type.toString());
		}

		return _Util.fillVersioned(reference, annos);
	}

}
