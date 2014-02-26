package sourguice.test;

import static org.testng.Assert.assertEquals;

import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.StaticIgnoreGuiceFilter;
import com.github.sourguice.annotation.controller.ViewDirectory;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.View;
import com.github.sourguice.view.Model;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method"})
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
		@View("Hi.jsp")
    	@RequestMapping("/hi")
    	public void hi(Model model) {
    		model.put("name", "Salomon");
    	}

    	@RequestMapping("/unknown")
    	public String unknown() {
    		return "unknown.jsp";
    	}
    }

    // ===================== MODULE =====================

    public static class ControllerModule extends MvcControlerModule {
        @Override
        protected void configureControllers() {
            control("/*").with(Controller.class);
        }
    }

    @Override
    protected MvcControlerModule module() {
        return new ControllerModule();
    }

    // ===================== SERVLET CONFIG =====================

    @Override
	protected void addServletTesterFilter(ServletTester tester) {
		tester.setResourceBase("./src/test/resources");
		tester.addFilter(StaticIgnoreGuiceFilter.class, "/*", 0);
		tester.addServlet(JspServlet.class, "*.jsp");
	}

    // ===================== TESTS =====================

	public void testIndex() throws Exception {
		if (travisSkip)
			throw new TravisSkipException();

		HttpTester request = makeRequest("GET", "/Hello.jsp");
		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "Hello World!");
	}


	public void testHi() throws Exception {
		if (travisSkip)
			throw new TravisSkipException();

		HttpTester request = makeRequest("GET", "/hi");
		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "Hi Salomon!");
	}


	public void testUnknown() throws Exception {
		if (travisSkip)
			throw new TravisSkipException();

		HttpTester request = makeRequest("GET", "/unknown");
		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 404);
		assertEquals(response.getReason(), "/WEB-INF/unknown.jsp");
	}

}

