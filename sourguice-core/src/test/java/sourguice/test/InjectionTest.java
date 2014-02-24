package sourguice.test;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;

import javax.servlet.http.HttpSession;

import org.eclipse.jetty.testing.HttpTester;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.sourguice.MvcControlerModule;
import com.github.sourguice.annotation.request.PathVariable;
import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.RequestHeader;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.annotation.request.RequestParam;
import com.github.sourguice.annotation.request.SessionAttribute;
import com.github.sourguice.annotation.request.Writes;
import com.github.sourguice.throwable.invocation.NoSuchPathVariableException;
import com.github.sourguice.utils.Arrays;
import com.github.sourguice.value.RequestMethod;
import com.google.inject.Singleton;

@SuppressWarnings({"javadoc", "static-method"})
@Test(invocationCount = TestBase.INVOCATION_COUNT, threadPoolSize = TestBase.THREAD_POOL_SIZE)
public class InjectionTest extends TestBase {

    // ===================== CONTROLLERS =====================

    @Singleton
    public static class Controller {
        @RequestMapping("/method")
        @Writes
        public String method(RequestMethod m) {
            return ":" + m.toString();
        }

        @RequestMapping("/pathvariablemap-{var}")
        @Writes
        public String pathvariablemap(@PathVariablesMap Map<String, String> var) {
            return ":" + var.get("var");
        }

        @RequestMapping("/emptypathvariablemap")
        @Writes
        public String emptypathvariablemap(@PathVariablesMap Map<String, String> var) {
            return ":" + var.isEmpty();
        }

        @RequestMapping("/sessionset")
        public void sessionset(HttpSession session) {
        	session.setAttribute("var", "Salomon");
        }

        @RequestMapping("/sessionget")
        @Writes
        public String sessionget(@SessionAttribute("var") String var) {
            return ":" + var;
        }

        @RequestMapping("/defaultparam")
        @Writes
        public String defaultparam(@RequestParam(value = "var", defaultValue = "Choucroute") String var) {
            return ":" + var;
        }

        @RequestMapping("/list")
        @Writes
        public String list(@RequestParam("var") List<String> var) {
        	String ret = "";
        	for (String v : var)
        		ret += ":" + v;
        	return ret;
        }

        @RequestMapping("/defaultlist")
        @Writes
        public String defaultlist(@RequestParam(value = "var", defaultValue = "a,b,c") List<String> var) {
        	String ret = "";
        	for (String v : var)
        		ret += ":" + v;
        	return ret;
        }

        @RequestMapping("/defaultemptylist")
        @Writes
        public String defaultemptylist(@RequestParam(value = "var", defaultValue = "") List<String> var) {
        	if (!var.isEmpty())
        		return "ko:" + var.size();
    		return "ok";
        }

        @RequestMapping("/map")
        @Writes
        public String map(@RequestParam("var") Map<String, String> var) {
        	String ret = "";
        	for (Map.Entry<String, String> v : var.entrySet())
        		ret += ":" + v.getKey() + "=" + v.getValue();
        	return ret;
        }

        @RequestMapping("/defaultmap")
        @Writes
        public String defaultmap(@RequestParam(value = "var", defaultValue = "a=one,b=two,c=three,d") Map<String, String> var) {
        	String ret = "";
        	for (Map.Entry<String, String> v : var.entrySet())
        		ret += ":" + v.getKey() + "=" + v.getValue();
        	return ret;
        }

        @RequestMapping("/defaultemptymap")
        @Writes
        public String defaultemptymap(@RequestParam(value = "var", defaultValue = "") Map<String, String> var) {
        	if (!var.isEmpty())
        		return "ko:" + var.size();
    		return "ok";
        }

        @RequestMapping("/array")
        @Writes
        public String array(@RequestParam("var") String[] var) {
        	String ret = "";
        	for (String v : var)
        		ret += ":" + v;
        	return ret;
        }

        @RequestMapping("/defaultheader")
        @Writes
        public String defaultheader(@RequestHeader(value = "x-choucroute", defaultValue = "Coucou") String var) {
        	return ":" + var;
        }

        @RequestMapping("/noheader")
        @Writes
        public String noheader(@RequestHeader("x-choucroute") String var) {
        	return ":" + var;
        }

        @RequestMapping("/printwriter")
        public void printwriter(PrintWriter writer) {
        	writer.println("Salomon");
        }

        @RequestMapping("/matchresult-{var}")
        @Writes
        public String matchresult(MatchResult mr) {
        	return ":" + mr.group(1);
        }

    }

    public static class BadPathVariableController {
        @RequestMapping("/badpathvariable-{var}")
        @Writes
        public String badpathvariable(@PathVariable("war") String war) {
            return ":" + war;
        }
    }

    // ===================== MODULE =====================

    public static class ControllerModule extends MvcControlerModule {
    	public static boolean exceptionCaught = false;
        @Override
        protected void configureControllers() {
            control("/*").with(Controller.class);
            try {
            	control("/*").with(BadPathVariableController.class);
            }
            catch (NoSuchPathVariableException e) {
            	exceptionCaught = true;
            }
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
            ret[i] = new Object[] { rms[i].toString() };
        return ret;
    }

    // ===================== TESTS =====================

