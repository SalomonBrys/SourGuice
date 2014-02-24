package sourguice.test;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.controller.Callable;
import com.github.sourguice.annotation.request.PathVariable;
import com.github.sourguice.annotation.request.RequestHeader;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.value.RequestMethod;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class RequestMappingTest extends TestBase {

	// ===================== CONTROLLERS =====================

	@Singleton
	public static class Controller1 {
		@RequestMapping("/hello")
		@Writes
		public String hello() {
			return "Hello, world";
		}

		@RequestMapping(value = "/print", method = RequestMethod.POST)
		@Writes
		public String print(@RequestParam("txt") String txt) {
			return txt;
		}

		@RequestMapping(value = "/simple")
		@Writes
		public String simple() {
			return "Get";
		}

		@RequestMapping(value = "/simple", params = {"var"})
		@Writes
		public String simpleParam(@RequestParam("var") String var) {
			return "v:" + var;
		}

		@RequestMapping(value = "/simple", headers = {"x-sj-test"})
		@Writes
		public String simpleHeader(@RequestHeader("x-sj-test") String var) {
			return "h:" + var;
		}

		@RequestMapping(value = "/simple", consumes = {"x-sj-test"})
		@Writes
		public String simpleConsumes() {
			return "Consumes";
		}

		@RequestMapping(value = "/simple", produces = {"x-sj-test"})
		@Writes
		public String simpleProduces() {
			return "Produces";
		}

		@RequestMapping(value = "/simple", produces = {"x-sj-nothing"})
		public void simpleProducesIgnored() {/**/}

		@Callable
		public void callableIgnored() {/**/}

		@RequestMapping({"/match-{var}"})
        @Writes
        public String match1(@PathVariable("var") String var) {
            return ":" + var;
        }

        @RequestMapping({"/match-{var}-{next}-{again}"})
        @Writes
        public String match2(@PathVariable("var") String var, @PathVariable("next") String next, @PathVariable("again") String again) {
            return ":" + var + ":" + next + ":" + again;
        }

        @RequestMapping({"/match-{var}-{next}"})
        @Writes
        public String match4(@PathVariable("var") String var, @PathVariable("next") String next) {
            return ":" + var + ":" + next;
        }

        @RequestMapping(value = {"/match-{var}-{next}-{again}"}, method = RequestMethod.GET)
        @Writes
        public String match3(@PathVariable("var") String var, @PathVariable("next") String next, @PathVariable("again") String again) {
            return "get:" + var + ":" + next + ":" + again;
        }

	}

	@Singleton
	public static class Controller2 {
		@RequestMapping(value = "/simple", method = RequestMethod.POST)
		@Writes
		public String simple() {
			return "Post";
		}
	}

	// ===================== MODULE =====================

	public static class ControllerModule extends MvcControlerModule {
		@Override
		protected void configureControllers() {
			control("/a/*").with(Controller1.class);
			control("/a/*").withInstance(new Controller2());
			control("/b/*").with(Controller1.class);
		}
	}

	@Override
	protected MvcControlerModule module() {
		return new ControllerModule();
	}

	// ===================== DATA PROVIDER =====================

	@DataProvider(name = "requestMethods")
	protected Object[][] createRequestMethods() {
		RequestMethod[] rms = RequestMethod.values();
		Object[][] ret = new Object[rms.length][];
		for (int i = 0; i < rms.length; ++i)
			ret[i] = new Object[] {
				rms[i].toString()
			};
		return ret;
	}

	// ===================== TESTS =====================


	public void getHello() throws Exception {
		HttpTester request = makeRequest("GET", "/a/hello");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("Hello, world");
	}


	public void getPrint() throws Exception {
		HttpTester request = makeRequest("POST", "/a/print");
		addPost(request, "txt", "hello the world");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("hello the world");
	}


	public void getExpect404() throws Exception {
		HttpTester request = makeRequest("GET", "/a/not-existing");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 404;
	}


	public void postMissingArgument() throws Exception {
		HttpTester request = makeRequest("POST", "/a/print");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 400;
	}


	public void getInvalidMethod() throws Exception {
		HttpTester request = makeRequest("GET", "/a/print");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 404;
	}

	@Test(invocationCount = 3, dataProvider = "requestMethods")
	public void allSimple(String method) throws Exception {
		if (method.equals("HEAD")) return ;
		HttpTester request = makeRequest(method, "/a/simple");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
	}


	public void getSimpleParam() throws Exception {
		HttpTester request = makeRequest("GET", "/a/simple?var=Salomon");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("v:Salomon");
	}


    public void getSimpleParamSessionId() throws Exception {
        HttpTester request = makeRequest("GET", "/a/simple;jsessionid=abcdef?var=Salomon");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        assert response.getContent().equals("v:Salomon");
    }



	public void getSimpleHeader() throws Exception {
		HttpTester request = makeRequest("GET", "/a/simple");
		request.addHeader("x-sj-test", "Salomon");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("h:Salomon");
	}


	public void getSimpleConsumes() throws Exception {
		HttpTester request = makeRequest("GET", "/a/simple");
		request.addHeader("content-type", "x-sj-test");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("Consumes");
	}


	public void getSimpleProduces() throws Exception {
		HttpTester request = makeRequest("GET", "/a/simple");
		request.addHeader("accept", "x-sj-test,xml;");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("Produces");
	}


	public void getMatch() throws Exception {
		HttpTester request = makeRequest("GET", "/b/match-one-two-three");
		HttpTester response = getResponse(request);

		assert response.getStatus() == 200;
		assert response.getContent().equals("get:one:two:three");
	}

}
