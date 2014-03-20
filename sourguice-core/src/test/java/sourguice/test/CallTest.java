package sourguice.test;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.SourGuiceControlerModule;
import com.github.sourguice.annotation.controller.Callable;
import com.github.sourguice.annotation.controller.InterceptWith;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.call.SGCaller;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.throwable.service.exception.UnreachableExceptionHandlerException;
import com.google.inject.Singleton;

@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
@SuppressWarnings({"javadoc", "static-method", "PMD"})
public class CallTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

    	@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

    	@RequestMapping("/callprint")
    	public void callprint(SGCaller caller) throws Throwable {
    		Method method = this.getClass().getMethod("printName", Writer.class);
    		caller.call(this.getClass(), method, null, false);
    	}

    	@RequestMapping("/callbad")
    	@Writes
    	public String callbad(SGCaller caller) throws Throwable {
    		Method method = String.class.getMethod("toUpperCase", char.class);
    		caller.call(this.getClass(), method, null, false);
    		return "Salomon";
    	}

    	@RequestMapping("/callhandled")
    	@Writes
    	public String callhandled(SGCaller caller) throws Throwable {
    		Method method = this.getClass().getMethod("throwhandled");
    		caller.call(this.getClass(), method, null, false);
    		return "Salomon";
    	}

    	@RequestMapping("/callthrowhandled")
    	@Writes
    	public String callthrowhandled(SGCaller caller) throws Throwable {
    		Method method = this.getClass().getMethod("throwhandled");
    		caller.call(this.getClass(), method, null, true);
    		return "Salomon";
    	}

    	@RequestMapping("/callthrownothandled")
    	@Writes
    	public String callthrownothandled(SGCaller caller) throws Throwable {
    		Method method = this.getClass().getMethod("thrownothandled");
    		try {
    			caller.call(this.getClass(), method, null, false);
    		}
    		catch (InvocationTargetException e) {
    			throw e.getCause();
    		}
    		return "Salomon";
    	}

    	@Callable
    	@InterceptWith(WriteInterceptor.class)
    	public void printName(Writer writer) throws IOException {
    		writer.write("Salomon");
    	}

    	@Callable
    	public void throwhandled() throws TestException {
    		throw new TestException();
    	}

    	@Callable
    	public void thrownothandled() {
    		throw new RuntimeException("Choucroute");
    	}

    	@Callable
    	public String fetched(@TestArgument String arg) {
    		return arg;
    	}

    }

    // ===================== INTERCEPTOR =====================

    public static class WriteInterceptor implements MethodInterceptor {
		@SuppressWarnings("resource")
		@Override public Object invoke(MethodInvocation invoc) throws Throwable {
			Writer writer = (Writer)invoc.getArguments()[0];
			writer.write("[");
			Object ret = invoc.proceed();
			writer.write("]");
			return ret;
		}
    }

    // ===================== EXCEPTION =====================

    @SuppressWarnings("serial")
	public static class TestException extends Exception { /**/ }

    public static class TestExceptionHandler implements ExceptionHandler<TestException> {
		@Override public boolean handle(TestException exception, HttpServletRequest req, HttpServletResponse res) throws IOException {
			res.getWriter().write("EXC!");
			return true;
		}
    }

    // ===================== FETCHER =====================

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public static @interface TestArgument {
    	// Flag Annotation
    }

    // ===================== MODULE =====================

    public static class ControllerModule extends SourGuiceControlerModule {
        @Override
        protected void configureControllers() {
            control("/*").with(Controller.class);
            try {
				handleException(TestException.class).withInstance(new TestExceptionHandler());
			}
            catch (UnreachableExceptionHandlerException e) {/**/}
        }
    }

    @Override
    protected SourGuiceControlerModule module() {
        return new ControllerModule();
    }

    // ===================== TESTS =====================

	public void getCallPrint() throws Exception {
		HttpTester request = makeRequest("GET", "/callprint");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "[Salomon]");
	}

	public void getCallBad() throws Exception {
		HttpTester request = makeRequest("GET", "/callbad");
        request.addHeader("x-sj-exc", "java.lang.NoSuchMethodException");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 500);
		assertEquals(response.getReason(), "java.lang.String.toUpperCase(char)");
	}

	public void getCallHandled() throws Exception {
		HttpTester request = makeRequest("GET", "/callhandled");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "EXC!Salomon");
	}

	public void getCallThrowHandled() throws Exception {
		HttpTester request = makeRequest("GET", "/callthrowhandled");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "EXC!");
	}

	public void getCallThrowNotHandled() throws Exception {
		HttpTester request = makeRequest("GET", "/callthrownothandled");
        request.addHeader("x-sj-exc", "java.lang.RuntimeException");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 500);
		assertEquals(response.getReason(), "Choucroute");
	}
}
