package com.github.sourguice.view;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import com.google.inject.servlet.RequestScoped;

/**
 * Key / Values passed to a view
 * This basically encapsulates a map
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@RequestScoped
public final class Model {

	/**
	 * The key / value pairs
	 */
	private final Map<String, Object> map = new HashMap<>();

	/**
	 * Adds an attribute and its value to the model
	 *
	 * @param attributeName The name of the attribute to add
	 * @param attributeValue The value of the attribute to add
	 * @return itself to permit command chain
	 */
	public Model addAttribute(final String attributeName, final @CheckForNull Object attributeValue) {
		this.map.put(attributeName, attributeValue);
		return this;
	}

	/**
	 * Alias to {@link Model#addAttribute(String, Object)}
	 *
	 * @param name The name of the attribute to add
	 * @param value The value of the attribute to add
	 * @return itself to permit command chain
	 */
	public Model put(final String name, final @CheckForNull Object value) {
		return this.addAttribute(name, value);
	}

	/**
	 * Adds all attributes of a given map to the model, erasing existing attributes with new ones
	 *
	 * @param attributes The attributes to add
	 * @return itself to permit command chain
	 */
	public Model addAllAttributes(final Map<String, ?> attributes) {
		this.map.putAll(attributes);
		return this;
	}

	/**
	 * Adds all attributes of a given map to the model, keeping old ones in case of duplicates
	 *
	 * @param attributes The attributes to merge
	 * @return itself to permit command chain
	 */
	public Model mergeAttributes(final Map<String, ?> attributes) {
		for (final String key : attributes.keySet()) {
			if (!this.map.containsKey(key)) {
				this.map.put(key, attributes.get(key));
			}
		}
		return this;
	}

	/**
	 * @return The key / value pairs of this model as a map
	 */
	public Map<String, Object> asMap() {
		return this.map;
	}
}
