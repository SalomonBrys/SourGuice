package com.github.sourguice.ws;

import java.util.HashMap;
import java.util.Map;

import com.github.sourguice.MultipleBindBuilder;
import com.github.sourguice.SourGuiceModule;
import com.github.sourguice.provider.GTPModuleFactory;
import com.github.sourguice.provider.TypedProvider;
import com.github.sourguice.provider.TypedProviderMultipleBindBuilder;
import com.google.inject.servlet.ServletModule;

public class SourGuiceWS extends ServletModule {

	private final SourGuiceModule sourguice;

	private final GTPModuleFactory gtpFactory = new GTPModuleFactory();

	protected Map<String, WSServlet> servlets = new HashMap<>();

	public SourGuiceWS(final SourGuiceModule sourguice) {
		super();
		this.sourguice = sourguice;
	}

	@Override
	protected void configureServlets() {
		super.configureServlets();

		for (final Map.Entry<String, WSServlet> entry : this.servlets.entrySet()) {
			serve(entry.getKey()).with(entry.getValue());
			entry.getValue().initialize();
		}

		this.gtpFactory.requestInjection(binder());
		install(this.sourguice.module());
	}

	public static interface ServiceBuilder extends MultipleBindBuilder<Object> {
		public ServiceBuilder defaultVersion(double version);
	}

	private class ServiceBuilderImpl extends TypedProviderMultipleBindBuilder<Object> implements ServiceBuilder {

		private WSServlet wsServlet;

		public ServiceBuilderImpl(final GTPModuleFactory gtpFactory, final String pattern) {
			super(gtpFactory);
			this.wsServlet = SourGuiceWS.this.servlets.get(pattern);
			if (this.wsServlet == null) {
				this.wsServlet = new WSServlet();
				SourGuiceWS.this.servlets.put(pattern, this.wsServlet);
			}
		}

		@Override
		public ServiceBuilder defaultVersion(final double version) {
			this.wsServlet.description.defaultVersion = version;
			return this;
		}

		@Override
		protected void register(final TypedProvider<? extends Object> controller) {
			this.wsServlet.add(controller);
		}
	}

	public ServiceBuilder service(final String pattern) {
		return new ServiceBuilderImpl(this.gtpFactory, pattern);
	}

}
