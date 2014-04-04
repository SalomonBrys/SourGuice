package com.github.sourguice.ws.desc.struct;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * A constant used by WS
 */
public final class WSDConstant extends Versioned {
	/**
	 * It's type
	 */
	public WSDTypeReference type;

	/**
	 * It's value
	 */
	public Object value;

	public WSDConstant(final WSDTypeReference type, final Object value) {
		super();
		this.type = type;
		this.value = value;
	}

	@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	private WSDConstant() { super(); }
}