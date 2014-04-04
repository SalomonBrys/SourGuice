package com.github.sourguice.ws.desc.struct;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import com.google.gson.annotations.SerializedName;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A reference to a type
 */
public final class WSDTypeReference extends Versioned {

	/**
	 * The type (class) of the reference
	 */
	public WSDType type;

	/**
	 * The name of the reference
	 */
	public @CheckForNull String ref;

	/**
	 * The generic type parameters (if any)
	 */
	private @CheckForNull Map<String, WSDTypeReference> parameterTypes;

	/**
	 * Whether or not this can be transmitted null (only when reference is a parameter)
	 */
	public @CheckForNull Boolean nullable;

	@SerializedName("extends")
	public @CheckForNull WSDTypeReference upperBound;

	@SerializedName("super")
	public @CheckForNull WSDTypeReference lowerBound;

	public WSDTypeReference(final WSDType type) {
		super();
		this.type = type;
	}

	@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	private WSDTypeReference() { super(); }

	public Map<String, WSDTypeReference> getParameterTypes() {
		if (this.parameterTypes == null) {
			this.parameterTypes = new HashMap<>();
		}
		return this.parameterTypes;
	}
}
