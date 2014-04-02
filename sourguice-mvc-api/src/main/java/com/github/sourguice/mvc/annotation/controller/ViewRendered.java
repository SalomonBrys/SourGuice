package com.github.sourguice.mvc.annotation.controller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container to be able to define multiple {@link ViewRenderedWith} on a controller
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ViewRendered {

	/**
	 * @return all {@link ViewRenderedWith} entries for this controller
	 */
	public ViewRenderedWith[] value();

}
