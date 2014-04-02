package sourguice.test.cache.server;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.testng.annotations.Test;

import sourguice.test.TestBase;

import com.github.sourguice.SourGuice;
import com.github.sourguice.cache.server.CacheFilter;
import com.github.sourguice.cache.server.CacheService;
import com.github.sourguice.cache.server.def.CacheInMemory;
import com.github.sourguice.cache.server.def.InMemoryCache;
import com.github.sourguice.cache.server.def.InMemoryCacheFilter;
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

		@RequestMapping(value = "/manual_char")
		@Writes
		public String manual_char(CacheService cacheService) throws IOException {
			cacheService.<InMemoryCache>cacheRequest().setExpiration(2 * 60); // 2 minutes

			++manualCharHit;

			return "Salomon:C";
		}

		@RequestMapping(value = "/manual_byte")
		public void manual_byte(CacheService cacheService, OutputStream stream) throws IOException {
			cacheService.<InMemoryCache>cacheRequest().setExpiration(2 * 60); // 2 minutes

			++manualByteHit;

			stream.write("Salomon:B".getBytes());
		}

		@RequestMapping(value = "/auto")
		@CacheInMemory(seconds = 2 * 60)
		@Writes
		public String auto() {
			++autoHit;

			return "Salomon:A";
		}

		@RequestMapping(value = "/remove_1")
		@CacheInMemory(seconds = 2 * 60)
		public void remove_1() {
			++removeHit;
		}

		@RequestMapping(value = "/remove_2")
		public void remove_2() {
			InMemoryCache.remove("/remove_1");
		}
    }

    // ===================== MODULE =====================

    public static class ControllerModule extends ServletModule {
        @Override
        protected void configureServlets() {
        	SourGuiceMvc mvc = new SourGuiceMvc(new SourGuice());
        	mvc.control("/*").with(Controller.class);
            install(InMemoryCache.initialize(16));
            install(mvc.module());
        }
    }

    @Override
    protected Module module() {
        return new ControllerModule();
    }

	@Override
	protected void addServletTesterFilter(ServletTester tester) {
		tester.addFilter(InMemoryCacheFilter.class, "/*", 0);
		tester.addFilter(CacheFilter.class, "/*", 0);
		tester.addFilter(TestGuiceFilter.class, "/*", 0);
	}

    // ===================== TESTS =====================

	public void getManualChar() throws Exception {
		synchronized (this) { // Forcing serial testing
			HttpTester request = makeRequest("GET", "/manual_char");
			HttpTester response = getResponse(request);

			assertEquals(response.getStatus(), 200);
			assertEquals(response.getContent(), "Salomon:C");
			assertEquals(Controller.manualCharHit, 1);
		}
	}

	public void getManualByte() throws Exception {
		synchronized (this) { // Forcing serial testing
			HttpTester request = makeRequest("GET", "/manual_byte");
			HttpTester response = getResponse(request);

			assertEquals(response.getStatus(), 200);
			assertEquals(response.getContent(), "Salomon:B");
			assertEquals(Controller.manualByteHit, 1);
		}
	}

	public void getAuto() throws Exception {
		synchronized (this) { // Forcing serial testing
			HttpTester request = makeRequest("GET", "/auto");
			HttpTester response = getResponse(request);

			assertEquals(response.getStatus(), 200);
			assertEquals(response.getContent(), "Salomon:A");
			assertEquals(Controller.autoHit, 1);
		}
	}

	public void getRemove() throws Exception {
		synchronized (this) { // Forcing serial testing
			final int hit = Controller.removeHit;
			getResponse(makeRequest("GET", "/remove_1"));
			assertEquals(Controller.removeHit, hit + 1);
			getResponse(makeRequest("GET", "/remove_1"));
			assertEquals(Controller.removeHit, hit + 1);

			getResponse(makeRequest("GET", "/remove_2"));

			getResponse(makeRequest("GET", "/remove_1"));
			assertEquals(Controller.removeHit, hit + 2);

			getResponse(makeRequest("GET", "/remove_2"));
		}
	}
}

