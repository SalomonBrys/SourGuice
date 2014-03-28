package com.github.sourguice;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
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
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.cache.CacheService;
import com.github.sourguice.cache.impl.CacheServiceImpl;
import com.github.sourguice.call.impl.PathVariablesHolder;
import com.github.sourguice.call.impl.SGInvocationFactoryImpl;
import com.github.sourguice.controller.ControllerHandlersRepository;
import com.github.sourguice.controller.ControllerInterceptor;
import com.github.sourguice.controller.ControllersServlet;
import com.github.sourguice.controller.GuiceTypedProvider;
import com.github.sourguice.controller.InstanceTypedProvider;
import com.github.sourguice.controller.InterceptWithMatcher;
import com.github.sourguice.controller.MembersInjectionRequest;
import com.github.sourguice.controller.TypedProvider;
import com.github.sourguice.conversion.ConversionService;
import com.github.sourguice.conversion.Converter;
import com.github.sourguice.conversion.def.BooleanConverter;
import com.github.sourguice.conversion.def.DoubleConverter;
import com.github.sourguice.conversion.def.EnumConverter;
import com.github.sourguice.conversion.def.FloatConverter;
import com.github.sourguice.conversion.def.IntegerConverter;
import com.github.sourguice.conversion.def.LongConverter;
import com.github.sourguice.conversion.def.ShortConverter;
import com.github.sourguice.conversion.def.StringConverter;
import com.github.sourguice.conversion.impl.ConversionServiceImpl;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.exception.ExceptionService;
import com.github.sourguice.exception.def.SGResponseExceptionHandler;
import com.github.sourguice.exception.impl.ExceptionServiceImpl;
import com.github.sourguice.request.ForwardableRequestFactory;
import com.github.sourguice.request.wrapper.GuiceForwardHttpRequest;
import com.github.sourguice.throwable.SGRuntimeException;
import com.github.sourguice.throwable.controller.SGResponseException;
import com.github.sourguice.throwable.service.exception.UnreachableExceptionHandlerException;
import com.github.sourguice.utils.RedirectServlet;
import com.github.sourguice.utils.SGCallInterceptSetter;
import com.github.sourguice.value.RequestMethod;
import com.github.sourguice.value.ValueConstants;
import com.github.sourguice.view.Model;
import com.github.sourguice.view.ViewRenderer;
import com.github.sourguice.view.ViewRendererService;
import com.github.sourguice.view.def.JSPViewRenderer;
import com.github.sourguice.view.impl.ViewRendererServiceImpl;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;

