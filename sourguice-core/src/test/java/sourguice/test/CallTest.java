package sourguice.test;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.controller.Callable;
import com.github.sourguice.annotation.controller.InterceptWith;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.call.CalltimeArgumentFetcher;
import com.github.sourguice.call.MvcCaller;
import com.github.sourguice.exception.ExceptionHandler;
import com.github.sourguice.throwable.service.exception.UnreachableExceptionHandlerException;
import com.github.sourguice.utils.Annotations;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
@SuppressWarnings({"javadoc", "static-method"})
public class CallTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

    	@RequestMapping("/callprint")
    	public void callprint(MvcCaller caller, Writer writer) throws Throwable {
    		caller.call(this.getClass(), "printName", null, false);
    		writer.write(":");
    		Method method = this.getClass().getMethod("printName", Writer.class);
    		caller.call(this.getClass(), method, null, false);
    	}

    	@RequestMapping("/callbad")
    	@Writes
    	public String callbad(MvcCaller caller) throws Throwable {
    		caller.call(this.getClass(), "choucroute", null, false);
    		return "Salomon";
    	}

    	@RequestMapping("/callhandled")
    	@Writes
    	public String callhandled(MvcCaller caller) throws Throwable {
    		caller.call(this.getClass(), "throwhandled", null, false);
    		return "Salomon";
    	}

    	@RequestMapping("/callthrowhandled")
    	@Writes
    	public String callthrowhandled(MvcCaller caller) throws Throwable {
    		Method method = this.getClass().getMethod("throwhandled");
    		caller.call(this.getClass(), method, null, true);
    		return "Salomon";
    	}

    	@RequestMapping("/callthrownothandled")
    	@Writes
    	public String callthrownothandled(MvcCaller caller) throws Throwable {
    		caller.call(this.getClass(), "thrownothandled", null, false);
    		return "Salomon";
    	}

    	@RequestMapping("/callfetched")
    	@Writes
    	public @Nullable String callfetched(MvcCaller caller) throws Throwable {
    		return (String)caller.call(this.getClass(), "fetched", null, false, new NoArgumentFetcher(), new TestArgumentFetcher());
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

    public static class TestArgumentFetcher implements CalltimeArgumentFetcher<String> {
		@Override public boolean canGet(TypeLiteral<?> type, int pos, Annotation[] annos) {
			return type.getRawType() == String.class && Annotations.fromArray(annos).isAnnotationPresent(TestArgument.class);
		}
		@Override public String get(TypeLiteral<?> type, int pos, Annotation[] annos) throws Throwable {
			return "Salomon";
		}
    }

    public static class NoArgumentFetcher implements CalltimeArgumentFetcher<String> {
		@Override public boolean canGet(TypeLiteral<?> type, int pos, Annotation[] annos) {
			return false;
		}
		@Override @CheckForNull public String get(TypeLiteral<?> type, int pos, Annotation[] annos) throws Throwable {
			return null;
		}
    }

    // ===================== MODULE =====================

    public static class ControllerModule extends MvcControlerModule {
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
    protected MvcControlerModule module() {
        return new ControllerModule();
    }

    // ===================== TESTS =====================

	public void getCallPrint() throws Exception {
		HttpTester request = makeRequest("GET", "/callprint");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("[Salomon]:[Salomon]");
	}

	public void getCallBad() throws Exception {
		HttpTester request = makeRequest("GET", "/callbad");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 500;
		assert response.getReason().equals("No such method @Callable sourguice.test.CallTest.Controller.choucroute");
	}

	public void getCallHandled() throws Exception {
		HttpTester request = makeRequest("GET", "/callhandled");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("EXC!Salomon");
	}

	public void getCallThrowHandled() throws Exception {
		HttpTester request = makeRequest("GET", "/callthrowhandled");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("EXC!");
	}

	public void getCallThrowNotHandled() throws Exception {
		HttpTester request = makeRequest("GET", "/callthrownothandled");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 500;
		assert response.getReason().equals("Choucroute");
	}

	public void getCallFetched() throws Exception {
		HttpTester request = makeRequest("GET", "/callfetched");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("Salomon");
	}
}