    @Test(invocationCount = 3, dataProvider = "requestMethods")
    public void requestMethods(String method) throws Exception {
        if (method.equals("HEAD"))
            return ;

        HttpTester request = makeRequest(method, "/method");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        assert response.getContent().equals(":" + method);
    }


    public void getPathVariableMap() throws Exception {
        HttpTester request = makeRequest("GET", "/pathvariablemap-Salomon");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        assert response.getContent().equals(":Salomon");
    }


    public void getEmptyPathVariableMap() throws Exception {
        HttpTester request = makeRequest("GET", "/emptypathvariablemap");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        assert response.getContent().equals(":true");
    }


    public void getSession() throws Exception {
    	String jsessionid;

    	{
	        HttpTester request = makeRequest("GET", "/sessionset");
	        HttpTester response = getResponse(request);

	        assert response.getStatus() == 200;

	        String setCookie = response.getHeader("set-cookie");
	        jsessionid = setCookie.substring(setCookie.indexOf('=') + 1, setCookie.indexOf(';'));
    	}

    	{
	        HttpTester request = makeRequest("GET", "/sessionget");
	        request.setHeader("cookie", "jsessionid=" + jsessionid);
	        HttpTester response = getResponse(request);

	        assert response.getStatus() == 200;
	        assert response.getContent().equals(":Salomon");
    	}
    }


    public void getDefaultParam() throws Exception {
        HttpTester request = makeRequest("GET", "/defaultparam");

        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        assert response.getContent().equals(":Choucroute");
    }


    public void getList() throws Exception {
        HttpTester request = makeRequest("GET", "/list?var=one&var=two&var=three");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        String[] split = response.getContent().split(":");
        assert split.length == 4;
        assert Arrays.Contains(split, "one");
        assert Arrays.Contains(split, "two");
        assert Arrays.Contains(split, "three");
    }


    public void geDefaultList() throws Exception {
        HttpTester request = makeRequest("GET", "/defaultlist");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        String[] split = response.getContent().split(":");
        assert split.length == 4;
        assert Arrays.Contains(split, "a");
        assert Arrays.Contains(split, "b");
        assert Arrays.Contains(split, "c");
    }


    public void geDefaultEmptyList() throws Exception {
        HttpTester request = makeRequest("GET", "/defaultemptylist");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        assert response.getContent().equals("ok");
    }


    public void geEmptyListError() throws Exception {
        HttpTester request = makeRequest("GET", "/list");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 400;
        assert response.getReason().equals("Missing request parameters: var");
    }


    public void getMapBraces() throws Exception {
        HttpTester request = makeRequest("GET", "/map?var[a]=one&var[b]=two&var[c]=three&var[=choucroute");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        String[] split = response.getContent().split(":");
        assert split.length == 4;
        assert Arrays.Contains(split, "a=one");
        assert Arrays.Contains(split, "b=two");
        assert Arrays.Contains(split, "c=three");
    }


    public void getMapColon() throws Exception {
        HttpTester request = makeRequest("GET", "/map?var:a=one&var:b=two&var:c=three&nothing=choucroute");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        String[] split = response.getContent().split(":");
        assert split.length == 4;
        assert Arrays.Contains(split, "a=one");
        assert Arrays.Contains(split, "b=two");
        assert Arrays.Contains(split, "c=three");
    }


    public void getDefaultMap() throws Exception {
        HttpTester request = makeRequest("GET", "/defaultmap");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        String[] split = response.getContent().split(":");
        assert split.length == 4;
        assert Arrays.Contains(split, "a=one");
        assert Arrays.Contains(split, "b=two");
        assert Arrays.Contains(split, "c=three");
    }


    public void geDefaultEmptyMap() throws Exception {
        HttpTester request = makeRequest("GET", "/defaultemptymap");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        assert response.getContent().equals("ok");
    }


    public void geEmptyMapError() throws Exception {
        HttpTester request = makeRequest("GET", "/map");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 400;
        assert response.getReason().equals("Missing request parameters: var");
    }


    public void getArray() throws Exception {
        HttpTester request = makeRequest("GET", "/array?var=one&var=two&var=three");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        String[] split = response.getContent().split(":");
        assert split.length == 4;
        assert Arrays.Contains(split, "one");
        assert Arrays.Contains(split, "two");
        assert Arrays.Contains(split, "three");
    }


    public void getDefaultHeader() throws Exception {
        HttpTester request = makeRequest("GET", "/defaultheader");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        assert response.getContent().equals(":Coucou");
    }


    public void getNoHeader() throws Exception {
        HttpTester request = makeRequest("GET", "/noheader");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 400;
        assert response.getReason().equals("Missing header: x-choucroute");
    }


    public void getBadPathVariable() {
    	assert ControllerModule.exceptionCaught;
    }


    public void getPrintWriter() throws Exception {
        HttpTester request = makeRequest("GET", "/printwriter");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        assert response.getContent().equals("Salomon\n");
    }


    public void getMatchResult() throws Exception {
        HttpTester request = makeRequest("GET", "/matchresult-Salomon");
        HttpTester response = getResponse(request);

        assert response.getStatus() == 200;
        assert response.getContent().equals(":Salomon");
    }
}

