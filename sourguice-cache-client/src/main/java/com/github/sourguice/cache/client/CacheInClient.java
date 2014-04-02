package com.github.sourguice.cache.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.sourguice.value.ValueConstants;

/**
 * Annotation that annotate any method whose response is to be cached
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CacheInClient {

	/**
	 * @return If true, set the public flag
	 */
	public boolean Public() default true;

	/**
	 * @return If empty, set the private flag. If non-empty, set the private field
	 */
	public String Private() default ValueConstants.DEFAULT_NONE;

	/**
	 * @return If empty, set the no-cache flag. If non-empty, set the no-cache field
	 */
	public String NoCache() default ValueConstants.DEFAULT_NONE;

	/**
	 * @return If true, set the no-store flag
	 */
	public boolean NoStore() default false;

	/**
	 * @return If true, set the no-transform flag
	 */
	public boolean NoTransform() default false;

	/**
	 * @return if true, set the must-revalidate flag
	 */
	public boolean MustRevalidate() default false;

	/**
	 * @return The max-age value (number of seconds until expiration)
	 */
	public int MaxAge() default -1;

	/**
	 * @return The s-max-age value (number of seconds the proxy is allowed to serve stale content)
	 */
	public int SMaxAge() default -1;

	/**
	 * @return Any extension to the Cache-Control calue
	 */
	public String Extension() default "";
}