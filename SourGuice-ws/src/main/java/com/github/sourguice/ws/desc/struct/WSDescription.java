package com.github.sourguice.ws.desc.struct;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents the architecture of a web services class,
 * it describes each type, enum and methods.
 * It is used to be translated into JSON to enable code generation.
 * This is basically the WSDL of SOAP, but for SourGuice JSON based WS
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@SuppressWarnings("PMD.FinalFieldCouldBeStatic")
public final class WSDescription {

	/**
	 * The default version declared by the WS
	 */
	public final double _SourWSVersion = 1.0;

	/**
	 * The default version declared by the WS
	 */
	public double defaultVersion = 1.0;

	/**
	 * List of type (Java classes) that are handled by these WS
	 */
	public Map<String, WSDClass> objectTypes = new HashMap<>();

	/**
	 * List of enums that are handled by these WS
	 */
	public Map<String, WSDEnum> enumTypes = new HashMap<>();

	/**
	 * List of methods that are callable from these WS
	 */
	public Map<String, WSDEndpoint> endpoints = new HashMap<>();
}
