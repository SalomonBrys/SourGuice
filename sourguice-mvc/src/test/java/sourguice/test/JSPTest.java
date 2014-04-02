package sourguice.test;

import static org.testng.Assert.assertEquals;

import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.github.sourguice.SourGuice;
import com.github.sourguice.StaticAwareGuiceFilter;
import com.github.sourguice.mvc.SourGuiceMvc;
import com.github.sourguice.mvc.annotation.controller.ViewDirectory;
import com.github.sourguice.mvc.annotation.request.RequestMapping;
import com.github.sourguice.mvc.annotation.request.View;
import com.github.sourguice.mvc.view.Model;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

@SuppressWarnings({"javadoc", "static-method", "PMD"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class JSPTest extends TestBase {

    // ===================== TRAVIS SKIP =====================

	@SuppressWarnings("serial")
	private static class TravisSkipException extends SkipException {
		public TravisSkipException() {
			super("Travis does not supports JSP compilation");
			reduceStackTrace();
		}
	}

	private boolean travisSkip = false;

	@BeforeMethod
	public void skipInTravis() {
		String travis = System.getenv("TRAVIS");
		this.travisSkip = travis != null && travis.equalsIgnoreCase("true");
	}

    // ===================== CONTROLLERS =====================

    @Singleton
    @ViewDirectory("/WEB-INF")
    public static class Controller {

    	@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

		@View("Hi.jsp")
    	@RequestMapping("/hi")
    	public void hi(Model model) {
    		model.put("name", "Salomon");
    	}

    	@RequestMapping("/unknown")
    	@View
    	public String unknown() {
    		return "unknown.jsp";
    	}
    }

    // ===================== MODULE =====================

    public static class ControllerModule extends ServletModule {
		@Override
        protected void configureServlets() {
        	SourGuiceMvc mvc = new SourGuiceMvc(new SourGuice());
        	mvc.control("/*").with(Controller.class);
            install(mvc.module());
        }
    }

    @Override
    protected Module module() {
        return new ControllerModule();
    }

    // ===================== SERVLET CONFIG =====================

    @Override
	protected void addServletTesterFilter(ServletTester tester) {
		tester.setResourceBase("./src/test/resources");
		tester.addFilter(StaticAwareGuiceFilter.class, "/*", 0);
		tester.addServlet(JspServlet.class, "*.jsp");
	}

    // ===================== TESTS =====================

	public void testIndex() throws Exception {
		if (this.travisSkip)
			throw new TravisSkipException();

		HttpTester request = makeRequest("GET", "/Hello.jsp");
		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "\n\nHello World!\n");
	}


	public void testHi() throws Exception {
		if (this.travisSkip)
			throw new TravisSkipException();

		HttpTester request = makeRequest("GET", "/hi");
		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "Hi Salomon!");
	}


	public void testUnknown() throws Exception {
		if (this.travisSkip)
			throw new TravisSkipException();

		HttpTester request = makeRequest("GET", "/unknown");
		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 404);
		assertEquals(response.getReason(), "/WEB-INF/unknown.jsp");
	}

}

