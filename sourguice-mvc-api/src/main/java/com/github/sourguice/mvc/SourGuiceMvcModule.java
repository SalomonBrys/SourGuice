package com.github.sourguice.mvc;

import com.github.sourguice.MultipleBindBuilder;
import com.github.sourguice.SingleBindBuilder;
import com.github.sourguice.mvc.view.ViewRenderer;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Defines all methods needed to configure SourGuice MVC
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface SourGuiceMvcModule {

	/**
	 * Interface returned by {@link SourGuiceMvc#redirect(String, String...)} to permit the syntax redirect(pattern).to(path)
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
	 * First method of the syntax control(pattern).with(controller.class)
	 *
	 * @param pattern The pattern to register for the later controller
	 * @param patterns Any additional patterns to register
	 * @return BindBuilder on which with() or withInstance() must be called
	 */
	public abstract MultipleBindBuilder<Object> control(String pattern, String... patterns);

	/**
	 * First method of the syntax redirect(pattern).to(path)
	 *
	 * @param pattern The pattern to redirect to the later path
	 * @param patterns Any additional patterns to redirect
	 * @return RedirectBuilder on which to() must be called
	 */
	public abstract RedirectBuilder redirect(String pattern, String... patterns);

	/**
	 * First method of the syntax renderViews(pattern).with(viewRenderer)
	 *
	 * @param regex The name regular expression to register to the later view renderer
	 * @param regexs Any additional name regular expression to register
	 * @return BindBuilder on which with() or withInstance() must be called
	 */
	public abstract SingleBindBuilder<ViewRenderer> renderViews(String regex, String... regexs);

	/**
	 * Module to install once you have fully configured SourGuice MVC.
	 * Should be used with {@link AbstractModule#install}: <pre>install(sourGuiceMvc.module())</pre>
	 *
	 * Careful: no other method of the SourGuice object should be called after calling this
	 *
	 * @return The module to install
	 */
	public Module module();
}
