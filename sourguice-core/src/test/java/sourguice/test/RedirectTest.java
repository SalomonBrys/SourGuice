package sourguice.test;

import static org.testng.Assert.*;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.SourGuice;
import com.github.sourguice.annotation.request.Redirects;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.throwable.controller.SGResponseException;
import com.github.sourguice.throwable.controller.SGResponseRedirect;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

@SuppressWarnings({"javadoc", "static-method", "PMD"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class RedirectTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

    	@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

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
        public void exception() throws SGResponseException {
        	throw new SGResponseRedirect("/redir4");
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

    public static class ControllerModule extends ServletModule {
        @Override
        protected void configureServlets() {
        	SourGuice sg = new SourGuice();
            sg.control("/*").with(Controller.class);
            sg.redirect("/defined").to("/go/to/hell");
            install(sg.module());
        }
    }

    @Override
    protected Module module() {
        return new ControllerModule();
    }

    // ===================== TESTS =====================


    public void getStatic() throws Exception {
        HttpTester request = makeRequest("GET", "/static");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 302);
		assertEquals(response.getHeader("location"), "http://tester/redir1");
    }


    public void getDynamic() throws Exception {
        HttpTester request = makeRequest("GET", "/dynamic");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 302);
		assertEquals(response.getHeader("location"), "http://tester/redir2");
    }


    public void getEnhancedStatic() throws Exception {
        HttpTester request = makeRequest("GET", "/enhancedstatic");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 302);
		assertEquals(response.getHeader("location"), "http://tester/redir3");
    }


    public void getException() throws Exception {
        HttpTester request = makeRequest("GET", "/exception");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 302);
		assertEquals(response.getHeader("location"), "http://tester/redir4");
    }


    public void getIgnored() throws Exception {
        HttpTester request = makeRequest("GET", "/ignored");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 302);
		assertEquals(response.getHeader("location"), "http://tester/redir5");
    }


    public void getDefined() throws Exception {
        HttpTester request = makeRequest("GET", "/defined");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 302);
		assertEquals(response.getHeader("location"), "http://tester/go/to/hell");
    }


    public void getNoRedirect() throws Exception {
        HttpTester request = makeRequest("GET", "/noredirect");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
    }

}

