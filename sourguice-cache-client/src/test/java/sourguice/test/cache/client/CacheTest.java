package sourguice.test.cache.client;

import static org.testng.Assert.assertEquals;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.testng.annotations.Test;

import sourguice.test.mvc.TestBase;

import com.github.sourguice.SourGuice;
import com.github.sourguice.cache.client.CacheInClient;
import com.github.sourguice.cache.client.CacheInClientBuilder;
import com.github.sourguice.cache.client.HttpClientCache;
import com.github.sourguice.mvc.SourGuiceMvc;
import com.github.sourguice.mvc.annotation.request.RequestMapping;
import com.github.sourguice.mvc.annotation.request.Writes;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

@SuppressWarnings({"javadoc", "static-method", "PMD"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class CacheTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

    	static int manualCharHit = 0;
    	static int manualByteHit = 0;
    	static int autoHit = 0;
    	static int removeHit = 0;

		@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

		@RequestMapping(value = "/client_cache")
		@CacheInClient(MaxAge = 60 * 5, SMaxAge = 60 * 10, NoStore = true, NoTransform = true)
		@Writes
		public String client_cache() {
			return "Salomon:N";
		}

		@RequestMapping(value = "/client_private")
		@CacheInClient(Private = "user", MustRevalidate = true, Extension = "coucou-le-monde")
		@Writes
		public String client_private() {
			return "Salomon:N";
		}

		@RequestMapping(value = "/client_manual")
		@Writes
		public String client_manual(HttpServletResponse res) {
			HttpClientCache.setCacheControl(res,
				new CacheInClientBuilder()
					.MaxAge(60 * 10)
					.NoTransform(true)
					.build()
			);

			return "Salomon:N";
		}
    }

    // ===================== MODULE =====================

    public static class ControllerModule extends ServletModule {
        @Override
        protected void configureServlets() {
        	SourGuiceMvc mvc = new SourGuiceMvc(new SourGuice());
        	mvc.control("/*").with(Controller.class);
            install(HttpClientCache.module());
            install(mvc.module());
        }
    }

    @Override
    protected Module module() {
        return new ControllerModule();
    }

	@Override
	protected void addServletTesterFilter(ServletTester tester) {
		tester.addFilter(TestGuiceFilter.class, "/*", 0);
	}

    // ===================== TESTS =====================

	public void getClientCache() throws Exception {
		HttpTester request = makeRequest("GET", "/client_cache");
		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "Salomon:N");
		assertEquals(response.getHeader("Pragma"), "public");
		assertEquals(response.getHeader("Cache-Control"), "public,no-store,no-transform,max-age=300,s-maxage=600");
	}

	public void getClientPrivate() throws Exception {
		HttpTester request = makeRequest("GET", "/client_private");
		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "Salomon:N");
		assertEquals(response.getHeader("Pragma"), "private");
		assertEquals(response.getHeader("Cache-Control"), "private=\"user\",must-revalidate,coucou-le-monde");
	}

	public void getClientManual() throws Exception {
		HttpTester request = makeRequest("GET", "/client_manual");
		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "Salomon:N");
		assertEquals(response.getHeader("Pragma"), "public");
		assertEquals(response.getHeader("Cache-Control"), "public,no-transform,max-age=600");
	}
}

