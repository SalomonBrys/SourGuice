package sourguice.test.mvc;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.SourGuice;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.mvc.SourGuiceMvc;
import com.github.sourguice.mvc.annotation.request.RequestMapping;
import com.github.sourguice.throwable.exception.UnreachableExceptionHandlerException;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

@SuppressWarnings({"javadoc", "static-method", "PMD"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class ExceptionTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

    	@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

    	@RequestMapping("/exception")
    	public void exception() throws CustomException {
    		throw new CustomException("Choucroute");
    	}

    	@RequestMapping("/other")
    	public void other() throws OtherException {
    		throw new OtherException("Choucroute");
    	}
    }

    // ===================== EXCEPTIONS =====================

    @SuppressWarnings("serial")
	public static class CustomException extends Exception {
		public CustomException(String message) {
			super(message);
		}
    }

    @SuppressWarnings("serial")
	public static class SubCustomException extends CustomException {
		public SubCustomException(String message) {
			super(message);
		}
    }

    @SuppressWarnings("serial")
	public static class OtherException extends Exception {
		public OtherException(String message) {
			super(message);
		}
    }

    public static class CustomExceptionHandler implements ExceptionHandler<CustomException> {
    	Provider<HttpServletResponse> responseProvider;

    	@Inject
		public CustomExceptionHandler(Provider<HttpServletResponse> responseProvider) {
			super();
			this.responseProvider = responseProvider;
		}

		@Override public boolean handle(CustomException exception) throws IOException {
			this.responseProvider.get().getWriter().write("Boom:" + exception.getMessage() + "!");
			return true;
		}
    }

    public static class SubCustomExceptionHandler implements ExceptionHandler<SubCustomException> {
    	Provider<HttpServletResponse> responseProvider;

    	@Inject
		public SubCustomExceptionHandler(Provider<HttpServletResponse> responseProvider) {
			super();
			this.responseProvider = responseProvider;
		}

		@Override public boolean handle(SubCustomException exception) throws IOException {
			this.responseProvider.get().getWriter().write("SubBoom:" + exception.getMessage() + "!");
			return true;
		}
    }

    // ===================== MODULE =====================

    public static class ControllerModule extends ServletModule {

    	public static boolean exceptionCaught = false;

		@Override
        protected void configureServlets() {
        	SourGuiceMvc mvc = new SourGuiceMvc(new SourGuice());
        	mvc.control("/*").with(Controller.class);
            try {
            	mvc.handleException(CustomException.class).with(CustomExceptionHandler.class);
            }
            catch (UnreachableExceptionHandlerException e) {
            	throw new RuntimeException(e);
            }

            try {
            	mvc.handleException(SubCustomException.class).with(SubCustomExceptionHandler.class);
            }
            catch (UnreachableExceptionHandlerException e) {
            	exceptionCaught = true;
            }
            install(mvc.module());
        }
    }

    @Override
    protected Module module() {
        return new ControllerModule();
    }

    // ===================== TESTS =====================


	public void getException() throws Exception {
		HttpTester request = makeRequest("GET", "/exception");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "Boom:Choucroute!");
	}


	public void getExceptionCaught() throws Exception {
		assertTrue(ControllerModule.exceptionCaught);
	}


	public void getOther() throws Exception {
		HttpTester request = makeRequest("GET", "/other");
        request.addHeader("x-sj-exc", "sourguice.test.mvc.ExceptionTest.OtherException");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 500);
		assertEquals(response.getReason(), "Choucroute");
	}
}
