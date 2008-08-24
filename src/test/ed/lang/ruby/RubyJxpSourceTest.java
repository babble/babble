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
    protected Node _getCode() throws IOException { return _parseContent("fake_file_path"); }
    protected void _setOutput(Scope s) {
	_writer = new JxpWriter.Basic();
	_runtime.getGlobalVariables().set("$stdout", new RubyIO(_runtime, new RubyJxpOutputStream(_writer)));
    }
    protected String getOutput() { return _writer.getContent(); }
    protected String trimmedOutput() {
	String str = getOutput();
	if (str != null) str = str.trim();
	return str;
    }
}

@Test(groups = {"ruby", "ruby.jxpsource"})
public class RubyJxpSourceTest {

    static JSFunction addSevenFunc;
    Scope s;
    org.jruby.Ruby r;
    TestRubyJxpSource source;

    @BeforeClass
    public void classSetUp() {
	addSevenFunc = new JSFunctionCalls1() {
		public Object call(Scope scope, Object arg, Object extras[]) {
		    return new Integer(((Number)arg).intValue() + 7);
		}
	    };
    }

    @BeforeTest
    public void globalSetUp() {
	s = new Scope("test", null);
	r = org.jruby.Ruby.newInstance();
	source = new TestRubyJxpSource(r);
    }

    @BeforeMethod
    public void setUp() {
	runJS("data = {}; data.count = 1; add_seven = function(i) { return i + 7; }; data.add_seven = add_seven;");
    }

    protected Object runJS(String jsCode) {
	return s.eval(jsCode);
    }

    protected void runRuby(String rubyCode) {
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
    }

    protected void assertRubyEquals(String rubyCode, String expected) {
	runRuby(rubyCode);
	assertEquals(source.trimmedOutput(), expected.trim());
    }

    @Test
    public void testContent() {
	assertRubyEquals("puts 1 + 1", "2");
    }

    @Test
    public void testScopeToVar() {
	assertRubyEquals("puts data.count;", "1");
    }

    @Test
    public void testCallFuncInVar() {
	assertRubyEquals("puts data.add_seven(35)", "42");
    }

    @Test
    public void testCallTopLevelFunc() {
	assertRubyEquals("puts add_seven(35)", "42");
    }

    @Test
    public void testGet() {
	assertRubyEquals("puts data.get('count')", "1");
    }

    @Test
    public void testGetUsingHash() {
	assertRubyEquals("puts data['count']", "1");
    }

    @Test
    public void testRubyModifiesJS() {
	runRuby("data.count = 42");
	assertEquals(s.eval("data.count").toString(), "42");
    }

    @Test
    public void testModifyUsingSet() {
	runRuby("data.set('count', 42)");
	assertEquals(s.eval("data.count").toString(), "42");
    }

    @Test
    public void testModifyUsingHash() {
	runRuby("data['count'] = 42");
	assertEquals(s.eval("data.count").toString(), "42");
    }
}
