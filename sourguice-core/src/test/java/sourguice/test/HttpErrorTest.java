package sourguice.test;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.controller.HttpError;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.throwable.controller.MvcResponseException;
import com.github.sourguice.throwable.controller.MvcResponseStatusCode;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method"})
public class HttpErrorTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

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
        public void unauthorized() throws MvcResponseStatusCode {
        	throw new MvcResponseStatusCode(401);
        }


        @RequestMapping("/teapot")
        public void teapot() throws MvcResponseException {
        	throw new MvcResponseStatusCode(418, "I'm a teapot");
        }

        @RequestMapping("/gotohell")
        @HttpError(477)
        public String gotohell() {
        	return "Go to hell!";
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

    // ===================== TESTS =====================

    @Test
    public void getForbidden() throws Exception {
        HttpTester request = makeRequest("GET", "/forbidden");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 403;
    }

    @Test
    public void getGone() throws Exception {
        HttpTester request = makeRequest("GET", "/gone");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 409;
    }

    @Test
    public void getUnauthorized() throws Exception {
        HttpTester request = makeRequest("GET", "/unauthorized");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 401;
    }

    @Test
    public void getTeapot() throws Exception {
        HttpTester request = makeRequest("GET", "/teapot");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 418;
    }

    @Test
    public void goToHell() throws Exception {
        HttpTester request = makeRequest("GET", "/gotohell");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 477;
    }

}

