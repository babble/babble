package ed.lang.ruby;

import java.io.*;

import org.jruby.Ruby;
import org.testng.annotations.Test;
import static org.testng.Assert.fail;

import ed.appserver.JSFileLibrary;
import ed.js.Shell;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.lang.ruby.RubyJxpSource;

@Test(groups = {"ruby", "ruby.testunit"})
public class RubyFileRunnerTest {

    public void testRunAllRubyFiles() {
	final File here = new File(System.getenv("ED_HOME"), "src/test/ed/lang/ruby");
	File f = new File(here, "run_all_tests.rb");

	Scope s = new Scope("test", null);
	ed.js.JSON.init(s);	// add tojson, tojson_u, fromjson
	s.set("connect", new Shell.ConnectDB());
	s.set("local", new JSFileLibrary(f.getParentFile(), "local", s));
	s.set("__instance__", new Object() { public String toString() { return here.getPath(); } }); // used to add this dir to path
	s.set("jsout", "");	// initial value; will be written over later

	JSFunction print = new JSFunctionCalls0() {
		public Object call(Scope s, Object[] args) {
		    return s.put("jsout", s.get("jsout").toString() + args[0].toString() + "\n", false); // default print behavior adds newline
		}
	    };
	print.setName("print");
	s.set("print", print);

	RubyJxpSource source = new RubyJxpSource(f, null);
	try {
	    source.getFunction().call(s, new Object[0]);
	}
	catch (Exception e) {
	    fail("while running file " + f.getPath() + ", exception was thrown: " + e);
	}
    }
}
