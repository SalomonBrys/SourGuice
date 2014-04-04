package com.github.sourguice.ws.desc.struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

/**
 * A class that will be handled (in or out) by the web services.
 */
public final class WSDClass extends Versioned {

	/**
	 * It's super class
	 */
	public @CheckForNull WSDTypeReference parent = null;

	/**
	 * Whether or not this in an abstract class
	 */
	public @CheckForNull Boolean isAbstract;

	/**
	 * List of properties
	 */
	public Map<String, WSDTypeReference> properties = new HashMap<>();

	private @CheckForNull List<WSDTypeParameter> typeVariables;

	/**
	 * List of constants defined within it's scope.
	 */
	private @CheckForNull Map<String, WSDConstant> constants;

	public Map<String, WSDConstant> getConstants() {
		if (this.constants == null) {
			this.constants = new HashMap<>();
		}
		return this.constants;
	}

	public List<WSDTypeParameter> getTypeVariables() {
		if (this.typeVariables == null) {
			this.typeVariables = new ArrayList<>();
		}
		return this.typeVariables;
	}
}
