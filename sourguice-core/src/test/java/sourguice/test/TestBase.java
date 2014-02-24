package sourguice.test;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.github.sourguice.MvcControlerModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;

@SuppressWarnings({"javadoc", "static-method"})
public abstract class TestBase {

	public static final int INVOCATION_COUNT = 3;
	public static final int THREAD_POOL_SIZE = 4;

	public static class StandardContextListener<T extends MvcControlerModule> extends GuiceServletContextListener {

		T module;

		public StandardContextListener(T module) {
			super();
			this.module = module;
		}

		@Override
		protected Injector getInjector() {
			return Guice.createInjector(module);
		}

	}

	private Queue<ServletTester> queue = new LinkedList<>();

	@BeforeClass
	public void startupServletTester() throws Exception {
		StandardContextListener<MvcControlerModule> scl = new StandardContextListener<>(module());
		for (int i = 0; i < THREAD_POOL_SIZE; ++i) {
			ServletTester tester = new ServletTester();
			tester.setContextPath("/");
			tester.addEventListener(scl);
			tester.addServlet(DefaultServlet.class, "/");
			addServletTesterFilter(tester);
			tester.start();
			queue.add(tester);
		}
	}

	protected void addServletTesterFilter(ServletTester tester) {
		tester.addFilter(GuiceFilter.class, "/*", 0);
	}

	@AfterClass
	public void teardownServletTester() throws Exception {
		for (ServletTester tester : queue)
			tester.stop();
	}

	protected HttpTester makeRequest(String method, String uri) {
		HttpTester request = new HttpTester();
		request.setMethod(method);
		request.setURI(uri);
		request.setHeader("Host", "tester");
		return request;
	}

	protected void addPost(HttpTester request, String param, String value) throws IOException {
		String post = URLEncoder.encode(param, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
		String content = request.getContent();
		if (content != null && !content.isEmpty())
			content += "&" + post;
		else
			content = post;
		request.setContent(content);
		request.setHeader("content-type", "application/x-www-form-urlencoded");
		request.setHeader("content-length", String.valueOf(content.length()));
	}

	protected HttpTester getResponse(ServletTester tester, HttpTester request, boolean debug) throws Exception {
		HttpTester response = new HttpTester();

		String reqTxt = request.generate();
		if (debug) {
			System.out.println("==========================================");
			System.out.println("REQUEST:");
			System.out.println(reqTxt);
		}

		String resTxt = tester.getResponses(reqTxt);
		if (debug) {
			System.out.println("==========================================");
			System.out.println("RESPONSE:");
			System.out.println(resTxt);
			System.out.println("==========================================");
		}
		response.parse(resTxt);

		return response;
	}

	protected synchronized ServletTester pollTester() {
		return queue.poll();
	}

	protected synchronized void offerTester(ServletTester tester) {
		queue.offer(tester);
	}

	protected HttpTester getResponse(HttpTester request, boolean debug) throws Exception {

		ServletTester tester = pollTester();

		try {
			HttpTester response = getResponse(tester, request, debug);
			return response;
		}
		finally {
			offerTester(tester);
		}
	}

	protected HttpTester getResponse(HttpTester request) throws Exception {
		return getResponse(request, false);
	}

	protected HttpTester getResponse(ServletTester tester, HttpTester request) throws Exception {
		return getResponse(tester, request, false);
	}

	abstract protected MvcControlerModule module();

//	public static void main(String[] args) {
//		TestNG testng = new TestNG();
//		testng.setTestClasses(new Class<?>[] {
//			FilterTest.class,
//			HttpErrorTest.class,
//			InjectionTest.class,
//			RedirectTest.class,
//			RequestMappingTest.class,
//			ViewTest.class,
//			WriteTest.class
//		});
//		testng.run();
//	}
}
