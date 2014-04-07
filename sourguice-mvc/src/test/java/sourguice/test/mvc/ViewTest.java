package sourguice.test.mvc;

import static org.testng.Assert.assertEquals;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.testng.annotations.Test;

import com.github.sourguice.SourGuice;
import com.github.sourguice.mvc.SourGuiceMvc;
import com.github.sourguice.mvc.annotation.controller.ViewDirectory;
import com.github.sourguice.mvc.annotation.controller.ViewRenderedWith;
import com.github.sourguice.mvc.annotation.request.RequestMapping;
import com.github.sourguice.mvc.annotation.request.View;
import com.github.sourguice.mvc.view.Model;
import com.github.sourguice.mvc.view.def.BasicViewRenderer;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

@SuppressWarnings({"javadoc", "static-method", "PMD"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class ViewTest extends TestBase {

    // ===================== RENDERERS =====================

	@Singleton
	public static class DefaultTestRenderer extends BasicViewRenderer {
		@Inject
		public DefaultTestRenderer(Provider<HttpServletResponse> responseProvider) { super(responseProvider); }

		@RenderFor("anno.view")
		public void annodir(PrintWriter writer, Map<String, Object> model) {
			writer.write("d:anno:" + model.get("name").toString());
		}
	}

	@Singleton
	public static class AnnoTestRenderer extends BasicViewRenderer {
		@Inject
		public AnnoTestRenderer(Provider<HttpServletResponse> responseProvider) { super(responseProvider); }

		@RenderFor("/views/annodir.view")
		public void annodir(PrintWriter writer, Map<String, Object> model) {
			writer.write("a:annodir:" + model.get("name").toString());
		}

		@RenderFor("/annoroot.view")
		public void annoroot(PrintWriter writer, Map<String, Object> model) {
			writer.write("a:annoroot:" + model.get("name").toString());
		}

		@RenderFor("/views/RV.view")
		public void rv(PrintWriter writer, Map<String, Object> model) {
			writer.write("a:return:" + model.get("name").toString());
		}

		@RenderFor("/views/dir/in.view")
		public void dirin(PrintWriter writer, Map<String, Object> model) {
			writer.write("a:dirin:" + model.get("name").toString());
		}
	}

    // ===================== CONTROLLERS =====================

	@Singleton
	@ViewDirectory("/views")
	@ViewRenderedWith(regex = ".*", renderer = AnnoTestRenderer.class)
    public static class AController {

    	@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

    	@SuppressWarnings("serial")
		@RequestMapping("/annodir")
        @View("annodir.view")
        public void annodir(Model model) {
        	model.put("name", "Choucroute");
        	model.addAllAttributes(new HashMap<String, String>() {{ put("name", "Salomon"); put("truc", "bidule"); }});
        }

        @SuppressWarnings("serial")
        @RequestMapping("/annoroot")
        @View("/annoroot.view")
        public void annoroot(Model model) {
        	model.put("name", "Salomon");
        	model.mergeAttributes(new HashMap<String, String>() {{ put("name", "Choucroute"); put("truc", "bidule"); }});
        }

        @RequestMapping("/returnview")
        @View
        public String returnview(Model model) {
        	model.put("name", "Salomon");
        	return "RV.view";
        }

        @RequestMapping("/dirin")
        @View("{}/in.view")
        public String dirin(Model model) {
        	model.put("name", "Salomon");
        	return "dir";
        }
	}

    @Singleton
    public static class DController {

    	@RequestMapping(value = "/__startup")
		public void startup() { /* startup */ }

        @RequestMapping("/anno")
        @View("anno.view")
        public void anno(Model model) {
        	model.put("name", "Salomon");
        }

        @RequestMapping("/noview")
        @View("noview.view")
        public void noview() { /**/ }
}

    // ===================== MODULE =====================

    public static class ControllerModule extends ServletModule {
        @Override
        protected void configureServlets() {
        	SourGuiceMvc mvc = new SourGuiceMvc(new SourGuice());
        	mvc.control("/a/*").with(AController.class);
        	mvc.control("/d/*").with(DController.class);
        	mvc.renderViews(".*").with(DefaultTestRenderer.class);
            install(mvc.module());
        }
    }

    @Override
    protected Module module() {
        return new ControllerModule();
    }

    @Override
	protected void makeStartupRequest(ServletTester tester) throws Exception {
		getResponse(tester, makeRequest("GET", "/a/__startup"));
		getResponse(tester, makeRequest("GET", "/d/__startup"));
	}

    // ===================== TESTS =====================


    public void getAAnnoDir() throws Exception {
        HttpTester request = makeRequest("GET", "/a/annodir");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "a:annodir:Salomon");
    }


    public void getAAnnoRoot() throws Exception {
        HttpTester request = makeRequest("GET", "/a/annoroot");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "a:annoroot:Salomon");
    }


    public void getAReturnView() throws Exception {
        HttpTester request = makeRequest("GET", "/a/returnview");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "a:return:Salomon");
    }


    public void getADirIn() throws Exception {
        HttpTester request = makeRequest("GET", "/a/dirin");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "a:dirin:Salomon");
    }


    public void getDAnno() throws Exception {
        HttpTester request = makeRequest("GET", "/d/anno");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 200);
		assertEquals(response.getContent(), "d:anno:Salomon");
    }


    public void getDNoView() throws Exception {
        HttpTester request = makeRequest("GET", "/d/noview");
        request.addHeader("x-sj-exc", "com.github.sourguice.mvc.view.def.BasicViewRenderer.NoSuchBasicViewMethodException");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 500);
		assertEquals(response.getReason(), "sourguice.test.mvc.ViewTest.DefaultTestRenderer has no method annotated with @RenderFor(\"noview.view\")");
    }

}

