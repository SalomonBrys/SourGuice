package sourguice.test;

import static org.testng.Assert.assertEquals;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.controller.ViewRenderedWith;
import com.github.sourguice.annotation.controller.ViewDirectory;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.View;
import com.github.sourguice.view.Model;
import com.github.sourguice.view.def.BasicViewRenderer;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method"})
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

    public static class ControllerModule extends MvcControlerModule {
        @Override
        protected void configureControllers() {
            control("/a/*").with(AController.class);
            control("/d/*").with(DController.class);
            renderViews(".*").with(DefaultTestRenderer.class);
        }
    }

    @Override
    protected MvcControlerModule module() {
        return new ControllerModule();
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
        request.addHeader("x-sj-exc", "com.github.sourguice.view.def.BasicViewRenderer.NoSuchBasicViewMethodException");

        HttpTester response = getResponse(request);

		assertEquals(response.getStatus(), 500);
		assertEquals(response.getReason(), "sourguice.test.ViewTest.DefaultTestRenderer has no method annotated with @RenderFor(\"noview.view\")");
    }

}

