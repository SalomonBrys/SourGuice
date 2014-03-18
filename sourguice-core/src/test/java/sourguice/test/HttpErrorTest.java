package sourguice.test;

import static org.testng.Assert.*;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.SourGuiceControlerModule;
import com.github.sourguice.annotation.controller.HttpError;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.throwable.controller.SGResponseException;
import com.github.sourguice.throwable.controller.SGResponseStatusCode;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method", "PMD"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class HttpErrorTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

    	@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

        @RequestMapping("/forbidden")
        @HttpError(403)
        public void forbidden() {
        	/* */
        }

        @RequestMapping("/gone")
        @HttpError
        public int gone() {
        	return 409;
        }

        @RequestMapping("/unauthorized")
        public void unauthorized() throws SGResponseStatusCode {
        	throw new SGResponseStatusCode(401);
        }


        @RequestMapping("/teapot")
        public void teapot() throws SGResponseException {
        	throw new SGResponseStatusCode(418, "I'm a teapot");
        }

        @RequestMapping("/gotohell")
        @HttpError(477)
        public String gotohell() {
        	return "Go to hell!";
        }

    }

    // ===================== MODULE =====================

    public static class ControllerModule extends SourGuiceControlerModule {
        @Override
        protected void configureControllers() {
            control("/*").with(Controller.class);
        }
    }

    @Override
    protected SourGuiceControlerModule module() {
        return new ControllerModule();
    }

    // ===================== TESTS =====================


    public void getForbidden() throws Exception {
        HttpTester request = makeRequest("GET", "/forbidden");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 403);
    }


    public void getGone() throws Exception {
        HttpTester request = makeRequest("GET", "/gone");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 409);
    }


    public void getUnauthorized() throws Exception {
        HttpTester request = makeRequest("GET", "/unauthorized");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 401);
    }


    public void getTeapot() throws Exception {
        HttpTester request = makeRequest("GET", "/teapot");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 418);
    }


    public void goToHell() throws Exception {
        HttpTester request = makeRequest("GET", "/gotohell");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 477);
    }

}

