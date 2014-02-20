package sourguice.test;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.throwable.service.exception.UnreachableExceptionHandlerException;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method"})
public class ExceptionTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {
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
		@Override public boolean handle(CustomException exception, HttpServletRequest req, HttpServletResponse res) throws IOException {
			res.getWriter().write("Boom:" + exception.getMessage() + "!");
			return true;
		}
    }

    public static class SubCustomExceptionHandler implements ExceptionHandler<SubCustomException> {
		@Override public boolean handle(SubCustomException exception, HttpServletRequest req, HttpServletResponse res) throws IOException {
			res.getWriter().write("SubBoom:" + exception.getMessage() + "!");
			return true;
		}
    }

    // ===================== MODULE =====================

    public static class ControllerModule extends MvcControlerModule {

    	public static boolean exceptionCaught = false;

        @Override
        protected void configureControllers() {
            control("/*").with(Controller.class);
            try {
            	handleException(CustomException.class).with(new CustomExceptionHandler());
            }
            catch (UnreachableExceptionHandlerException e) {
            	throw new RuntimeException(e);
            }

            try {
            	handleException(SubCustomException.class).with(new SubCustomExceptionHandler());
            }
            catch (UnreachableExceptionHandlerException e) {
            	exceptionCaught = true;
            }
        }
    }

    @Override
    protected MvcControlerModule module() {
        return new ControllerModule();
    }

    // ===================== TESTS =====================

	@Test
	public void getException() throws Exception {
		HttpTester request = makeRequest("GET", "/exception");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("Boom:Choucroute!");
	}

	@Test
	public void getExceptionCaught() throws Exception {
		assert ControllerModule.exceptionCaught;
	}

	@Test
	public void getOther() throws Exception {
		HttpTester request = makeRequest("GET", "/other");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 500;
		assert response.getReason().equals("Choucroute");
	}
}
