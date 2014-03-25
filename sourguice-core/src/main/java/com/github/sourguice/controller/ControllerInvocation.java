package com.github.sourguice.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.annotation.controller.HttpError;
import com.github.sourguice.annotation.request.PathVariable;
import com.github.sourguice.annotation.request.Redirects;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.View;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.call.ArgumentFetcher;
import com.github.sourguice.call.ArgumentFetcherFactory;
import com.github.sourguice.call.SGInvocation;
import com.github.sourguice.call.SGInvocationFactory;
import com.github.sourguice.controller.fetchers.PathVariableArgumentFetcher;
import com.github.sourguice.throwable.invocation.HandledException;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;
import com.github.sourguice.utils.Annotations;
import com.github.sourguice.utils.Arrays;
import com.github.sourguice.utils.HttpStrings;
import com.github.sourguice.value.RequestMethod;
import com.google.inject.TypeLiteral;

/**
 * Class that holds every informations available at compile time needed to call a controller's method
 * Each Invocation object corresponds to a method of a controller
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public final class ControllerInvocation implements ArgumentFetcherFactory {

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
	private final RequestMapping mapping;

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
	protected final Map<String, Integer> matchRef = new HashMap<>();

	/**
	 * The handler of the controller of the method
	 */
	private final ControllerHandler<?> controller;

	/**
	 * The invocation itself (method & argument fetchers)
	 */
	private final SGInvocation invocation;

	/**
	 * Constructor
	 *
	 * @param mapping The annotation that must be present on each invocation method
	 * @param controller The controller on witch to call the method
	 * @param method The method to call
     * @param membersInjector Responsible for injecting newly created {@link ArgumentFetcher}
     * @param invocationFactory The factory responsible for creating new invocations
	 */
	public ControllerInvocation(final ControllerHandler<?> controller, final RequestMapping mapping, final Method method, final MembersInjectionRequest membersInjector, final SGInvocationFactory invocationFactory) {
		// Set properties
		this.controller = controller;
		this.mapping = mapping;

		this.view = Annotations.getOneTreeRecursive(View.class, method);

		this.writes = Annotations.getOneTreeRecursive(Writes.class, method);

		this.httpError = Annotations.getOneRecursive(HttpError.class, method.getAnnotations());

		this.redirects = Annotations.getOneRecursive(Redirects.class, method.getAnnotations());

		// Transform URL like "/foo-{bar}" into /foo-[^/]+ and registers "bar" as match 1
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

		this.invocation = invocationFactory.newInvocation(controller.getTypeLiteral(), method, this);

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
	 * Invoke the method on the controller
	 *
	 * @param throwWhenHandled Whether to throw a {@link HandledException} when an exception has been caught and handled.
	 *                         This allows to cancel all future work until the {@link HandledException} has been caught (and ignored).
	 * @return What the method call returned
	 * @throws NoSuchRequestParameterException In case of a parameter asked from request argument or path variable that does not exists
	 * @throws InvocationTargetException Any thing that the method call might have thrown
	 * @throws HandledException If an exception has been caught and handled. It is safe to ignore and used to cancel any depending work.
	 * @throws IOException IO failure while writing the response
	 */
	public @CheckForNull Object invoke(final boolean throwWhenHandled) throws NoSuchRequestParameterException, InvocationTargetException, HandledException, IOException {
		return this.invocation.invoke(this.controller.get(), throwWhenHandled);
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

	@Override
	public ArgumentFetcher<?> create(final Method method, final int position, final TypeLiteral<?> argType) {
		final PathVariable pathVariable = Annotations.fromArray(method.getParameterAnnotations()[position]).getAnnotation(PathVariable.class);
		if (pathVariable != null) {
			return new PathVariableArgumentFetcher<>(argType, pathVariable, this.matchRef);
		}
		return null;
	}
}
