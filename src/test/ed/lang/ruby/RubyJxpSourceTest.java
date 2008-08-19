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

import java.io.IOException;

import org.jruby.RubyIO;
import org.jruby.ast.Node;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import ed.js.*;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.lang.ruby.RubyJxpSource;
import ed.lang.ruby.RubyJxpOutputStream;
import ed.net.httpserver.JxpWriter;

/** Makes RubyJxpSource testable by letting us control input and capture output. */
class TestRubyJxpSource extends RubyJxpSource {
    JxpWriter.Basic _writer;	// receives output
    String _content;		// set directly from within test methods
    public TestRubyJxpSource(org.jruby.Ruby runtime) {
	super(runtime);
    }
    protected String getContent() { return _content; }
    protected Node _getCode() throws IOException { return _parseContent("fake_file_path"); } // skip file checking 'cause there ain't no file
    protected void _setOutput(Scope s) {
	_writer = new JxpWriter.Basic();
	_runtime.getGlobalVariables().set("$stdout", new RubyIO(_runtime, new RubyJxpOutputStream(_writer)));
    }
}

@Test(groups = {"ruby", "ruby.jxpsource"})
public class RubyJxpSourceTest {

    static JSFunction addSevenFunc;
    Scope s;
    org.jruby.Ruby r;
    TestRubyJxpSource source;

    @BeforeClass(groups = {"convert", "r2js", "js2r"})
    public void classSetUp() {
	addSevenFunc = new JSFunctionCalls1() {
		public Object call(Scope scope, Object arg, Object extras[]) {
		    return new Integer(((Number)arg).intValue() + 7);
		}
	    };
    }

    @BeforeTest(groups = {"convert", "r2js", "js2r"})
    public void setUp() {
	s = new Scope("test", null);
	r = org.jruby.Ruby.newInstance();
	source = new TestRubyJxpSource(r);
    }

    protected void runRuby(String rubyCode, String expected) {
	source._content = rubyCode;
	JSFunction f = null;
	try {
	    f = source.getFunction();
	    f.call(s, new Object[0]);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    fail(e.toString());
	}
	assertEquals(source._writer.getContent().trim(), expected.trim());
    }

    public void testContent() {
	runRuby("puts 1 + 1", "2");
    }

    public void testScopeToVar() {
	JSObjectBase jobj = new JSObjectBase();
	jobj.set("count", new Integer(1));
	jobj.set("add_seven", addSevenFunc);

	s.set("data", jobj);
	runRuby("puts data.count", "1");
    }
}
