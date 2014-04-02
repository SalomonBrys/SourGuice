package com.github.sourguice.mvc;

import com.github.sourguice.SourGuice;
import com.github.sourguice.mvc.view.ViewRenderer;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

/**
 * This class is where you can configure your MVC bindings
 * To configure a SourGuice MVC module, create a usual {@link ServletModule}
 * in its {@link ServletModule#configureServlets} method,
 * create a SourGuice object, configure it, then install it's module.
 *
 <pre>
	public static class DemoModule extends ServletModule {
		@Override
		protected void configureServlets() {
			SourGuice sg = new SourGuice();
			sg.control("/*", MyController.class);
			install(sg.module);
		}
	}
 </pre>
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public class SourGuiceMvc extends SourGuice implements SourGuiceMvcModule {

	/**
	 * The helper proxy on which every implementation call will be forwarded
	 * Found by reflexivity in the core jar.
	 */
	private SourGuiceMvcModule implementation;

	/**
	 * This will check that the implementation jar is actually in the classpath
	 *
	 * @param sourGuice The SourGuice core implementation
	 */
	public SourGuiceMvc(final SourGuice sourGuice) {
		super(sourGuice);
		try {
			this.implementation = (SourGuiceMvcModule)
					Class
						.forName("com.github.sourguice.mvc.SourGuiceMvcModuleImpl")
						.getConstructor(SourGuice.class)
						.newInstance(sourGuice);
		}
		catch (ReflectiveOperationException e) {
			throw new UnsupportedOperationException("Cannot find SourGuice Implementation, make sure it is deployed with your application", e);
		}
	}

	@Override
	public final BindBuilder<Object> control(final String pattern, final String... patterns) {
		return this.implementation.control(pattern, patterns);
	}

	@Override
	public final RedirectBuilder redirect(final String pattern, final String... patterns) {
		return this.implementation.redirect(pattern, patterns);
	}

	@Override
	public final BindBuilder<ViewRenderer> renderViews(final String regex, final String... regexs) {
		return this.implementation.renderViews(regex, regexs);
	}

	@Override
	public Module module() {
		return this.implementation.module();
	}
}
