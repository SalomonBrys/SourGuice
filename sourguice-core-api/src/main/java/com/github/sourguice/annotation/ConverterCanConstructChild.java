package com.github.sourguice.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.sourguice.conversion.Converter;

/**
 * Must be applied to a {@link Converter} to indicates that it can create child-classes instances.
 *
 * Normally, a converter is only called to construct an instance of the type it was registered for
 * or a super-type instance (which would allow a type instance assign and thereforeit is perfectly fine to construct a type and not a super-type).
 * When set with this annotation, a converter can be used to construct a sub-type of the type it was registered for.
 * In this case, the constructor must create an instance of the given type, not the type it was registered for.
 *
 * @author salomon
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConverterCanConstructChild {
	// Flag annotation
}
