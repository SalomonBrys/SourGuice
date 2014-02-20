package sourguice.test;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.Reader;

import javax.annotation.Nullable;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.Writes;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method"})
public class WriteTest extends TestBase {

    // ===================== CONTROLLER =====================

    @Singleton
    public static class Controller {

		@RequestMapping(value = "/writefail")
		@Writes
		public @Nullable String writefail() {
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

    public static class ControllerModule extends MvcControlerModule {
        @Override
        protected void configureControllers() {
            control("/*").with(Controller.class);
        }
    }

    @Override
    protected MvcControlerModule module() {
        return new ControllerModule();
    }

    // ===================== TESTS =====================

	@Test
	public void getWriteFail() throws Exception {
		HttpTester request = makeRequest("GET", "/writefail");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 500;
		assert response.getReason().equals("@Writes annotated method must NOT return null");
	}

	@Test
	public void getWriteStream() throws Exception {
		HttpTester request = makeRequest("GET", "/writestream");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("Salomon");
	}

	@Test
	public void getWriteReader() throws Exception {
		HttpTester request = makeRequest("GET", "/writereader");

		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("Salomon");
	}

}

