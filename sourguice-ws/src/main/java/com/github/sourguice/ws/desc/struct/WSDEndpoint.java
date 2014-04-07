package com.github.sourguice.ws.desc.struct;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;


public final class WSDEndpoint extends Versioned {

	/**
	 * List of methods that are callable in this endpoint
	 */
	public Map<String, WSDEMethod> methods = new HashMap<>();

	/**
	 * List of Constants that are helpful for these WS
	 */
	private @CheckForNull Map<String, WSDConstant> constants;

	public Map<String, WSDConstant> getConstants() {
		if (this.constants == null) {
			this.constants = new HashMap<>();
		}
		return this.constants;
	}

}