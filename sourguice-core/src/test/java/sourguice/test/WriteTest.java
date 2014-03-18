package sourguice.test;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.Reader;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.SourGuiceControlerModule;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.Writes;
import com.google.inject.Singleton;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings({"javadoc", "static-method", "PMD"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class WriteTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

		@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

		@RequestMapping(value = "/writefail")
		@Writes
		@SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
		public String writefail() {
			return null;
		}

		@RequestMapping(value = "/writestream")
		@Writes
		public InputStream writestream() {
			return new ByteArrayInputStream("Salomon".getBytes());
		}

		@RequestMapping(value = "/writereader")
		@Writes
		public Reader writereader() {
			return new CharArrayReader("Salomon".toCharArray());
		}

    }

    // ===================== MODULE =====================

    public static class ControllerModule extends SourGuiceControlerModule {
        @Override
        protected void configureControllers() {
            control("/*").with(Controller.class);
        }
    }

    @Override
    protected SourGuiceControlerModule module() {
        return new ControllerModule();
    }

    // ===================== TESTS =====================


	public void getWriteFail() throws Exception {
		HttpTester request = makeRequest("GET", "/writefail");
        request.addHeader("x-sj-exc", "java.lang.UnsupportedOperationException");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 500);
		assertEquals(response.getReason(), "@Writes annotated method must NOT return null");
	}


	public void getWriteStream() throws Exception {
		HttpTester request = makeRequest("GET", "/writestream");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "Salomon");
	}


	public void getWriteReader() throws Exception {
		HttpTester request = makeRequest("GET", "/writereader");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "Salomon");
	}

}

