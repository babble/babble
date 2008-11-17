package ed.appserver.framework;

import ed.appserver.AppContext;
import ed.appserver.adapter.AdapterType;
import ed.js.JSDict;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 *  Test the framework ssytem for the AppContext
 */
public class AppContextFrameworkTest {

    @Test
    public void testFrameworkNameAppEngine() throws Exception {

        // first test is  = "appengineE" (no object and wacky casing, which should be safe)

        AppContext ac = new AppContext("src/test/data/test/ed/appserver/framework/test_name1");

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.UNSET);

        ac.getScope();

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.CGI);

        JSDict packages = (JSDict)ac.getConfigObject("packages");

        assertTrue(packages != null && packages.containsKey("google"));
        assertTrue(packages != null && packages.containsKey("django"));

        // second test is  as an object

        ac = new AppContext("src/test/data/test/ed/appserver/framework/test_name1");

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.UNSET);

        ac.getScope();

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.CGI);

        packages = (JSDict)ac.getConfigObject("packages");

        assertTrue(packages != null && packages.containsKey("google"));
        assertTrue(packages != null && packages.containsKey("django"));
    }

    @Test
    public void testFrameworkClass() throws Exception {
        AppContext ac = new AppContext("src/test/data/test/ed/appserver/framework/test_class");

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.UNSET);

        ac.getScope();

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.CGI);

        JSDict packages = (JSDict)ac.getConfigObject("packages");

        assertTrue(packages != null && packages.containsKey("floogie"));

        assertTrue(ac.getInitObject("myGlobalVariable").toString().equals("thing"));
    }

    @Test
    public void testNonFrameworkClass() throws Exception {
        AppContext ac = new AppContext("src/test/data/test/ed/appserver/framework/test_brokenclass");

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.UNSET);

        try {
            ac.getScope();
            fail();
        }
        catch (RuntimeException e) {
            // ok...
        }        
    }

    @Test
    public void testFrameworkCustom() throws Exception {
        AppContext ac = new AppContext("src/test/data/test/ed/appserver/framework/test_custom");

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.UNSET);

        ac.getScope();

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.WSGI);

        JSDict packages = (JSDict)ac.getConfigObject("packages");

        assertTrue(packages != null && packages.containsKey("schlorp"));
        assertTrue(ac.getInitObject("myGlobalVariable").toString().equals("thang"));

    }
}
