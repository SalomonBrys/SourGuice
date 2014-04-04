package com.github.sourguice.mvc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.MultipleBindBuilder;
import com.github.sourguice.SingleBindBuilder;
import com.github.sourguice.SourGuice;
import com.github.sourguice.call.SGInvocation;
import com.github.sourguice.call.SGInvocationFactory;
import com.github.sourguice.mvc.annotation.request.PathVariablesMap;
import com.github.sourguice.mvc.annotation.request.RequestMapping;
import com.github.sourguice.mvc.controller.ControllerHandlersRepository;
import com.github.sourguice.mvc.controller.ControllersServer;
import com.github.sourguice.mvc.controller.ControllersServlet;
import com.github.sourguice.mvc.controller.PathVariablesHolder;
import com.github.sourguice.mvc.exception.def.SGResponseExceptionHandler;
import com.github.sourguice.mvc.request.ForwardableRequestFactory;
import com.github.sourguice.mvc.request.GuiceForwardHttpRequest;
import com.github.sourguice.mvc.throwable.controller.SGResponseException;
import com.github.sourguice.mvc.utils.RedirectServlet;
import com.github.sourguice.mvc.view.Model;
import com.github.sourguice.mvc.view.ViewRenderer;
import com.github.sourguice.mvc.view.ViewRendererService;
import com.github.sourguice.mvc.view.def.JSPViewRenderer;
import com.github.sourguice.mvc.view.impl.ViewRendererServiceImpl;
import com.github.sourguice.provider.GTPModuleFactory;
import com.github.sourguice.provider.TypedProvider;
import com.github.sourguice.provider.TypedProviderMultipleBindBuilder;
import com.github.sourguice.provider.TypedProviderSingleBindBuilder;
import com.github.sourguice.throwable.SGRuntimeException;
import com.github.sourguice.throwable.exception.UnreachableExceptionHandlerException;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;

