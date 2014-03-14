package com.github.sourguice;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.MvcControlerModule.BindBuilder;
import com.github.sourguice.MvcControlerModule.MvcControlerModuleHelperProxy;
import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.call.MvcCaller;
import com.github.sourguice.call.impl.MvcCallerImpl;
import com.github.sourguice.call.impl.PathVariablesProvider;
import com.github.sourguice.controller.ControllerHandlersRepository;
import com.github.sourguice.controller.ControllerInterceptor;
import com.github.sourguice.controller.ControllersServlet;
import com.github.sourguice.controller.InstanceGetter;
import com.github.sourguice.controller.InterceptWithMatcher;
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
import com.github.sourguice.exception.def.MVCResponseExceptionHandler;
import com.github.sourguice.exception.impl.ExceptionServiceImpl;
import com.github.sourguice.request.ForwardableRequestFactory;
import com.github.sourguice.request.wrapper.GuiceForwardHttpRequest;
import com.github.sourguice.throwable.controller.MvcResponseException;
import com.github.sourguice.throwable.service.exception.UnreachableExceptionHandlerException;
import com.github.sourguice.utils.MVCCallInterceptSetter;
import com.github.sourguice.view.Model;
import com.github.sourguice.view.ViewRenderer;
import com.github.sourguice.view.ViewRendererService;
import com.github.sourguice.view.def.JSPViewRenderer;
import com.github.sourguice.view.impl.ViewRendererServiceImpl;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletScopes;

