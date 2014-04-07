package com.github.sourguice.ws.desc.struct;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Parameter of a method
 */
public final class WSDEMParam extends Versioned {

	/**
	 * Position of the parameter
	 */
	public int position;

	/**
	 * Type of the parameter
	 */
	public WSDTypeReference type;

	public WSDEMParam(final int position, final WSDTypeReference type) {
		super();
		this.position = position;
		this.type = type;
	}

	@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	private WSDEMParam() { super(); }
}