/**
 * This is used by {@link SourGuice} to actually bind the implementations of SourGuices classes
 * This is needed because SourGuice builds two different jars : one for API and one for implementation
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@SuppressWarnings({"static-method", "PMD.TooManyMethods", "PMD.CouplingBetweenObjects"})
public class SourGuiceModuleImpl extends ServletModule implements SourGuiceModule {

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
	protected @CheckForNull List<GuiceTypedProvider<?>> gtpToInject = new LinkedList<>();

	/**
	 * Contains all ControllerHandlers
	 * This is to make sure that there will be one and only one ControllerHandler for each controller class
	 */
	private final ControllerHandlersRepository repository = new ControllerHandlersRepository();

	/**
	 * Conversion service
	 */
	protected final ConversionServiceImpl conversionService = new ConversionServiceImpl();

	/**
	 * Exception service
	 */
	protected final ExceptionServiceImpl exceptionService = new ExceptionServiceImpl();

	/**
	 * ViewRenderer service
	 */
	protected final ViewRendererServiceImpl rendererService = new ViewRendererServiceImpl();

	/**
	 * Simple class that holds a pattern and an associated controller
	 * Used to remember the controller to register between {@link SourGuiceModuleImpl#control(String, String...)} and {@link SourGuiceModuleImpl#module()}
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
	 * Registers in guice the PrintWriter class to be binded to the request's response writer
	 *
	 * @param res The current HTTP response object
	 * @return The response writer
	 * @throws IOException If an input or output exception occurs
	 */
	@Provides @RequestScoped public PrintWriter getRequestPrintWriter(final HttpServletResponse res) throws IOException {
		return res.getWriter();
	}

	/**
	 * Registers in guice the Writer class to be binded to the request's response writer
	 *
	 * @param res The current HTTP response object
	 * @return The response writer
	 * @throws IOException If an input or output exception occurs
	 */
	@Provides @RequestScoped public Writer getRequestWriter(final HttpServletResponse res) throws IOException {
		return res.getWriter();
	}

	/**
	 * Registers in guice the ServletOutputStream class to be binded to the request's response output stream
	 *
	 * @param res The current HTTP response object
	 * @return The response stream
	 * @throws IOException If an input or output exception occurs
	 */
	@Provides @RequestScoped public ServletOutputStream getRequestServletOutputStream(final HttpServletResponse res) throws IOException {
		return res.getOutputStream();
	}

	/**
	 * Registers in guice the OutputStream class to be binded to the request's response output stream
	 *
	 * @param res The current HTTP response object
	 * @return The response stream
	 * @throws IOException If an input or output exception occurs
	 */
	@Provides @RequestScoped public OutputStream getRequestOutputStream(final HttpServletResponse res) throws IOException {
		return res.getOutputStream();
	}

	/**
	 * Registers in guice the MatchResult class to be binded to the request's URL parsed path variables
	 * according to the request's {@link RequestMapping}
	 *
	 * @param req The request of the match
	 * @return The MatchResult
	 */
	@Provides @RequestScoped public @CheckForNull MatchResult getPathMatcher(final HttpServletRequest req) {
		return (MatchResult) req.getAttribute(ValueConstants.MATCH_RESULT_REQUEST_ATTRIBUTE);
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

	/**
	 * Registers in guice the RequestMethod enum to be binded to the request's method
	 *
	 * @param req The current HTTP request object
	 * @return The RequestMethod of the current request
	 */
	@Provides @RequestScoped public RequestMethod getRequestMethod(final HttpServletRequest req) {
		return RequestMethod.valueOf(req.getMethod());
	}

	@Override
	@SuppressWarnings({"PMD.AvoidUsingShortType"})
	public final void configureServlets() {

		// Registers default converters
		convertTo(boolean.class, Boolean.class).withInstance(new BooleanConverter());
		convertTo(short.class, Short.class).withInstance(new ShortConverter());
		convertTo(int.class, Integer.class).withInstance(new IntegerConverter());
		convertTo(long.class, Long.class).withInstance(new LongConverter());
		convertTo(float.class, Float.class).withInstance(new FloatConverter());
		convertTo(double.class, Double.class).withInstance(new DoubleConverter());
		convertTo(Enum.class).withInstance(new EnumConverter());
		convertTo(String.class).withInstance(new StringConverter());

		// Registers default exception handlers
		try {
			handleException(SGResponseException.class).with(SGResponseExceptionHandler.class);
		}
		catch (UnreachableExceptionHandlerException e) {
			throw new SGRuntimeException(e);
		}

		// Registers default view renderers
		renderViews(".*\\.jsp").with(JSPViewRenderer.class);

		// Binds the services
		bind(ConversionService.class).toInstance(this.conversionService);
		bind(ExceptionService.class).toInstance(this.exceptionService);
		bind(ViewRendererService.class).toInstance(this.rendererService);
		bind(CacheService.class).to(CacheServiceImpl.class);

		// Binds view related classes
		bind(Model.class).in(ServletScopes.REQUEST);

		// Binds method calling related classes
		bind(new TypeLiteral<Map<String, String>>() {/**/}).annotatedWith(PathVariablesMap.class).toProvider(PathVariablesHolder.class).in(RequestScoped.class);

		// Creates a controllerHandler repository and registers it in guice
		// We create it because we need to handle it directly in this method
		bind(ControllerHandlersRepository.class).toInstance(this.repository);

		// Binds Intercept
		bind(SGCallInterceptSetter.class);

		// Binds interceptors
		final ControllerInterceptor interceptor = new ControllerInterceptor();
		requestInjection(interceptor);
		bindInterceptor(Matchers.any(), new InterceptWithMatcher(), interceptor);
		bindInterceptor(new InterceptWithMatcher(), Matchers.any(), interceptor);

		assert this.patternControllers != null;
		for (final PatternController entry : this.patternControllers) {
			createServlet(entry.pattern, entry.controller);
		}

		// Loops through all registered patterns and their corresponding ControllerHandler.
		// Registers each couple in Guice.
		for (final Entry<String, HttpServlet> entry : this.servlets.entrySet()) {
			requestInjection(entry.getValue());
			serve(entry.getKey()).with(entry.getValue());
		}

		assert this.gtpToInject != null;
		for (final GuiceTypedProvider<?> gtp : this.gtpToInject) {
			requestInjection(gtp);
		}

		// Sets null to the servlets variable so any further call to control().with() will raise a NullPointerException
		this.patternControllers = null;
	}

	/**
	 * Registers a pattern to a controller class
	 * This is called by {@link AbstractBindBuilder#with(Class)}
	 *
	 * @param pattern The pattern on which to register to controller
	 * @param controller The controller class to register
	 */
	public void createServlet(final String pattern, final TypedProvider<?> controller) {
		final Map<String, String> initParams = new HashMap<>();
		initParams.put("pattern", pattern);

		final MembersInjectionRequest membersInjector = new MembersInjectionRequest() {
			@SuppressWarnings("synthetic-access")
			@Override public void requestMembersInjection(final Object instance) {
				requestInjection(instance);
			}
		};

		// Creates a controller servlet for this pattern or gets it if this pattern has already been registered
		ControllersServlet servlet;
		if (this.servlets.containsKey(pattern)) {
			servlet = (ControllersServlet) this.servlets.get(pattern);
		}
		else {
			servlet = new ControllersServlet(membersInjector);
			this.servlets.put(pattern, servlet);
		}

		// Registers a controller handler into the controller servlet
		// The handler is retrived from the repository to avoid creating two handlers for the same controller class
		servlet.addController(this.repository.get(controller, membersInjector, new SGInvocationFactoryImpl(binder())));
	}

	/**
	 * Interface returned by {@link #control(String, String...)} to permit the syntax control(pattern).with(controller.class)
	 *
	 * @param <T> The type of instance to register
	 */
	protected abstract class AbstractBindBuilder<T> implements BindBuilder<T> {

		@Override
		public void with(final Key<? extends T> key) {
			if (SourGuiceModuleImpl.this.gtpToInject == null) {
				throw new UnsupportedOperationException("You cannot register anything after calling install(sourguice.module())");
			}
			final GuiceTypedProvider<? extends T> getter = new GuiceTypedProvider<>(key);
			SourGuiceModuleImpl.this.gtpToInject.add(getter);
			register(getter);
		}

		@Override
		public void with(final Class<? extends T> type) {
			with(Key.get(type));
		}

		@Override
		public void with(final TypeLiteral<? extends T> type) {
			with(Key.get(type));
		}

		@Override
		public void withInstance(final T instance) {
			final TypedProvider<? extends T> getter = new InstanceTypedProvider<>(instance);
			register(getter);
		}

		@Override
		public void withInstance(final T instance, final TypeLiteral<T> type) {
			final TypedProvider<? extends T> getter = new InstanceTypedProvider<>(instance, type);
			register(getter);
		}

		/**
		 * Makes the registration within the corresponding service.
		 *
		 * @param getter The {@link TypedProvider} to register within the service.
		 */
		abstract protected void register(TypedProvider<? extends T> getter);
	}

	@Override
	public final BindBuilder<Object> control(final String pattern, final String... patterns) {
		return new AbstractBindBuilder<Object>() {
			@Override protected void register(final TypedProvider<? extends Object> controller) {
				if (SourGuiceModuleImpl.this.patternControllers == null) {
					throw new UnsupportedOperationException("You cannot register new controllers after calling install(sourguice.module())");
				}
				SourGuiceModuleImpl.this.patternControllers.add(new PatternController(pattern, controller));
				for (final String addPattern : patterns) {
					SourGuiceModuleImpl.this.patternControllers.add(new PatternController(addPattern, controller));
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
				SourGuiceModuleImpl.this.servlets.put(pattern, servlet);
				for (final String addPattern : patterns) {
					SourGuiceModuleImpl.this.servlets.put(addPattern, servlet);
				}
			}
		};
	}

	@Override
	public BindBuilder<ViewRenderer> renderViews(final String regex, final String... regexs) {
		return new AbstractBindBuilder<ViewRenderer>() {
			@Override protected void register(final TypedProvider<? extends ViewRenderer> renderer) {
				SourGuiceModuleImpl.this.rendererService.register(Pattern.compile(regex), renderer);
				for (final String addRegex : regexs) {
					SourGuiceModuleImpl.this.rendererService.register(Pattern.compile(addRegex), renderer);
				}
			}
		};
	}

	@Override
	public final BindBuilder<Converter<?>> convertTo(final Class<?> toType, final Class<?>... toTypes) {
		return new AbstractBindBuilder<Converter<?>>() {
			@Override protected void register(final TypedProvider<? extends Converter<?>> converter) {
				SourGuiceModuleImpl.this.conversionService.register(toType, converter);
				for (final Class<?> addToType : toTypes) {
					SourGuiceModuleImpl.this.conversionService.register(addToType, converter);
				}
			}
		};
	}

	@Override @SafeVarargs
	public final <T extends Exception> BindBuilder<ExceptionHandler<T>> handleException(final Class<? extends T> exc, final Class<? extends T>... excs) {
		return new AbstractBindBuilder<ExceptionHandler<T>>() {
			@Override protected void register(final TypedProvider<? extends ExceptionHandler<T>> handler) {
				SourGuiceModuleImpl.this.exceptionService.register(exc, handler);
				for (final Class<? extends T> addExc : excs) {
					SourGuiceModuleImpl.this.exceptionService.register(addExc, handler);
				}
			}
		};
	}

	@Override
	public Module module() {
		return this;
	}
}
