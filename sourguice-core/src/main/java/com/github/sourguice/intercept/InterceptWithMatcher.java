package com.github.sourguice.intercept;

import java.lang.reflect.AnnotatedElement;

import com.github.sourguice.annotation.InterceptWith;
import com.github.sourguice.utils.Annotations;
import com.google.inject.matcher.AbstractMatcher;

/**
 * Guice matcher that will check for the @{@link InterceptWith} annotation on the element
 * AND on it's java tree and parent
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class InterceptWithMatcher extends AbstractMatcher<AnnotatedElement> {

	@Override
	public boolean matches(final AnnotatedElement element) {
		return Annotations.getOneTreeRecursive(InterceptWith.class, element) != null;
	}

}
