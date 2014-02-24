package sourguice.test;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.conversion.Converter;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

@SuppressWarnings({ "javadoc", "static-method" })
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class CustomConvertorTest extends TestBase {

    // ===================== POJOS =====================

	public static interface HasAName {
		public String getName();
	}

	public static class Man implements HasAName {
		public String name;
		public Man(String name) { this.name = name; }
		@Override public String getName() { return name; }
	}

	public static class Woman implements HasAName {
		public String name;
		public Woman(String name) { this.name = name; }
		@Override public String getName() { return name; }
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
		@RequestMapping("/name")
		@Writes
		public String name(@RequestParam("var") HasAName n) {
			return n.getClass().getSimpleName() + ":" + n.getName();
		}
	}

	// ===================== MODULE =====================

	public static class ControllerModule extends MvcControlerModule {
		@Override
		protected void configureControllers() {
			control("/*").with(Controller.class);
			convertTo(Man.class).withInstance(new ManConverter());
			convertTo(Child.class).with(ChildConverter.class);
		}
	}

	@Override
	protected MvcControlerModule module() {
		return new ControllerModule();
	}

	// ===================== TESTS =====================

	
	public void getName() throws Exception {
		HttpTester request = makeRequest("GET", "/name?var=Salomon");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("Man:Salomon");
	}

}