/**
 * This is used by {@link SourGuiceMvc} to actually bind the implementations of SourGuices classes
 * This is needed because SourGuice builds two different jars : one for API and one for implementation
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@SuppressWarnings({"static-method", "PMD.TooManyMethods", "PMD.CouplingBetweenObjects"})
public class SourGuiceMvcModuleImpl extends ServletModule implements SourGuiceMvcModule {

	/**
	 * The list of registered path and their corresponding controllers
	 * The purpose of this is to ensure that a path is handled by one controller,
	 * even if this path has been registered multiple times
	 */
	protected @CheckForNull List<PatternController> patternControllers = new LinkedList<>();

	/**
	 * List of servlet and their mapping that we will serve
	 *
	 * This is a {@link LinkedHashMap} because we need to remember the order of registration
	 * (specifically, redirect servlets are to be registers before controllers
	 */
	protected final Map<String, HttpServlet> servlets = new LinkedHashMap<>();

	/**
	 * List of providers that need injection and that we created before calling {@link #module()}
	 */
	protected @CheckForNull GTPModuleFactory gtpFactory = new GTPModuleFactory();

	/**
	 * Contains all ControllerHandlers
	 * This is to make sure that there will be one and only one ControllerHandler for each controller class
	 */
	private final ControllerHandlersRepository repository = new ControllerHandlersRepository();

	/**
	 * ViewRenderer service
	 */
	protected final ViewRendererServiceImpl rendererService = new ViewRendererServiceImpl();

	/**
	 * SourGuice core implementation
	 */
	private final SourGuice sourguice;

	/**
	 * Constructor
	 *
	 * @param sourguice SourGuice core implementation
	 */
	public SourGuiceMvcModuleImpl(SourGuice sourguice) {
		this.sourguice = sourguice;
	}

	/**
	 * Simple class that holds a pattern and an associated controller
	 * Used to remember the controller to register between {@link SourGuiceMvcModuleImpl#control(String, String...)} and {@link SourGuiceMvcModuleImpl#module()}
	 */
	protected static class PatternController {
		/** The pattern to register the controller on */
		public final String pattern;
		/** The controller to register */
		public final TypedProvider<?> controller;
		/**
		 * @param pattern The pattern to register the controller on
		 * @param controller The controller to register
		 */
		public PatternController(final String pattern, final TypedProvider<?> controller) {
			super();
			this.pattern = pattern;
			this.controller = controller;
		}
	}

	/**
	 * Registers in guice the MatchResult class to be binded to the request's URL parsed path variables
	 * according to the request's {@link RequestMapping}
	 *
	 * @param req The request of the match
	 * @return The MatchResult
	 */
	@Provides @RequestScoped public @CheckForNull MatchResult getPathMatcher(final HttpServletRequest req) {
		return (MatchResult) req.getAttribute(ControllersServer.MATCH_RESULT_REQUEST_ATTRIBUTE);
	}

	/**
	 * Registers in guice the ForwardableRequestFactory class that easily allow servlet request forwarding
	 * Attention: Do not to be confuse Servlet redirection with HTTP redirection!
	 *
	 * @param req The current HTTP request object
	 * @param context The current Servlet Context object
	 * @return The ForwardableRequestFactory usable for the current request
	 */
	@Provides @RequestScoped public ForwardableRequestFactory getForwardableRequestFactory(final HttpServletRequest req, final ServletContext context) {
		return new GuiceForwardHttpRequest(req, context);
	}

	@Override
	@SuppressWarnings({"PMD.AvoidUsingShortType"})
	public final void configureServlets() {

		// Registers default exception handlers
		try {
			this.sourguice.handleException(SGResponseException.class).with(SGResponseExceptionHandler.class);
		}
		catch (UnreachableExceptionHandlerException e) {
			throw new SGRuntimeException(e);
		}

		// Registers default view renderers
		renderViews(".*\\.jsp").with(JSPViewRenderer.class);

		// Binds the services
		bind(ViewRendererService.class).toInstance(this.rendererService);

		// Binds view related classes
		bind(Model.class).in(ServletScopes.REQUEST);

		// Binds method calling related classes
		bind(new TypeLiteral<Map<String, String>>() {/**/}).annotatedWith(PathVariablesMap.class).toProvider(PathVariablesHolder.class).in(RequestScoped.class);

		// Creates a controllerHandler repository and registers it in guice
		// We create it because we need to handle it directly in this method
		bind(ControllerHandlersRepository.class).toInstance(this.repository);

		// Binds controllers
		SGInvocationFactory invocationFactory = this.sourguice.newInvocationFactory(binder());
		assert this.patternControllers != null;
		for (final PatternController entry : this.patternControllers) {
			createServlet(entry.pattern, entry.controller, invocationFactory);
		}

		// Loops through all registered patterns and their corresponding ControllerHandler.
		// Registers each couple in Guice.
		for (final Entry<String, HttpServlet> entry : this.servlets.entrySet()) {
			requestInjection(entry.getValue());
			serve(entry.getKey()).with(entry.getValue());
		}

		assert this.gtpFactory != null;
		this.gtpFactory.requestInjection(binder());
		this.gtpFactory = null;

		// Sets null to the servlets variable so any further call to control().with() will raise a NullPointerException
		this.patternControllers = null;

		install(this.sourguice.module());
	}

	/**
	 * Registers a pattern to a controller class
	 * This is called by {@link SingleBindBuilder#with(Class)}
	 *
	 * @param pattern The pattern on which to register to controller
	 * @param controller The controller class to register
	 * @param invocationFactory The factory that creates {@link SGInvocation} that will be cached for each of the controller's method.
	 */
	public void createServlet(final String pattern, final TypedProvider<?> controller, SGInvocationFactory invocationFactory) {
		final Map<String, String> initParams = new HashMap<>();
		initParams.put("pattern", pattern);

		// Creates a controller servlet for this pattern or gets it if this pattern has already been registered
		ControllersServlet servlet;
		if (this.servlets.containsKey(pattern)) {
			servlet = (ControllersServlet) this.servlets.get(pattern);
		}
		else {
			servlet = new ControllersServlet(binder());
			this.servlets.put(pattern, servlet);
		}

		// Registers a controller handler into the controller servlet
		// The handler is retrived from the repository to avoid creating two handlers for the same controller class
		servlet.addController(this.repository.get(controller, binder(), invocationFactory));
	}

	@Override
	public final MultipleBindBuilder<Object> control(final String pattern, final String... patterns) {
		return new TypedProviderMultipleBindBuilder<Object>(this.gtpFactory) {
			@Override protected void register(final TypedProvider<? extends Object> controller) {
				if (SourGuiceMvcModuleImpl.this.patternControllers == null) {
					throw new UnsupportedOperationException("You cannot register new controllers after calling install(sourguice.module())");
				}
				SourGuiceMvcModuleImpl.this.patternControllers.add(new PatternController(pattern, controller));
				for (final String addPattern : patterns) {
					SourGuiceMvcModuleImpl.this.patternControllers.add(new PatternController(addPattern, controller));
				}
			}
		};
	}

	@Override
	public final RedirectBuilder redirect(final String pattern, final String... patterns) {
		return new RedirectBuilder() {
			@SuppressWarnings({"PMD.ShortMethodName"})
			@Override public void to(final String dest) {
				final RedirectServlet servlet = new RedirectServlet(dest);
				SourGuiceMvcModuleImpl.this.servlets.put(pattern, servlet);
				for (final String addPattern : patterns) {
					SourGuiceMvcModuleImpl.this.servlets.put(addPattern, servlet);
				}
			}
		};
	}

	@Override
	public SingleBindBuilder<ViewRenderer> renderViews(final String regex, final String... regexs) {
		return new TypedProviderSingleBindBuilder<ViewRenderer>(this.gtpFactory) {
			@Override protected void register(final TypedProvider<? extends ViewRenderer> renderer) {
				SourGuiceMvcModuleImpl.this.rendererService.register(Pattern.compile(regex), renderer);
				for (final String addRegex : regexs) {
					SourGuiceMvcModuleImpl.this.rendererService.register(Pattern.compile(addRegex), renderer);
				}
			}
		};
	}

	@Override
	public Module module() {
		return this;
	}
}
