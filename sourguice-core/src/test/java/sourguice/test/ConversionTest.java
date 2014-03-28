package sourguice.test;

import static org.testng.Assert.assertEquals;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.sourguice.SourGuice;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.annotation.request.Writes;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

@SuppressWarnings({ "javadoc", "static-method", "PMD" })
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class ConversionTest extends TestBase {

	// ===================== POJOS =====================

	public static enum Finger {
		Thumb, Index, Middle, Ring, Little
	}

	public static class Weird {
		int schloff;
		String schtroumpf;
	}

	// ===================== CONTROLLER =====================

	@Singleton
	public static class Controller {

    	@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

		@RequestMapping("/boolean")
		@Writes
		public String _boolean(@RequestParam("var") Boolean[] var) {
			String ret = "";
			for (Boolean v : var)
				ret += ":" + v.toString();
			return ret;
		}

		@RequestMapping("/short")
		@Writes
		public String _short(@RequestParam("p") short p, @RequestParam("o") Short o) {
			return ":" + p + ":" + o;
		}

		@RequestMapping("/int")
		@Writes
		public String _int(@RequestParam("p") int p, @RequestParam("o") Integer o) {
			return ":" + p + ":" + o;
		}

		@RequestMapping("/long")
		@Writes
		public String _long(@RequestParam("p") long p, @RequestParam("o") Long o) {
			return ":" + p + ":" + o;
		}

		@RequestMapping("/float")
		@Writes
		public String _float(@RequestParam("p") float p, @RequestParam("o") Float o) {
			return ":" + p + ":" + o;
		}

		@RequestMapping("/double")
		@Writes
		public String _double(@RequestParam("p") double p, @RequestParam("o") Double o) {
			return ":" + p + ":" + o;
		}

		@RequestMapping("/enum")
		@Writes
		public String _enum(@RequestParam("var") Finger f) {
			return ":" + f;
		}

		@SuppressWarnings("unused")
		@RequestMapping("/primarray")
		public void primarray(@RequestParam("var") int[] var) {
			/**/
		}

		@SuppressWarnings("unused")
		@RequestMapping("/noconverter")
		public void noconverter(@RequestParam("var") Weird var) {
			/**/
		}

		@RequestMapping("/bigintarray")
		@Writes
		public String bigintarray(@RequestParam("var") Integer[][] var) {
			String ret = "";
			for (Integer[] ints : var) {
				ret += ":";
				for (Integer i : ints)
					ret += "-" + i;
			}
			return ret;
		}

	}

	// ===================== MODULE =====================

    public static class ControllerModule extends ServletModule {
        @Override
        protected void configureServlets() {
        	SourGuice sg = new SourGuice();
        	sg.control("/*").with(Controller.class);
            install(sg.module());
		}


	}

	@Override
	protected Module module() {
		return new ControllerModule();
	}

	// ===================== DATA PROVIDER =====================

	@DataProvider(name = "primitives")
	protected Object[][] createPrimitives() {
		return new Object[][] {
			{ "short",  "42",    "63",    "0" },
			{ "int",    "42",    "63",    "0" },
			{ "long",   "42",    "63",    "0" },
			{ "float",  "42.21", "63.84", "0.0" },
			{ "double", "42.21", "63.84", "0.0" }
		};

	}

	// ===================== TESTS =====================


	public void getBoolean() throws Exception {
		HttpTester request = makeRequest("GET", "/boolean?var=true,on,Y,yes,1,choucroute,0");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), ":true:true:true:true:true:false:false");
	}


	public void getEmptyArray() throws Exception {
		HttpTester request = makeRequest("GET", "/boolean?var=");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), null);
	}

	@Test(invocationCount = 3, dataProvider = "primitives")
	@SuppressWarnings("unused")
	public void getPrimitive(String name, String v1, String v2, String zero) throws Exception {
		HttpTester request = makeRequest("GET", "/" + name + "?p=" + v1 + "&o=" + v2);

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), ":" + v1 + ":" + v2);
	}

	@Test(invocationCount = 3, dataProvider = "primitives")
	@SuppressWarnings("unused")
	public void getBadPrimitive(String name, String v1, String v2, String zero) throws Exception {
		HttpTester request = makeRequest("GET", "/" + name + "?p=salut&o=coucou");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), ":" + zero + ":null");
	}


	public void getEnum() throws Exception {
		HttpTester request = makeRequest("GET", "/enum?var=Index");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), ":Index");
	}


	public void getEnumNoArray() throws Exception {
		HttpTester request = makeRequest("GET", "/enum?var=Index&var=Thumb");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), ":Index");
	}


	public void getBadEnum() throws Exception {
		HttpTester request = makeRequest("GET", "/enum?var=Choucroute");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), ":null");
	}


	public void getPrimReqArray() throws Exception {
		HttpTester request = makeRequest("GET", "/primarray?var=21&var=42");
        request.addHeader("x-sj-exc", "com.github.sourguice.throwable.service.converter.CannotConvertToPrimitiveException");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 500);
		assertEquals(response.getReason(), "Array conversion does not support primitive type: int");
	}


	public void getPrimConvArray() throws Exception {
		HttpTester request = makeRequest("GET", "/primarray?var=21,42");
        request.addHeader("x-sj-exc", "com.github.sourguice.throwable.service.converter.CannotConvertToPrimitiveException");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 500);
		assertEquals(response.getReason(), "Array conversion does not support primitive type: int");
	}


	public void getNoConverter() throws Exception {
		HttpTester request = makeRequest("GET", "/noconverter?var=coucou");
        request.addHeader("x-sj-exc", "com.github.sourguice.throwable.service.converter.NoConverterException");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 500);
		assertEquals(response.getReason(), "Could not find converter for sourguice.test.ConversionTest$Weird");
	}


	public void getBigIntArray() throws Exception {
		HttpTester request = makeRequest("GET", "/bigintarray?var=21,42&var=63,84");

		HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), ":-21-42:-63-84");
	}

}
