package sourguice.test.mvc;

import static org.testng.Assert.*;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.SourGuice;
import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.conversion.Converter;
import com.github.sourguice.mvc.SourGuiceMvc;
import com.github.sourguice.mvc.annotation.request.RequestMapping;
import com.github.sourguice.mvc.annotation.request.Writes;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;

@SuppressWarnings({ "javadoc", "static-method", "PMD" })
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class CustomConvertorTest extends TestBase {

    // ===================== POJOS =====================

	public static interface HasAName {
		public String getName();
	}

	public static class Man implements HasAName {
		public String name;
		public Man(String name) { this.name = name; }
		@Override public String getName() { return this.name; }
	}

	public static class Woman implements HasAName {
		public String name;
		public Woman(String name) { this.name = name; }
		@Override public String getName() { return this.name; }
	}

	public static class Child extends Woman {
		public Child(String name) { super("child-of-" + name); }
	}

    // ===================== CONVERTERS =====================

	public static class ManConverter implements Converter<Man> {
		@Override public Man get(TypeLiteral<? extends Man> clazz, String arg) {
			return new Man(arg);
		}
	}

	public static class ChildConverter implements Converter<Child> {
		@Override public Child get(TypeLiteral<? extends Child> clazz, String arg) {
			return new Child(arg);
		}
	}

	// ===================== CONTROLLER =====================

	@Singleton
	public static class Controller {

		@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

		@RequestMapping("/name")
		@Writes
		public String name(@RequestParam("var") HasAName n) {
			return n.getClass().getSimpleName() + ":" + n.getName();
		}
	}

	// ===================== MODULE =====================

    public static class ControllerModule extends ServletModule {
        @Override
        protected void configureServlets() {
        	SourGuiceMvc mvc = new SourGuiceMvc(new SourGuice());
        	mvc.control("/*").with(Controller.class);
        	mvc.convertTo(Man.class).withInstance(new ManConverter());
        	mvc.convertTo(Child.class).with(ChildConverter.class);
            install(mvc.module());
		}
	}

	@Override
    protected Module module() {
		return new ControllerModule();
	}

	// ===================== TESTS =====================


	public void getName() throws Exception {
		HttpTester request = makeRequest("GET", "/name?var=Salomon");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "Man:Salomon");
	}

}
