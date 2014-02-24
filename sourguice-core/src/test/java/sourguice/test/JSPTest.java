package sourguice.test;

import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.StaticIgnoreGuiceFilter;
import com.github.sourguice.annotation.controller.ViewSystem;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.View;
import com.github.sourguice.view.Model;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class JSPTest extends TestBase {

    // ===================== CONTROLLERS =====================

    @Singleton
    @ViewSystem(directory = "/WEB-INF")
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

	@Override
	protected void addServletTesterFilter(ServletTester tester) {
		tester.setResourceBase("./src/test/resources");
		tester.addFilter(StaticIgnoreGuiceFilter.class, "/*", 0);
		tester.addServlet(JspServlet.class, "*.jsp");
	}


	public void testIndex() throws Exception {
		HttpTester request = makeRequest("GET", "/Hello.jsp");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("Hello World!");
	}


	public void testHi() throws Exception {
		HttpTester request = makeRequest("GET", "/hi");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("Hi Salomon!");
	}


	public void testUnknown() throws Exception {
		HttpTester request = makeRequest("GET", "/unknown");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 404;
		assert response.getReason().equals("/WEB-INF/unknown.jsp");
	}

}

