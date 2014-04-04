package com.github.sourguice.ws.desc.builder;

import com.github.sourguice.ws.desc.struct.WSDEnum;

final class _Enum {

	private _Enum() {}

	protected static void put(final Class<? extends Enum<?>> cls, final DescriptionBuilder root) {
		final WSDEnum wsdEnum = new WSDEnum();

		_Util.fillVersioned(wsdEnum, cls);

		for (final Object cst : cls.getEnumConstants()) {
			wsdEnum.values.add(cst.toString());
		}

		root.description.enumTypes.put(cls.getName(), wsdEnum);
	}


}
