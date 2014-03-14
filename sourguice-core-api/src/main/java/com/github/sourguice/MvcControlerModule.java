package com.github.sourguice;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.controller.GivenInstanceGetter;
import com.github.sourguice.controller.GuiceInstanceGetter;
import com.github.sourguice.controller.InstanceGetter;
import com.github.sourguice.conversion.Converter;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.request.ForwardableRequestFactory;
import com.github.sourguice.utils.RedirectServlet;
import com.github.sourguice.value.RequestMethod;
import com.github.sourguice.value.ValueConstants;
import com.github.sourguice.view.ViewRenderer;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;

/**
 * This class is the base guice module class to inherit
 * To configure a MVC module, create a subclass of this class
 * then override {@link #configureControllers()}
 * Then, use the syntax control(pattern).with(controller.class)
 * You can, of course, use the standard guice and guice-servlet commands.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@SuppressWarnings({"static-method", "PMD.TooManyMethods"})
public abstract class MvcControlerModule extends ServletModule {

	/**
	 * This class is needed because API and implementation are not in the same JAR project.
	 * This means that while the module is in the API jar, all implementations are in a seperate jar
	 * that is unknown from the API code.
	 * To connect to the implementation, the API code uses reflexivity to find the implementation class.
	 * This implementation class implements MvcServletModuleHelperProxy that the API module will use
	 * to delegate the actual implementation and bindings.
	 * @author salomon
	 */
	@SuppressWarnings("javadoc")
	static interface MvcControlerModuleHelperProxy {

		public ForwardableRequestFactory getForwardableRequestFactory(HttpServletRequest req, ServletContext context);

		public void configureServlets();

		public void registerControl(String pattern, InstanceGetter<?> controller);

		public void registerConverter(Class<?> type, InstanceGetter<? extends Converter<?>> converter);

		public <T extends Exception> void registerExceptionHandler(Class<? extends T> cls, InstanceGetter<? extends ExceptionHandler<T>> handler);

		public void registerViewRenderer(Pattern pattern, InstanceGetter<? extends ViewRenderer> renderer);
	}

	/**
	 * The helper proxy on which every implementation call will be forwarded
	 * This should be an instance of com.github.sourguice.MvcServletModuleHelperImpl
	 * found by reflexivity
	 */
	protected MvcControlerModuleHelperProxy helper;

	/**
	 * This will check that the implementation jar is actually in the classpath
	 */
	public MvcControlerModule() {
		super();
		try {
			this.helper = (MvcControlerModuleHelperProxy)
						Class
							.forName("com.github.sourguice.MvcControlerModuleHelperImpl")
							.getConstructor(MvcControlerModule.class)
							.newInstance(this);
		}
		catch (ReflectiveOperationException e) {
			throw new UnsupportedOperationException("Cannot find SourGuice Implementation, make sure it is deployed with your application", e);
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
		return this.helper.getForwardableRequestFactory(req, context);
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

	/**
	 * This is the method that guice requires to overrides to configure servlets.
	 * To configure controllers or additional servlets, override {@link #configureControllers()}
	 *
	 * This is where the magic happens ;)
	 * This methods binds all necessary classes and interface to make SourGuice work
	 */
	@Override
	protected final void configureServlets() {
		super.configureServlets();

		this.helper.configureServlets();
	}

	/**
	 * This is the method to override to configure controllers.
	 * In this method, you shall use the syntax control(pattern).with(controller.class)
	 * Refer to the documentation for more informations
	 */
	abstract protected void configureControllers();

	/**
	 * Interface returned by {@link #control(String, String...)} to permit the syntax control(pattern).with(controller.class)
	 *
	 * @param <T> The type of instance to register
	 */
	public abstract class BindBuilder<T> {

		/**
		 * Registers a Guice key
		 *
		 * @param key The key to register
		 */
		@SuppressWarnings("synthetic-access")
		public void with(final Key<? extends T> key) {
			final InstanceGetter<? extends T> getter = new GuiceInstanceGetter<>(key);
			requestInjection(getter);
			register(getter);
		}

		/**
		 * Registers a type to be retrieved with Guice
		 *
		 * @param type The type to register
		 */
		public void with(final Class<? extends T> type) {
			with(Key.get(type));
		}

		/**
		 * Registers a type to be retrieved with Guice
		 *
		 * @param type The type to register
		 */
		public void with(final TypeLiteral<? extends T> type) {
			with(Key.get(type));
		}

		/**
		 * Registers a pre-created instance
		 *
		 * @param instance The object to register
		 */
		public void withInstance(final T instance) {
			final InstanceGetter<? extends T> getter = new GivenInstanceGetter<>(instance);
			register(getter);
		}

		/**
		 * Makes the registration within the corresponding service.
		 *
		 * @param getter The {@link InstanceGetter} to register within the service.
		 */
		abstract protected void register(InstanceGetter<? extends T> getter);
	}

	/**
	 * First method of the syntax control(pattern).with(controller.class)
	 *
	 * @param pattern The pattern to register for the later controller
	 * @param patterns Any additional patterns to register
	 * @return BindBuilder on which with() or withInstance() must be called
	 */
	public final BindBuilder<Object> control(final String pattern, final String... patterns) {
		return new BindBuilder<Object>() {
			@Override protected void register(final InstanceGetter<? extends Object> controller) {
				MvcControlerModule.this.helper.registerControl(pattern, controller);
				for (final String addPattern : patterns) {
					MvcControlerModule.this.helper.registerControl(addPattern, controller);
				}
			}
		};
	}

	/**
	 * Interface returned by {@link MvcControlerModule#redirect(String, String...)} to permit the syntax redirect(pattern).to(path)
	 */
	public static interface RedirectBuilder {
		/**
		 * Second method of the syntax redirect(pattern).to(path)
		 * Associates previously defined pattern to the path
		 *
		 * @param path The path on which redirect
		 */
		@SuppressWarnings("PMD.ShortMethodName")
		public void to(String path);
	}

	/**
	 * First method of the syntax redirect(pattern).to(path)
	 *
	 * @param pattern The pattern to redirect to the later path
	 * @param patterns Any additional patterns to redirect
	 * @return RedirectBuilder on which to() must be called
	 */
	public final RedirectBuilder redirect(final String pattern, final String... patterns) {
		return new RedirectBuilder() {
			@SuppressWarnings({"synthetic-access", "PMD.ShortMethodName"})
			@Override public void to(final String dest) {
				serve(pattern, patterns).with(new RedirectServlet(dest));
			}
		};
	}

	/**
	 * Call this from {@link #configureControllers()} to change the default view renderer.
	 *
	 * @param regex The name regular expression to register to the later view renderer
	 * @param regexs Any additional name regular expression to register
	 * @return BindBuilder on which with() or withInstance() must be called
	 */
	public BindBuilder<ViewRenderer> renderViews(final String regex, final String... regexs) {
		return new BindBuilder<ViewRenderer>() {
			@Override protected void register(final InstanceGetter<? extends ViewRenderer> renderer) {
				MvcControlerModule.this.helper.registerViewRenderer(Pattern.compile(regex), renderer);
				for (final String addRegex : regexs) {
					MvcControlerModule.this.helper.registerViewRenderer(Pattern.compile(addRegex), renderer);
				}
			}
		};
	}

	/**
	 * First method of the syntax convertTo(class).with(converter)
	 *
	 * @param toType The class to convert to using the later converter
	 * @param toTypes Any additional class to convert to
	 * @return BindBuilder on which with() or withInstance() must be called
	 */
	public final BindBuilder<Converter<?>> convertTo(final Class<?> toType, final Class<?>... toTypes) {
		return new BindBuilder<Converter<?>>() {
			@Override protected void register(final InstanceGetter<? extends Converter<?>> converter) {
				MvcControlerModule.this.helper.registerConverter(toType, converter);
				for (final Class<?> addToType : toTypes) {
					MvcControlerModule.this.helper.registerConverter(addToType, converter);
				}
			}
		};
	}

	/**
	 * First method of the syntax handleException(class).with(handler)
	 *
	 * @param exc The exception class to handle using the later handler
	 * @param excs Any additional class to convert to
	 * @return BindBuilder on which with() or withInstance() must be called
	 */
	@SafeVarargs
	public final <T extends Exception> BindBuilder<ExceptionHandler<T>> handleException(final Class<? extends T> exc, final Class<? extends T>... excs) {
		return new BindBuilder<ExceptionHandler<T>>() {
			@Override protected void register(final InstanceGetter<? extends ExceptionHandler<T>> handler) {
				MvcControlerModule.this.helper.registerExceptionHandler(exc, handler);
				for (final Class<? extends T> addExc : excs) {
					MvcControlerModule.this.helper.registerExceptionHandler(addExc, handler);
				}
			}
		};
	}

	/**
	 * Allows implementation proxy class to access the actual binder
	 * This is needed because the protected permission allows the implementation proxy,
	 * which is on the same package as this module class, to access this method.
	 */
	@Override
	@SuppressWarnings("PMD.UselessOverridingMethod")
	protected Binder binder() {
		return super.binder();
	}

	/**
	 * Allows implementation proxy to access the Guice-Servlet syntax serve().with()
	 * This is needed because the protected permission allows the implementation proxy,
	 * which is on the same package as this module class, to access this method.
	 *
	 * @param urlPattern Any Servlet-style pattern. examples: /*, /html/*, *.html, etc.
	 * @param morePatterns Any Servlet-style pattern. examples: /*, /html/*, *.html, etc.
	 * @return The necessary object for the serve().with() DSL
	 */
	@SuppressWarnings("PMD.MethodNamingConventions")
	protected ServletModule.ServletKeyBindingBuilder _serve(final String urlPattern, final String... morePatterns) {
		return serve(urlPattern, morePatterns);
	}

}
