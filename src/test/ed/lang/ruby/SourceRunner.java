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

import ed.js.JSFunction;
import ed.js.PrintBuffer;
import ed.js.engine.Scope;
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
}

/**
 * Provides the ability to run JS and Ruby, and defines {@link #assertRubyEquals}.
 */
public class SourceRunner {

    Scope s;
    org.jruby.Ruby r;
    String jsOutput;
    String rubyOutput;
    TestRubyJxpSource source;

    @BeforeTest(groups={"ruby", "ruby.jxpsource", "ruby.db"})
    public void globalSetUp() {
	s = new Scope("test", null);
	ed.js.JSON.init(s);	// add tojson, tojson_u, fromjson
	r = org.jruby.Ruby.newInstance();
	source = new TestRubyJxpSource(r);
    }

    /** Return result of running jsCode. Output is stored in jsOutput. */
    protected Object runJS(String jsCode) {
	PrintBuffer buf = new PrintBuffer();
	s.put("print", buf);
	Object o = s.eval(jsCode);
	jsOutput = buf.toString().trim();
	return o;
    }

    /** Output is stored in rubyOutput. */
    protected Object runRuby(String rubyCode) {
	source._content = rubyCode;
	Object o = null;
	try {
	    o = source.getFunction().call(s, new Object[0]);
	    rubyOutput = source.getOutput().trim();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    fail(e.toString());
	}
	return o;
    }

    protected void assertRubyEquals(String rubyCode, String expected) {
	runRuby(rubyCode);
	assertEquals(rubyOutput, expected);
    }

    protected void assertRubyEqualsJS(String rubyCode, String jsCode) {
	runJS(jsCode);
	runRuby(rubyCode);
	assertEquals(rubyOutput, jsOutput);
    }
}
