package com.github.sourguice.mvc.annotation.controller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.sourguice.mvc.view.ViewRenderer;
import com.github.sourguice.mvc.view.ViewRendererService;

/**
* Annotation to a controller to indicate when a {@link ViewRenderer} should be used.
* Controllers can use this to override the {@link ViewRendererService} configuration.
*
* @author Salomon BRYS <salomon.brys@gmail.com>
*/
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ViewRenderedWith {
	/**
	 * @return On which views the renderer should be used.
	 *         To render for all views, use ".*"
	 */
	public String regex();

	/**
	 * @return The {@link ViewRenderer} to use for this pattern.
	 */
	public Class<? extends ViewRenderer> renderer();
}
