package com.github.sourguice.controller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.controller.Callable;
import com.github.sourguice.annotation.controller.ViewDirectory;
import com.github.sourguice.annotation.controller.ViewRendered;
import com.github.sourguice.annotation.controller.ViewRenderedWith;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.View;
import com.github.sourguice.utils.Annotations;
import com.github.sourguice.view.Model;
import com.github.sourguice.view.NoViewRendererException;
import com.github.sourguice.view.ViewRenderer;
import com.github.sourguice.view.ViewRendererService;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Handles a controller class.
 * A controller class can be any class that is declared in {@link MvcControlerModule} configureControllers method
 * using the syntax control(pattern).with(controller.class)
 * This class is responsible for creating and managing all possible invocations for the given class
 *   (which are all methods annotated with @{@link RequestMapping})
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 * @param <T> The controller class to handle
 */
public final class ControllerHandler<T> implements InstanceGetter<T> {
	/**
	 * The Class object of the controller class to handle
	 */
	private final InstanceGetter<T> controller;

	/**
	 * List of available invocations for this controller
	 */
	private final List<MvcInvocation> invocations = new ArrayList<>();

	private String viewDirectory = "";

	private ViewRenderedWith[] viewRenderers = {};

	private final Map<String, InstanceGetter<? extends ViewRenderer>> rendererCache = new HashMap<>();

	/**
	 * @param clazz The controller class to handle
	 */
	public ControllerHandler(final InstanceGetter<T> controller) {
		this.controller = controller;

		final ViewDirectory vdAnno = Annotations.GetOneTreeRecursive(ViewDirectory.class, controller.getTypeLiteral().getRawType());
		if (vdAnno != null) {
			this.viewDirectory = vdAnno.value();
		}

		final ViewRendered rdAnno = Annotations.GetOneTreeRecursive(ViewRendered.class, controller.getTypeLiteral().getRawType());
		if (rdAnno == null) {
			final ViewRenderedWith rdwAnno = Annotations.GetOneTreeRecursive(ViewRenderedWith.class, controller.getTypeLiteral().getRawType());
			if (rdwAnno != null) {
				this.viewRenderers = new ViewRenderedWith[] { rdwAnno };
			}
		}
		else {
			this.viewRenderers = rdAnno.value();
		}

		for (final Method method : controller.getTypeLiteral().getRawType().getMethods()) {
			if (Annotations.GetOneTreeRecursive(Callable.class, method) != null) {
				this.invocations.add(new MvcInvocation(this, Annotations.GetOneRecursive(RequestMapping.class, method.getAnnotations()), method));
			}
		}
	}

	/**
	 * Gets the best invocation of all the invocable methods of this controller for this request
	 *
	 * @param req The request to get invocation for
	 * @return All infos opf the best invocation
	 */
	public @CheckForNull ControllerInvocationInfos getBestInvocation(final HttpServletRequest req) {
		// Get the best invocation for the given request
		ControllerInvocationInfos infos = null;
		for (final MvcInvocation invocation : this.invocations) {
			infos = ControllerInvocationInfos.getBest(infos, invocation.canServe(req));
		}

		// If found (not null) gather invocation informations from annotations
		if (infos != null) {
			final View vAnno = infos.invocation.getView();
			if (vAnno != null) {
				infos.defaultView = vAnno.value();
			}
		}
		return infos;
	}

	/**
	 * @return All invocations that were found on this controller class
	 */
	public List<MvcInvocation> getInvocations() {
		return this.invocations;
	}

	public void renderView(String view, final Injector injector) throws NoViewRendererException, Throwable {

		// If a view directory were set, prefixes the view with it
		if (view.charAt(0) != '/' && !this.viewDirectory.isEmpty()) {
			view = this.viewDirectory + "/" + view;
		}

		// Maybe it has already been set, so we look for it
		InstanceGetter<? extends ViewRenderer> renderer = this.rendererCache.get(view);

		// If it has not been set, we need to set it only once, so we get a synchronized lock
		if (renderer == null) {
			synchronized (this.rendererCache) {
				// Maybe it has been set while we were waiting for the lock, so we check again
				renderer = this.rendererCache.get(view);
				if (renderer == null) {
					// Gets the view renderer either from the controller class or from Guice
					for (final ViewRenderedWith rdw : this.viewRenderers) {
						if (Pattern.matches(rdw.regex(), view)) {
							renderer = new GuiceInstanceGetter<>(Key.get(rdw.renderer()));
							injector.injectMembers(renderer);
							break ;
						}
					}
					if (renderer == null) {
						renderer = injector.getInstance(ViewRendererService.class).getRenderer(view);
					}
					this.rendererCache.put(view, renderer);
				}
			}
		}

		renderer.getInstance().render(view, injector.getInstance(Model.class).asMap());
	}

	@Override
	public T getInstance() {
		return this.controller.getInstance();
	}

	@Override
	public TypeLiteral<T> getTypeLiteral() {
		return this.controller.getTypeLiteral();
	}
}
