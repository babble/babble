/**
*    Copyright (C) 2008 10gen Inc.
*
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.lang.ruby;

import java.io.*;

import org.jruby.*;
import org.testng.annotations.Test;
import static org.testng.Assert.fail;

import ed.appserver.JSFileLibrary;
import ed.appserver.Module;
import ed.js.*;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.lang.ruby.RubyJxpSource;

@Test(groups = {"ruby", "ruby.testunit"})
public class RubyFileRunnerTest {

    public void testRunAllRubyFiles() {
	String edHome = System.getenv("ED_HOME");
	File here = new File(edHome, "src/test/ed/lang/ruby");
	File f = new File(here, "run_all_tests.rb");

	Scope s = createScope(f.getParentFile());

	RubyJxpSource source = new RubyJxpSource(f, null);
	addRubyLoadPath(source, new File(edHome, "build").getPath()); // for xgen_internals.rb
	addRubyLoadPath(source, here.getPath());

	try {
	    source.getFunction().call(s, new Object[0]);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    fail("while running file " + f.getPath() + ", exception was thrown: " + e);
	}
    }

    protected Scope createScope(File localRootDir) {
	Scope s = new Scope("test", null);
	ed.js.JSON.init(s);	// add tojson, tojson_u, fromjson

	// From ed.js.Shell.addNiceShellStuff
        s.put("core", CoreJS.get().getLibrary(null, null, s, false), true);
        s.put("external", Module.getModule("external").getLibrary(null, null, s, false), true);
        s.put("connect", new Shell.ConnectDB(), true);

	s.set("local", new JSFileLibrary(localRootDir, "local", s));
	s.set("jsout", "");	// initial value; used by tests; will be written over later

	JSFunction print = new JSFunctionCalls0() {
		public Object call(Scope s, Object[] args) {
		    return s.put("jsout", s.get("jsout").toString() + args[0].toString() + "\n", false); // default print behavior adds newline
		}
	    };
	print.setName("print");
	s.set("print", print);

	return s;
    }

    protected void addRubyLoadPath(RubyJxpSource source, String path) {
	Ruby runtime = source.getRuntime();
	RubyString rpath = RubyString.newString(runtime, path.replace('\\', '/'));
	RubyArray loadPath = (RubyArray)runtime.getLoadService().getLoadPath();
	if (loadPath.include_p(runtime.getCurrentContext(), rpath).isFalse())
	    loadPath.append(rpath);
    }
}
