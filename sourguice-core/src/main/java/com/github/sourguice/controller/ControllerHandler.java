package com.github.sourguice.controller;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.controller.Callable;
import com.github.sourguice.annotation.controller.ViewSystem;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.View;
import com.github.sourguice.utils.Annotations;

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

	/**
	 * The view system, declared directly on the controller using {@link ViewSystem}
	 */
	private @CheckForNull ViewSystem viewSystem = null;

	/**
	 * @param clazz The controller class to handle
	 */
	public ControllerHandler(InstanceGetter<T> ig) {
		this.ig = ig;

		viewSystem = Annotations.GetOneTreeRecursive(ViewSystem.class, ig.getTypeLiteral().getRawType());

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
			View vAnno = Annotations.GetOneTreeRecursive(View.class, infos.invocation.getMethod());
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

	public @CheckForNull ViewSystem getViewSystem() {
		return viewSystem;
	}
}
