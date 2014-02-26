package com.github.sourguice.controller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.controller.Callable;
import com.github.sourguice.annotation.controller.ViewRendered;
import com.github.sourguice.annotation.controller.ViewRenderedWith;
import com.github.sourguice.annotation.controller.ViewDirectory;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.View;
import com.github.sourguice.utils.Annotations;
import com.github.sourguice.view.Model;
import com.github.sourguice.view.NoViewRendererException;
import com.github.sourguice.view.ViewRenderer;
import com.github.sourguice.view.ViewRendererService;
import com.google.inject.Injector;
import com.google.inject.Key;

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
public final class ControllerHandler<T> {
	/**
	 * The Class object of the controller class to handle
	 */
	private InstanceGetter<T> ig;

	/**
	 * List of available invocations for this controller
	 */
	private ArrayList<MvcInvocation> invocations = new ArrayList<>();

	private String viewDirectory = "";

	private ViewRenderedWith[] viewRenderers = {};

	private Map<String, InstanceGetter<? extends ViewRenderer>> rendererCache = new HashMap<>();

	/**
	 * @param clazz The controller class to handle
	 */
	public ControllerHandler(InstanceGetter<T> ig) {
		this.ig = ig;

		ViewDirectory vdAnno = Annotations.GetOneTreeRecursive(ViewDirectory.class, ig.getTypeLiteral().getRawType());
		if (vdAnno != null)
			viewDirectory = vdAnno.value();

		ViewRendered rdAnno = Annotations.GetOneTreeRecursive(ViewRendered.class, ig.getTypeLiteral().getRawType());
		if (rdAnno != null)
			viewRenderers = rdAnno.value();
		else {
			ViewRenderedWith rdwAnno = Annotations.GetOneTreeRecursive(ViewRenderedWith.class, ig.getTypeLiteral().getRawType());
			if (rdwAnno != null)
				viewRenderers = new ViewRenderedWith[] { rdwAnno };
		}

		for (Method method : ig.getTypeLiteral().getRawType().getMethods())
			if (Annotations.GetOneTreeRecursive(Callable.class, method) != null)
				invocations.add(new MvcInvocation(this, Annotations.GetOneRecursive(RequestMapping.class, method.getAnnotations()), ig, method));
	}

	/**
	 * Gets the best invocation of all the invocable methods of this controller for this request
	 *
	 * @param req The request to get invocation for
	 * @return All infos opf the best invocation
	 */
	public @CheckForNull ControllerInvocationInfos getBestInvocation(HttpServletRequest req) {
		// Get the best invocation for the given request
		ControllerInvocationInfos infos = null;
		for (MvcInvocation invocation : invocations)
			infos = ControllerInvocationInfos.GetBest(infos, invocation.canServe(req));

		//TODO: There should be no reflexivity at call-time !
		// If found (not null) gather invocation informations from annotations
		if (infos != null) {
			View vAnno = infos.invocation.getView();
			if (vAnno != null)
				infos.defaultView = vAnno.value();
		}
		return infos;
	}

	/**
	 * @return All invocations that were found on this controller class
	 */
	public ArrayList<MvcInvocation> getInvocations() {
		return invocations;
	}

	protected InstanceGetter<T> getInstanceGetter() {
		return ig;
	}

	public void renderView(String view, Injector injector) throws NoViewRendererException, Throwable {

		// If a view directory were set, prefixes the view with it
		if (!view.startsWith("/") && !viewDirectory.isEmpty())
			view = viewDirectory + "/" + view;

		// Maybe it has already been set, so we look for it
		InstanceGetter<? extends ViewRenderer> renderer = rendererCache.get(view);

		// If it has not been set, we need to set it only once, so we get a synchronized lock
		if (renderer == null)
			synchronized (rendererCache) {
				// Maybe it has been set while we were waiting for the lock, so we check again
				renderer = rendererCache.get(view);
				if (renderer == null) {
					// Gets the view renderer either from the controller class or from Guice
					for (ViewRenderedWith rdw : viewRenderers)
						if (Pattern.matches(rdw.regex(), view)) {
							renderer = new GuiceInstanceGetter<>(Key.get(rdw.renderer()));
							injector.injectMembers(renderer);
							break ;
						}
					if (renderer == null)
						renderer = injector.getInstance(ViewRendererService.class).getRenderer(view);
					rendererCache.put(view, renderer);
				}
			}

		System.out.println("Rendering " + view + " with " + renderer);
		renderer.getInstance().render(view, injector.getInstance(Model.class).asMap());
	}
}
