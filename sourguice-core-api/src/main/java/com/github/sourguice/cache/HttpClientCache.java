package com.github.sourguice.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.github.sourguice.value.ValueConstants;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

/**
 * Interceptor that will automatically add Cache-Control header to the HTTP response
 * so that the response may be cached by the client or by a caching proxy
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class HttpClientCache implements MethodInterceptor {

	/**
	 * Guice provider for the current HTTP Response
	 */
	@Inject
	private @CheckForNull Provider<HttpServletResponse> responseProvider;

	/**
	 * Annotation that annotate any method whose response is to be cached
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Documented
	public static @interface CacheInClient {

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

	/**
	 * @return The module to install to enable this interceptor
	 */
	public static Module module() {
		return new AbstractModule() {
			@Override protected void configure() {
				final HttpClientCache interceptor = new HttpClientCache();
				requestInjection(interceptor);
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(CacheInClient.class), interceptor);
			}
		};
	}

	/**
	 * Add protection-related entries to the cache-control list
	 *
	 * @param cacheControl The list to add entries
	 * @param info The annotation
	 * @param response Response used to write Pragma header
	 */
	@SuppressWarnings("PMD.AvoidDuplicateLiterals")
	private static void setCacheControlProtection(final List<String>cacheControl, final CacheInClient info, final HttpServletResponse response) {
		if (info.NoCache().isEmpty()) {
			cacheControl.add("no-cache");
			response.addHeader("Pragma", "no-cache");
			return ;
		}
		else if (!info.NoCache().equals(ValueConstants.DEFAULT_NONE)) {
			cacheControl.add("no-cache=\"" + info.NoCache() + "\"");
			response.addHeader("Pragma", "no-cache");
			return ;
		}

		if (info.Private().isEmpty()) {
			cacheControl.add("private");
			response.addHeader("Pragma", "private");
		}
		else if (!info.Private().equals(ValueConstants.DEFAULT_NONE)) {
			cacheControl.add("private=\"" + info.Private() + "\"");
			response.addHeader("Pragma", "private");
		}
		else if (info.Public()) {
			cacheControl.add("public");
			response.addHeader("Pragma", "public");
		}
	}

	/**
	 * Add flag entries to the cache-control list
	 *
	 * @param cacheControl The list to add entries
	 * @param info The annotation
	 */
	private static void setFlags(final List<String>cacheControl, final CacheInClient info) {
		if (info.NoStore()) {
			cacheControl.add("no-store");
		}

		if (info.NoTransform()) {
			cacheControl.add("no-transform");
		}

		if (info.MustRevalidate()) {
			cacheControl.add("must-revalidate");
		}
	}

	/**
	 * Add age entries to the cache-control list
	 *
	 * @param cacheControl The list to add entries
	 * @param info The annotation
	 */
	private static void setAge(final List<String>cacheControl, final CacheInClient info) {
		if (info.MaxAge() >= 0) {
			cacheControl.add("max-age=" + info.MaxAge());
		}

		if (info.SMaxAge() >= 0) {
			cacheControl.add("s-maxage=" + info.SMaxAge());
		}
	}

	/**
	 * Set the cache-control & pragma headers
	 *
	 * @param info The annotation
	 * @param response The response to set the header on
	 */
	public static void setCacheControl(final CacheInClient info, final HttpServletResponse response) {
		final List<String> cacheControl = new ArrayList<>();

		setCacheControlProtection(cacheControl, info, response);
		setFlags(cacheControl, info);
		setAge(cacheControl, info);

		if (!info.Extension().isEmpty()) {
			cacheControl.add(info.Extension());
		}

		final StringBuilder cacheControlHeader = new StringBuilder();
		String coma = "";
		for (final String value : cacheControl) {
			cacheControlHeader.append(coma);
			cacheControlHeader.append(value);
			coma = ",";
		}
		response.addHeader("Cache-Control", cacheControlHeader.toString());
	}

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		final Object ret = invocation.proceed();

		final CacheInClient info = invocation.getMethod().getAnnotation(CacheInClient.class);
		if (info == null) {
			return ret;
		}

		if (this.responseProvider == null) {
			throw new UnsupportedOperationException("HttpClientCache interceptor has not been injected");
		}
		final HttpServletResponse response = this.responseProvider.get();

		setCacheControl(info, response);

		return ret;
	}

}