/**
 * This is used by {@link MvcControlerModule} to actually bind the implementations of SourGuices classes
 * This is needed because SourGuice builds two different jars : one for API and one for implementation
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public class MvcControlerModuleHelperImpl implements MvcControlerModuleHelperProxy {

	/**
	 * The list of registered path and their corresponding controllers
	 * The purpose of this is to ensure that a path is handled by one controller,
	 * even if this path has been registered multiple times
	 */
	private @CheckForNull Map<String, ControllersServlet> servlets = null;

	/**
	 * Contains all ControllerHandlers
	 * This is to make sure that there will be one and only one ControllerHandler for each controller class
	 */
	private final ControllerHandlersRepository repository = new ControllerHandlersRepository();

	/**
	 * The actual module that ws subclassed to bind controllers
	 */
	protected MvcControlerModule module;

	/**
	/**
	 * Conversion service
	 */
	private final ConversionServiceImpl conversionService = new ConversionServiceImpl();

	/**
	 * Exception service
	 */
	private final ExceptionServiceImpl exceptionService = new ExceptionServiceImpl();

	/**
	 * ViewRenderer service
	 */
	private final ViewRendererServiceImpl rendererService = new ViewRendererServiceImpl();

	/**
	 * Constructor, used by {@link MvcControlerModule}.
	 * @param module The module itself, so this helper will access to Guice binding methods
	 */
	public MvcControlerModuleHelperImpl(final MvcControlerModule module) {
		super();
		this.module = module;
	}

	@Override
	public ForwardableRequestFactory getForwardableRequestFactory(final HttpServletRequest req, final ServletContext context) {
		return new GuiceForwardHttpRequest(req, context);
	}

	@Override
	@SuppressWarnings({"PMD.AvoidUsingShortType", "PMD.AvoidThrowingRawExceptionTypes"})
	public final void configureServlets() {

		// Registers default converters
		this.module.convertTo(boolean.class, Boolean.class).withInstance(new BooleanConverter());
		this.module.convertTo(short.class, Short.class).withInstance(new ShortConverter());
		this.module.convertTo(int.class, Integer.class).withInstance(new IntegerConverter());
		this.module.convertTo(long.class, Long.class).withInstance(new LongConverter());
		this.module.convertTo(float.class, Float.class).withInstance(new FloatConverter());
		this.module.convertTo(double.class, Double.class).withInstance(new DoubleConverter());
		this.module.convertTo(Enum.class).withInstance(new EnumConverter());
		this.module.convertTo(String.class).withInstance(new StringConverter());

		// Registers default exception handlers
		try {
			this.module.handleException(MvcResponseException.class).withInstance(new MVCResponseExceptionHandler());
		}
		catch (UnreachableExceptionHandlerException e) {
			throw new RuntimeException(e);
		}

		// Registers default view renderers
		this.module.renderViews(".*\\.jsp").with(JSPViewRenderer.class);

		// Creates servlet map to be later filled by configureControllers()
		this.servlets = new HashMap<>();

		// Asks for controller registration by subclass
		// This will fill the servlets map
		this.module.configureControllers();

		// Binds the services
		this.module.binder().bind(ConversionService.class).toInstance(this.conversionService);
		this.module.binder().bind(ExceptionService.class).toInstance(this.exceptionService);
		this.module.binder().bind(ViewRendererService.class).toInstance(this.rendererService);

		// Binds view related classes
		this.module.binder().bind(Model.class).in(ServletScopes.REQUEST);

		// Binds method calling related classes
		this.module.binder().bind(MvcCaller.class).to(MvcCallerImpl.class).in(RequestScoped.class);
		this.module.binder().bind(new TypeLiteral<Map<String, String>>() {/**/}).annotatedWith(PathVariablesMap.class).toProvider(PathVariablesProvider.class).in(RequestScoped.class);

		// Creates a controllerHandler repository and registers it in guice
		// We create it because we need to handle it directly in this method
		this.module.binder().bind(ControllerHandlersRepository.class).toInstance(this.repository);

		// Binds Intercept
		this.module.binder().bind(MVCCallInterceptSetter.class);

		// Binds interceptors
		final ControllerInterceptor interceptor = new ControllerInterceptor();
		this.module.binder().requestInjection(interceptor);
		this.module.binder().bindInterceptor(Matchers.any(), new InterceptWithMatcher(), interceptor);
		this.module.binder().bindInterceptor(new InterceptWithMatcher(), Matchers.any(), interceptor);

		assert this.servlets != null;
		final Map<String, ControllersServlet> allServlets = this.servlets;
		// Loops through all registered patterns and their corresponding ControllerHandler.
		// Registers each couple in Guice.
		for (final String pattern : allServlets.keySet()) {
			final ControllersServlet servlet = allServlets.get(pattern);
			this.module.binder().requestInjection(servlet);
			this.module._serve(pattern).with(servlet);
		}

		// Sets null to the servlets variable so any further call to control().with() will raise a NullPointerException
		this.servlets = null;
	}

	/**
	 * Registers a pattern to a controller class
	 * This is called by {@link BindBuilder#with(Class)}
	 *
	 * @param pattern The pattern on which to register to controller
	 * @param controller The controller class to register
	 */
	@Override
	public void registerControl(final String pattern, final InstanceGetter<?> controller) {
		// Registers all filters that are declared by the @FilterThrough annotation of this class and of all its parents
		final Map<String, String> initParams = new HashMap<>();
		initParams.put("pattern", pattern);

		// Creates a controller servlet for this pattern or gets it if this pattern has already been registered
		ControllersServlet servlet;
		assert this.servlets != null;
		if (this.servlets.containsKey(pattern)) {
			servlet = this.servlets.get(pattern);
		}
		else {
			servlet = new ControllersServlet();
			this.servlets.put(pattern, servlet);
		}

		// Registers a controller handler into the controller servlet
		// The handler is retrived from the repository to avoid creating two handlers for the same controller class
		servlet.addController(this.repository.get(controller));
	}

	@Override
	public void registerViewRenderer(final Pattern pattern, final InstanceGetter<? extends ViewRenderer> renderer) {
		this.rendererService.register(pattern, renderer);
	}

	@Override
	public void registerConverter(final Class<?> type, final InstanceGetter<? extends Converter<?>> converter) {
		this.conversionService.register(type, converter);
	}

	@Override
	public <T extends Exception> void registerExceptionHandler(final Class<? extends T> cls, final InstanceGetter<? extends ExceptionHandler<T>> handler) {
		this.exceptionService.register(cls, handler);
	}

}
