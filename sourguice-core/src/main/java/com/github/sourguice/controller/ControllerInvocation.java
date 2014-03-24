package com.github.sourguice.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.controller.HttpError;
import com.github.sourguice.annotation.request.InterceptParam;
import com.github.sourguice.annotation.request.PathVariable;
import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.Redirects;
import com.github.sourguice.annotation.request.RequestAttribute;
import com.github.sourguice.annotation.request.RequestHeader;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.annotation.request.SessionAttribute;
import com.github.sourguice.annotation.request.View;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.call.impl.PathVariablesHolder;
import com.github.sourguice.controller.fetchers.InjectorArgumentFetcher;
import com.github.sourguice.controller.fetchers.NullArgumentFetcher;
import com.github.sourguice.controller.fetchers.PathVariableArgumentFetcher;
import com.github.sourguice.controller.fetchers.RequestAttributeArgumentFetcher;
import com.github.sourguice.controller.fetchers.RequestHeaderArgumentFetcher;
import com.github.sourguice.controller.fetchers.RequestParamArgumentFetcher;
import com.github.sourguice.controller.fetchers.SessionAttributeArgumentFetcher;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.utils.Annotations;
import com.github.sourguice.utils.Arrays;
import com.github.sourguice.utils.HttpStrings;
import com.github.sourguice.value.RequestMethod;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Class that holds every informations available at compile time needed to call a controller's method
 * Each Invocation object corresponds to a method of a controller
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public final class ControllerInvocation {

	/**
	 * Pattern to detect a variable in a url string
	 */
	static private final Pattern SEARCH = Pattern.compile("\\{([a-zA-Z0-9\\-_]+)\\}");

	/**
	 * Regular expressions to be applied to a URL to see if it matches
	 * It bascially is the @{@link RequestMapping} URL like /foo-{bar}/plop to /foo-[^/]+/plop
	 */
	private final List<Pattern> patterns = new ArrayList<>();

	/**
	 * The Annotation of a controller's method
	 */
	private final @CheckForNull RequestMapping mapping;

	/**
	 * All fetchers for each arguments of the method
	 */
	private final ArgumentFetcher<?>[] fetchers;

	/**
	 * The method of this Invocation
	 */
	private final Method method;

	/**
	 * View annotation if defined on the method
	 */
	private final @CheckForNull View view;

	/**
	 * Writes annotation if defined on the method
	 */
	private final @CheckForNull Writes writes;

	/**
	 * HttpError annotation if defined on the method
	 */
	private final @CheckForNull HttpError httpError;

	/**
	 * Redirects annotation if defined on the method
	 */
	private final @CheckForNull Redirects redirects;

	/**
	 * The reference of each path variable name and their position in the url regex
	 */
	private final Map<String, Integer> matchRef = new HashMap<>();

	/**
	 * The handler of the controller of the method
	 */
	private final ControllerHandler<?> controller;

	/**
	 * Provider for {@link PathVariablesHolder}
	 */
	@Inject
	private @CheckForNull Provider<PathVariablesHolder> pathVariablesProvider;

	/**
	 * Creates the appropriate fetcher for the given argument
	 *
	 * @param type The argument's type
	 * @param annotations The argument's annotation
	 * @return The appropriate argument fetcher
	 */
	private <T> ArgumentFetcher<T> createFetcher(final TypeLiteral<T> type, final Annotation[] annotations) {
		final AnnotatedElement annos = Annotations.fromArray(annotations);

		final RequestParam requestParam = annos.getAnnotation(RequestParam.class);
		if (requestParam != null) {
			return new RequestParamArgumentFetcher<>(type, requestParam);
		}

		final PathVariable pathVariable = annos.getAnnotation(PathVariable.class);
		if (pathVariable != null) {
			final boolean check = this.mapping != null && this.mapping.value().length > 0;
			return new PathVariableArgumentFetcher<>(type, pathVariable, check ? this.matchRef : null);
		}

		final RequestAttribute requestAttribute = annos.getAnnotation(RequestAttribute.class);
		if (requestAttribute != null) {
			return new RequestAttributeArgumentFetcher<>(type, requestAttribute);
		}

		final SessionAttribute sessionAttribute = annos.getAnnotation(SessionAttribute.class);
		if (sessionAttribute != null) {
			return new SessionAttributeArgumentFetcher<>(type, sessionAttribute);
		}

		final RequestHeader requestHeader = annos.getAnnotation(RequestHeader.class);
		if (requestHeader != null) {
			return new RequestHeaderArgumentFetcher<>(type, requestHeader);
		}

		final InterceptParam interceptParam = annos.getAnnotation(InterceptParam.class);
		if (interceptParam != null) {
			return new NullArgumentFetcher<>();
		}

		return new InjectorArgumentFetcher<>(type, annotations);
	}

	/**
	 * @param mapping The annotation that must be present on each invocation method
	 * @param controller The controller on witch to call the method
	 * @param method The method to call
     * @param membersInjector Responsible for injecting newly created {@link ArgumentFetcher}
	 */
	public ControllerInvocation(final ControllerHandler<?> controller, final @CheckForNull RequestMapping mapping, final Method method, final MembersInjectionRequest membersInjector) {
		// Set properties
		this.controller = controller;
		this.mapping = mapping;
		this.method = method;

		this.view = Annotations.getOneTreeRecursive(View.class, method);

		this.writes = Annotations.getOneTreeRecursive(Writes.class, method);

		this.httpError = Annotations.getOneRecursive(HttpError.class, method.getAnnotations());

		this.redirects = Annotations.getOneRecursive(Redirects.class, method.getAnnotations());

		// Transform URL like "/foo-{bar}" into /foo-[^/]+ and registers "bar" as match 1
		if (this.mapping != null) {
			for (String location : this.mapping.value()) {
				final Matcher matcher = SEARCH.matcher(location);
				int pos = 1;
				while (matcher.find()) {
					this.matchRef.put(matcher.group(1), Integer.valueOf(pos));
					++pos;
				}
				location = matcher.replaceAll("([^/]+)");
				this.patterns.add(Pattern.compile(location));
			}
		}

		// Registers all fetchers
		// Fetchers are configured in constructor so they are constructed only once
		// If no fetcher is suitable, then uses the guice fetcher
		final List<TypeLiteral<?>> parameterTypes = controller.getTypeLiteral().getParameterTypes(method);
		final Annotation[][] annotations = method.getParameterAnnotations();
		this.fetchers = new ArgumentFetcher<?>[parameterTypes.size()];
		for (int n = 0; n < parameterTypes.size(); ++n) {
			this.fetchers[n] = createFetcher(parameterTypes.get(n), annotations[n]);
			membersInjector.requestMembersInjection(this.fetchers[n]);
		}

		membersInjector.requestMembersInjection(this);
	}

	/**
	 * This will determine if this invocation can serve for this request and how confident it is to serve it
	 * The more confident it is, the more specialised it is for this request.
	 * @param req The request
	 * @return InvocationInfos with all infos (including confidence) if it can, null if it can't
	 */
	@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
	public @CheckForNull ControllerInvocationInfos canServe(final HttpServletRequest req) {
		if (this.mapping == null) {
			return null;
		}

		ControllerInvocationInfos ret = new ControllerInvocationInfos(this);

		// Checks if the URL declared in @RequestMapping matches. This is mandatory
		for (final Pattern pattern : this.patterns) {
			String path = req.getPathInfo();
			if (path == null) {
				path = "/";
			}
			final Matcher matcher = pattern.matcher(path);
			if (matcher.matches()) {
				ret.urlMatch = matcher.toMatchResult();
				break ;
			}
		}
		if (ret.urlMatch == null) {
			return null;
		}

		// Checks the HTTP Method
		if (this.mapping.method().length > 0) {
			final RequestMethod requestMethod = RequestMethod.valueOf(req.getMethod());
			if (Arrays.contains(this.mapping.method(), requestMethod)) {
				++ret.confidence;
			}
			else {
				return null;
			}
		}

		// Checks request parametes
		for (final String param : this.mapping.params()) {
			if (req.getParameter(param) != null) {
				++ret.confidence;
			}
			else {
				return null;
			}
		}

		// Checks HTTP headers
		for (final String header : this.mapping.headers()) {
			if (req.getHeader(header) != null) {
				++ret.confidence;
			}
			else {
				return null;
			}
		}

		// Checks HTTP header Content-Type
		if (this.mapping.consumes().length > 0) {
			if (Arrays.contains(this.mapping.consumes(), req.getContentType())) {
				++ret.confidence;
			}
			else {
				return null;
			}
		}

		// Checks HTTP header Accept
		if (this.mapping.produces().length > 0) {
			if (req.getHeader("Accept") == null) {
				return null;
			}
			if (HttpStrings.acceptContains(req.getHeader("Accept"), this.mapping.produces())) {
				++ret.confidence;
			}
			else {
				return null;
			}
		}

		return ret;
	}

	/**
	 * This is where the magic happens: This will invoke the method by fetching all of its arguments and call it
	 *
	 * @param pathVariables Variables that were parsed from request URL
	 * @return What the method call returned
	 * @throws NoSuchRequestParameterException In case of a parameter asked from request argument or path variable that does not exists
	 * @throws InvocationTargetException Any thing that the method call might have thrown
	 */
	public @CheckForNull Object invoke(
			final @PathVariablesMap Map<String, String> pathVariables
			) throws NoSuchRequestParameterException, InvocationTargetException {

		assert this.pathVariablesProvider != null;

		// Pushes path variables to the stack, this permits to have invocations inside invocations
		this.pathVariablesProvider.get().push(pathVariables);

		try {
			// Fetches all arguments
			Object[] params = new Object[this.fetchers.length];
			Object invocRet = null;
			for (int n = 0; n < this.fetchers.length; ++n) {
				params[n] = this.fetchers[n].getPrepared();
			}

			try {
				// Calls the method
				final Object ret = this.method.invoke(this.controller.getInstance(), params);

				// If method did not returned void, gets what it returned
				if (!this.method.getReturnType().equals(Void.TYPE) && !this.method.getReturnType().equals(void.class)) {
					invocRet = ret;
				}
			}
			catch (IllegalAccessException e) {
				throw new UnsupportedOperationException(e);
			}

			// Returns whatever the method call returned
			return invocRet;
		}
		finally {
			// Pops path variables from the stack, this invocation is over
			this.pathVariablesProvider.get().pop();
		}
	}

	/**
	 * @see #invoke(HttpServletRequest, Map, Injector, CalltimeArgumentFetcher...)
	 */
	@SuppressWarnings("javadoc")
	public @CheckForNull Object invoke(
			final MatchResult urlMatch
			) throws NoSuchRequestParameterException, InvocationTargetException {
		return invoke(PathVariablesHolder.fromMatch(urlMatch, this.matchRef));
	}

	/**
	 * @return The invocation's method's name
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * @return The controller of the method of this invocation
	 */
	public ControllerHandler<?> getController() {
		return this.controller;
	}

	/**
	 * @return View annotation if defined on the method
	 */
	public @CheckForNull View getView() {
		return this.view;
	}

	/**
	 * @return Writes annotation if defined on the method
	 */
	public @CheckForNull Writes getWrites() {
		return this.writes;
	}

	/**
	 * @return HttpError annotation if defined on the method
	 */
	public @CheckForNull HttpError getHttpError() {
		return this.httpError;
	}

	/**
	 * @return Redirects annotation if defined on the method
	 */
	public @CheckForNull Redirects getRedirects() {
		return this.redirects;
	}
}
