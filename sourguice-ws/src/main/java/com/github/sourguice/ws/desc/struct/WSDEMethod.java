package com.github.sourguice.ws.desc.struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A method in the WS class
 */
public final class WSDEMethod extends Versioned {

	/**
	 * The method's parameters
	 */
	private @CheckForNull Map<String, WSDEMParam> params;

	/**
	 * The type of the return of the method
	 */
	public WSDTypeReference returns;

	/**
	 * List of exceptions declared by this method
	 */
	private @CheckForNull List<WSDTypeReference> exceptions;

	private @CheckForNull List<WSDTypeParameter> typeVariables;

	public WSDEMethod(final WSDTypeReference returns) {
		super();
		this.returns = returns;
	}

	@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	private WSDEMethod() { super(); }

	public Map<String, WSDEMParam> getParams() {
		if (this.params == null) {
			this.params = new HashMap<>();
		}
		return this.params;
	}

	public List<WSDTypeReference> getExceptions() {
		if (this.exceptions == null) {
			this.exceptions = new ArrayList<>();
		}
		return this.exceptions;
	}

	public List<WSDTypeParameter> getTypeVariables() {
		if (this.typeVariables == null) {
			this.typeVariables = new ArrayList<>();
		}
		return this.typeVariables;
	}
}