package com.github.sourguice.cache.server.def;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation that indicates that this method's response should be cached
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CacheInMemory {
	/**
	 * @return Duration of the cache
	 */
	int seconds();

	/**
	 * @return List of headers that are part of this cache's entry
	 */
	String[] headers() default {};
}