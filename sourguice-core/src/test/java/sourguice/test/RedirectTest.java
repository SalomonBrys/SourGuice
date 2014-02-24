package sourguice.test;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.controller.Redirects;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.throwable.controller.MvcResponseException;
import com.github.sourguice.throwable.controller.MvcResponseRedirect;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class RedirectTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

        @RequestMapping("/static")
        @Redirects("/redir1")
        public void _static() {
        	/* */
        }

        @RequestMapping("/dynamic")
        @Redirects
        public String dynamic() {
        	return "/redir2";
        }

        @RequestMapping("/enhancedstatic")
        @Redirects("/redir{}")
        public int enhancedstatic() {
        	return 3;
        }

        @RequestMapping("/exception")
        public void exception() throws MvcResponseException {
        	throw new MvcResponseRedirect("/redir4");
        }

        @RequestMapping("/ignored")
        @Redirects("/redir5")
        public String ignored() {
        	return "/coucou";
        }

        @RequestMapping("/noredirect")
        @Redirects
        public void noredirect() {
        	/* */
        }

    }

    // ===================== MODULE =====================

    public static class ControllerModule extends MvcControlerModule {
        @Override
        protected void configureControllers() {
            control("/*").with(Controller.class);
            redirect("/defined").to("/go/to/hell");
        }
    }

    @Override
    protected MvcControlerModule module() {
        return new ControllerModule();
    }

    // ===================== TESTS =====================


    public void getStatic() throws Exception {
        HttpTester request = makeRequest("GET", "/static");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 302;
        assert response.getHeader("location").equals("http://tester/redir1");
    }


    public void getDynamic() throws Exception {
        HttpTester request = makeRequest("GET", "/dynamic");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 302;
        assert response.getHeader("location").equals("http://tester/redir2");
    }


    public void getEnhancedStatic() throws Exception {
        HttpTester request = makeRequest("GET", "/enhancedstatic");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 302;
        assert response.getHeader("location").equals("http://tester/redir3");
    }


    public void getException() throws Exception {
        HttpTester request = makeRequest("GET", "/exception");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 302;
        assert response.getHeader("location").equals("http://tester/redir4");
    }


    public void getIgnored() throws Exception {
        HttpTester request = makeRequest("GET", "/ignored");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 302;
        assert response.getHeader("location").equals("http://tester/redir5");
    }


    public void getDefined() throws Exception {
        HttpTester request = makeRequest("GET", "/defined");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 302;
        assert response.getHeader("location").equals("http://tester/go/to/hell");
    }


    public void getNoRedirect() throws Exception {
        HttpTester request = makeRequest("GET", "/noredirect");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
    }

}

