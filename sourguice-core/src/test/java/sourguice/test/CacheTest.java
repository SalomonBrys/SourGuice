package sourguice.test;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.cache.Cache;
import com.github.sourguice.cache.CacheService;
import com.github.sourguice.cache.def.InMemoryCache;
import com.github.sourguice.cache.def.InMemoryCacheFilter;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method", "PMD"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class CacheTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

    	static int manualCharHit = 0;
    	static int manualByteHit = 0;

		@RequestMapping(value = "/startup")
		public void startup() {
			// Does nothing.
			// Calling it makes SourGuice initialize
		}

		@RequestMapping(value = "/manual_char")
		@Writes
		public String manual_char(CacheService cacheService) throws IOException {
			cacheService.<InMemoryCache>cacheRequest().setExpiration(2 * 60); // 2 minutes

			++manualCharHit;

			return "Salomon:C";
		}

		@RequestMapping(value = "/manual_byte")
		public void manual_byte(CacheService cacheService, HttpServletResponse res) throws IOException {
			cacheService.<InMemoryCache>cacheRequest().setExpiration(2 * 60); // 2 minutes

			++manualByteHit;

			res.getOutputStream().write("Salomon:B".getBytes());
		}

    }

    // ===================== MODULE =====================

    public static class ControllerModule extends MvcControlerModule {
        @Override
        protected void configureControllers() {
            control("/*").with(Controller.class);
            InMemoryCache.initialize(16);
            bind(Cache.class).to(InMemoryCache.class);
        }
    }

    @Override
    protected MvcControlerModule module() {
        return new ControllerModule();
    }

	@Override
	protected void addServletTesterFilter(ServletTester tester) {
		tester.addFilter(InMemoryCacheFilter.class, "/*", 0);
		tester.addFilter(TestGuiceFilter.class, "/*", 0);
	}

    // ===================== TESTS =====================

	public void getManualChar() throws Exception {
		synchronized (this) { // Forcing serial testing
			getResponse(makeRequest("GET", "/startup"));

			HttpTester request = makeRequest("GET", "/manual_char");

			long start = System.nanoTime();
			HttpTester response = getResponse(request);
			long end = System.nanoTime();

			System.out.println("Manual char request duration: " + ((double)(end - start) / 1000000) + "ms");

			assertEquals(response.getStatus(), 200);
			assertEquals(response.getContent(), "Salomon:C");
			assertEquals(Controller.manualCharHit, 1);
		}
	}

	public void getManualByte() throws Exception {
		synchronized (this) { // Forcing serial testing
			getResponse(makeRequest("GET", "/startup"));

			HttpTester request = makeRequest("GET", "/manual_byte");

			long start = System.nanoTime();
			HttpTester response = getResponse(request);
			long end = System.nanoTime();

			System.out.println("Manual byte request duration: " + ((double)(end - start) / 1000000) + "ms");

			assertEquals(response.getStatus(), 200);
			assertEquals(response.getContent(), "Salomon:B");
			assertEquals(Controller.manualByteHit, 1);
		}
	}

}

