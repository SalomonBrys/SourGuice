package sourguice.test;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

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

    	static int manualHit = 0;

		@RequestMapping(value = "/manual")
		@Writes
		public String writereader(CacheService cacheService) throws IOException {
			cacheService.<InMemoryCache>cacheRequest().setExpiration(2 * 60); // 2 minutes

			++manualHit;

			return "Salomon";
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

	public void getManual() throws Exception {
		synchronized (this) { // Forcing serial testing
			HttpTester request = makeRequest("GET", "/manual");

			long start = System.nanoTime();
			HttpTester response = getResponse(request);
			long end = System.nanoTime();

			System.out.println("Manual request duration: " + ((double)(end - start) / 1000000) + "ms");

			assertEquals(response.getStatus(), 200);
			assertEquals(response.getContent(), "Salomon");
			assertEquals(Controller.manualHit, 1);
		}
	}

}

