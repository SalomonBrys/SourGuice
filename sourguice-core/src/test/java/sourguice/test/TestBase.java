package sourguice.test;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.github.sourguice.SourGuiceControlerModule;
import com.github.sourguice.SourGuiceFilter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

@SuppressWarnings({"javadoc", "static-method", "PMD"})
public abstract class TestBase {

	public static final int INVOCATION_COUNT = 3;
	public static final int THREAD_POOL_SIZE = 4;

	public static class StandardContextListener<T extends SourGuiceControlerModule> extends GuiceServletContextListener {

		private Injector injector;

		public StandardContextListener(T module) {
			super();
			this.injector = Guice.createInjector(module);
		}

		@Override
		protected Injector getInjector() {
			return this.injector;
		}

	}

	protected Queue<ServletTester> queue = new ConcurrentLinkedQueue<>();

	@BeforeClass
	public void startupServletTester() throws Exception {
		StandardContextListener<SourGuiceControlerModule> scl = new StandardContextListener<>(module());
		for (int i = 0; i < THREAD_POOL_SIZE; ++i) {
			ServletTester tester = new ServletTester();
			tester.setContextPath("/");
			tester.addEventListener(scl);
			tester.addServlet(DefaultServlet.class, "/");
			addServletTesterFilter(tester);
			tester.start();
			makeStartupRequest(tester);
			this.queue.add(tester);
		}
	}

	protected void makeStartupRequest(ServletTester tester) throws Exception {
		getResponse(tester, makeRequest("GET", "/__startup"));
	}

	public static class TestGuiceFilter extends SourGuiceFilter {

		static void handleException(ServletException e, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
			Throwable cause = e.getCause();
			if (cause == null) {
				throw e;
			}
			String exc = req.getHeader("x-sj-exc");
			if (exc != null && exc.equals(cause.getClass().getCanonicalName())) {
				res.sendError(500, cause.getMessage());
				return ;
			}
			System.err.println("UNEXPECTED SERVLET EXCEPTION: " + cause.getClass().getCanonicalName());
			throw e;
		}

		@Override
		public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
			try {
				super.doFilter(req, res, chain);
			}
			catch (ServletException e) {
				handleException(e, (HttpServletRequest)req, (HttpServletResponse)res);
			}
			catch (Throwable t) {
				handleException(new ServletException(t), (HttpServletRequest)req, (HttpServletResponse)res);
			}
		}

	}

	protected void addServletTesterFilter(ServletTester tester) {
		tester.addFilter(TestGuiceFilter.class, "/*", 0);
	}

	@AfterClass
	public void teardownServletTester() throws Exception {
		for (ServletTester tester : this.queue)
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

	protected HttpTester getResponse(ServletTester tester, HttpTester request) throws Exception {
		HttpTester response = new HttpTester();

		String reqTxt = request.generate();

		long start = System.nanoTime();
		String resTxt = tester.getResponses(reqTxt);
		long end = System.nanoTime();
		System.out.println(request.getMethod() + " " + request.getURI() + ": " + ((double)(end - start) / 1000000) + "ms");

		response.parse(resTxt);

		return response;
	}

	protected HttpTester getResponse(HttpTester request) throws Exception {

		ServletTester tester = this.queue.poll();

		try {
			HttpTester response = getResponse(tester, request);
			return response;
		}
		finally {
			this.queue.offer(tester);
		}
	}

	abstract protected SourGuiceControlerModule module();

	public static void main(String[] args) {
		TestNG testng = new TestNG();
		testng.setTestClasses(new Class<?>[] {
			ConversionTest.class,
			CustomConvertorTest.class,
			ExceptionTest.class,
			FilterTest.class,
			HttpErrorTest.class,
			InjectionTest.class,
			JSPTest.class,
			RedirectTest.class,
			RequestMappingTest.class,
			TestBase.class,
			ViewTest.class,
			WriteTest.class
		});
		testng.run();
	}
}
