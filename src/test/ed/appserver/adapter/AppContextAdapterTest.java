package ed.appserver.adapter;

import ed.appserver.AppContext;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

import java.io.File;

/**
 *  Test the adapter ssytem for the AppContext
 */
public class AppContextAdapterTest {


    @Test
    public void testInit() throws Exception {
        AppContext ac = new AppContext("src/test/data/test/ed/appserver/adapter/test_static");

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.UNSET);

        ac.getScope();

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.CGI);

        assertTrue(ac.getAdapterType(new File("_init.js")) == AdapterType.DIRECT_10GEN);

        // we don't care about the non "_init." files
        
        assertTrue(ac.getAdapterType(new File("_init_bar.js")) == AdapterType.CGI);
    }

    @Test
    public void testStatic() throws Exception {

        AppContext ac = new AppContext("src/test/data/test/ed/appserver/adapter/test_static");

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.UNSET);

        ac.getScope();

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.CGI);

        assertTrue(ac.getAdapterType(new File("foo.js")) == AdapterType.CGI);
        assertTrue(ac.getAdapterType(new File("bar.py")) == AdapterType.CGI);
        assertTrue(ac.getAdapterType(new File("_init-woogie.js")) == AdapterType.CGI);        
    }

    @Test
    public void testDynamic() throws Exception {

        String ROOT = "src/test/data/test/ed/appserver/adapter/test_dynamic/";

        AppContext ac = new AppContext(ROOT);

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.UNSET);

        ac.getScope();

        assertTrue(ac.getStaticAdapterTypeValue() == AdapterType.DIRECT_10GEN);

        assertTrue(ac.getAdapterType(new File(ROOT + "cgi.py")) == AdapterType.CGI);
        assertTrue(ac.getAdapterType(new File(ROOT + "wsgi.py")) == AdapterType.WSGI);
        assertTrue(ac.getAdapterType(new File(ROOT + "direct.js")) == AdapterType.DIRECT_10GEN);
        assertTrue(ac.getAdapterType(new File(ROOT + "other.js")) == AdapterType.DIRECT_10GEN);
    }
}
