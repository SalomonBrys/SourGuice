package sourguice.test;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.testng.annotations.Test;

import com.github.sourguice.SourGuice;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.cache.CacheService;
import com.github.sourguice.cache.def.CacheInMemory;
import com.github.sourguice.cache.def.InMemoryCache;
import com.github.sourguice.cache.def.InMemoryCacheFilter;
import com.github.sourguice.cache.httpclient.CacheInClient;
import com.github.sourguice.cache.httpclient.CacheInClientBuilder;
import com.github.sourguice.cache.httpclient.HttpClientCache;
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
        	SourGuice sg = new SourGuice();
        	sg.control("/*").with(Controller.class);
            install(InMemoryCache.initialize(16));
            install(HttpClientCache.module());
            install(sg.module());
        }
    }

    @Override
    protected Module module() {
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

