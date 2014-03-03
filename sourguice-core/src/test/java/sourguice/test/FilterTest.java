package sourguice.test;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.controller.InterceptWith;
import com.github.sourguice.annotation.request.InterceptParam;
import com.github.sourguice.annotation.request.RequestAttribute;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.utils.MVCCallInterceptSetter;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method", "PMD"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class FilterTest extends TestBase {

    // ===================== INTERCEPTOR =====================

	@Singleton
	public static class Interceptor implements MethodInterceptor {

		private MVCCallInterceptSetter setter;

		@Inject
		public Interceptor(MVCCallInterceptSetter setter) {
			super();
			this.setter = setter;
		}

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			this.setter.set(invocation, "var", "Salomon");
			return invocation.proceed();
		}

	}

    // ===================== FILTER =====================

	@Singleton
	public static class _Filter implements Filter {
		@Override public void init(FilterConfig arg0) throws ServletException { /**/ }
		@Override public void destroy() { /**/ }

		@Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
			req.setAttribute("var", "Salomon");
			chain.doFilter(req, res);
		}

	}

    // ===================== CONTROLLER =====================

	@Singleton
    public static class Controller {

    	@RequestMapping("/intercepted")
    	@InterceptWith(Interceptor.class)
    	@Writes
    	public String intercepted(@InterceptParam("var") String var) {
    		return ":" + var;
    	}

    	@RequestMapping("/filtered")
    	@Writes
    	public String filtered(@RequestAttribute("var") String var) {
    		return ":" + var;
    	}

    }

    // ===================== MODULE =====================

    public static class ControllerModule extends MvcControlerModule {
        @Override
        protected void configureControllers() {
            control("/*").with(Controller.class);
            filter("/filtered").through(_Filter.class );
        }
    }

    @Override
    protected MvcControlerModule module() {
        return new ControllerModule();
    }

    // ===================== TESTS =====================


    public void getIntercepted() throws Exception {
        HttpTester request = makeRequest("GET", "/intercepted");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), ":Salomon");
    }


    public void getFiltered() throws Exception {
        HttpTester request = makeRequest("GET", "/filtered");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), ":Salomon");
    }

}
