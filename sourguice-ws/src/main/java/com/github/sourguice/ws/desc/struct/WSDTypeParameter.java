package com.github.sourguice.ws.desc.struct;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;

import com.google.gson.annotations.SerializedName;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A method in the WS class
 */
public final class WSDTypeParameter {

	public String name;

	@SerializedName("extends")
	private @CheckForNull List<WSDTypeReference> bounds = null;

	public WSDTypeParameter(final String name) {
		super();
		this.name = name;
	}

	@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	private WSDTypeParameter() {}

	public List<WSDTypeReference> getBounds() {
		if (this.bounds == null) {
			this.bounds = new LinkedList<>();
		}
		return this.bounds;
	}
}