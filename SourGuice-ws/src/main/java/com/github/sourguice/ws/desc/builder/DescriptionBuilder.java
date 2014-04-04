package com.github.sourguice.ws.desc.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.sourguice.provider.TypedProvider;
import com.github.sourguice.ws.desc.struct.WSDescription;

public class DescriptionBuilder {

	protected final WSDescription description;

	private final Set<Class<? extends Enum<?>>> pendingEnums = new HashSet<>();
	private final Set<Class<?>> pendingTypes = new HashSet<>();


	public DescriptionBuilder(final WSDescription description) {
		this.description = description;
	}

	protected void addToPendingTypes(final Class<?> cls) {
		if (	!cls.equals(Object.class)
			&&	!cls.equals(Throwable.class)
			&&	!cls.equals(Error.class)
			&&	!cls.equals(Exception.class)
			&&	!cls.equals(RuntimeException.class)
			&&	!cls.equals(Serializable.class)
			) {
			this.pendingTypes.add(cls);
		}
	}

	protected void addToPendingEnums(final Class<? extends Enum<?>> cls) {
		this.pendingEnums.add(cls);
	}

	public void build(final Collection<TypedProvider<?>> controllers) {
		for (final TypedProvider<?> type : controllers) {
			_Endpoint.put(type.getTypeLiteral(), this);
		}

		boolean hasPending = true;
		while (hasPending) {
			hasPending = false;
			for (final Class<? extends Enum<?>> enumClass : new ArrayList<>(this.pendingEnums)) {
				hasPending = true;
				_Enum.put(enumClass, this);
				this.pendingEnums.remove(enumClass);
			}
			for (final Class<?> objectClass : new ArrayList<>(this.pendingTypes)) {
				hasPending = true;
				_Class.put(objectClass, this);
				this.pendingTypes.remove(objectClass);
			}
		}
	}

}
