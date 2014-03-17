package com.github.sourguice.annotation.request;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.sourguice.cache.Cache;
import com.github.sourguice.cache.CacheService;

/**
 * Flag annotation that indicates that this request will be cached with default {@link Cache}
 *
 * The {@link Cache} can be retrieved in the request via classic injection.
 *
 * This annotation simply prevents you from registering the cache in the {@link CacheService}
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheRequest {
	// Flag annotation
}
