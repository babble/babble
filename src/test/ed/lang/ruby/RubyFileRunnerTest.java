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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import ed.appserver.JSFileLibrary;
import ed.js.*;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.lang.ruby.RubyJxpSource;

public class RubyFileRunnerTest {

    protected static final String QA_RAILS_TEST_DIR_RELATIVE = "modules/ruby/rails";

    @Test(groups = {"ruby", "ruby.testunit"})
    public void testRunRubyTests() {
        runTestsIn(new File(System.getenv("ED_HOME"), "src/test/ed/lang/ruby"));
    }

    /**
     * This test runs the tests in QA_RAILS_TEST_DIR_RELATIVE, but only if
     * that directory exists. We look in two places: /data/qa and
     * $ED_HOME/../qa.
     * <p>
     * These tests require one or more copies of Rails itself, which we'd like
     * to keep out of the base Babble code.
     * <p>
     * NOTE: by default this test is not run by "ant test-ruby" for now (the
     * definition of ruby.groups in build.xml does not include
     * "ruby.activerecord". This is because our automated test environment's
     * of the QA project isn't quite ready to run these tests.
     */
    @Test(groups = {"ruby.activerecord"})
    public void testRunRailsTests() {
        File dir;
        if ((dir = new File("/data/qa", QA_RAILS_TEST_DIR_RELATIVE)).exists() ||
            (dir = new File(new File(System.getenv("ED_HOME"), "../qa"), QA_RAILS_TEST_DIR_RELATIVE)).exists())
            runTestsIn(dir);
        else
            assertTrue(true);
    }

    protected void runTestsIn(File rootDir) {
        String edHome = System.getenv("ED_HOME");
        File f = new File(rootDir, "run_all_tests.rb");

        Scope s = createScope(f.getParentFile());

        RubyJxpSource source = new RubyJxpSource(f, Ruby.newInstance());
        addRubyLoadPath(s, source, new File(edHome, "build").getPath()); // for xgen.rb and files it includes
        addRubyLoadPath(s, source, rootDir.getPath());

        try {
            source.getFunction().call(s, new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("while running file " + f.getPath() + ", exception was thrown: " + e);
        }
    }

    protected Scope createScope(File localRootDir) {
        Scope s = Scope.newGlobal();
        s = new Scope("test", s); // child of global scope
        Shell.addNiceShellStuff(s);
        s.set("local", new JSFileLibrary(localRootDir, "local", s));
        s.set("rails_local", new JSFileLibrary(new File(localRootDir, "rails-test-app"), "local", s));
        s.set("core", CoreJS.get().getLibrary(null, null, s, false));

        s.set("jsout", ""); // initial value; used by tests; will be written over later
        JSFunction print = new JSFunctionCalls0() {
                public Object call(Scope s, Object[] args) {
                    return s.put("jsout", s.get("jsout").toString() + args[0].toString() + "\n", false); // default print behavior adds newline
                }
            };
        print.setName("print");
        s.set("print", print);

        return s;
    }

    protected void addRubyLoadPath(Scope s, RubyJxpSource source, String path) {
        Ruby runtime = source.getRuntime(s);
        RubyString rpath = RubyString.newString(runtime, path.replace('\\', '/'));
        RubyArray loadPath = (RubyArray)runtime.getLoadService().getLoadPath();
        if (loadPath.include_p(runtime.getCurrentContext(), rpath).isFalse())
            loadPath.append(rpath);
    }
}
