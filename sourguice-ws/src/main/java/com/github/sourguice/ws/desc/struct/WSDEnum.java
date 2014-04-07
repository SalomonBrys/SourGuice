package com.github.sourguice.ws.desc.struct;

import java.util.LinkedList;
import java.util.List;

/**
 * An enum that will be handled (in or out) by the web services.
 */
public final class WSDEnum extends Versioned {

	/**
	 * List of enum values
	 */
	public List<String> values = new LinkedList<>();
}