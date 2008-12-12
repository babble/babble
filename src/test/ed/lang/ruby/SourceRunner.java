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
import java.io.OutputStream;

import org.jruby.Ruby;
import org.jruby.ast.Node;
import org.jruby.runtime.builtin.IRubyObject;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import ed.appserver.AppContext;
import ed.js.PrintBuffer;
import ed.js.engine.Scope;
import ed.net.httpserver.JxpWriter;

/** Makes RubyJxpSource testable by letting us control input and capture output. */
class TestRubyJxpSource extends RubyJxpSource {

    static final RuntimeEnvironment runenv = new RuntimeEnvironment(null);

    JxpWriter.Basic _writer;    // receives output
    String _content;            // set directly from within test methods

    public TestRubyJxpSource() {
        super(null);
    }

    protected Ruby getRuntime() { return runenv.getRuntime(); }
    protected String getContent() { return _content; }
    protected Node getAST() throws IOException { return parseContent("fake_file_path"); }

    protected IRubyObject _doCall(Node node, Scope s, Object unused[]) {
        _writer = new JxpWriter.Basic();
        runenv.setup(s, null, new OutputStream() {
                public void write(int b) { _writer.write(b); }
            });
        return runenv.commonRun(node);
    }

    protected String getOutput() { return _writer.getContent(); }
}

/**
 * Provides the ability to run JS and Ruby, and defines {@link #assertRubyEquals}.
 */
public class SourceRunner {

    Scope s;
    String jsOutput;
    String rubyOutput;
    TestRubyJxpSource source;

    @BeforeMethod(groups={"ruby", "ruby.jxpsource", "ruby.db", "ruby.db.findone", "ruby.db.find", "ruby.jsobj", "ruby.jsfunc", "ruby.required"})
    public void setUp() {
        s = new Scope("test", null);
        ed.js.JSON.init(s);        // add tojson, tojson_u, fromjson
        source = new TestRubyJxpSource();
    }

    /** Return result of running jsCode. Output is stored in jsOutput. */
    protected Object runJS(String jsCode) {
        PrintBuffer buf = new PrintBuffer();
        s.put("print", buf);
        Object o = s.eval(jsCode);
        jsOutput = buf.toString().trim();
        return o;
    }

    protected void assertJSEquals(String jsCode, String expected) {
        runJS(jsCode);
        assertEquals(jsOutput, expected);
    }

    /**
     * Runs <var>rubyCode</var> and returns the JSObject result. Output is
     * stored in rubyOutput.
     */
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

    /**
     * Runs JS first, then Ruby, then compares the outputs using the JS output
     * as the expected value.
     */
    protected void assertRubyEqualsJS(String rubyCode, String jsCode) {
        runJS(jsCode);
        runRuby(rubyCode);
        assertEquals(rubyOutput, jsOutput);
    }
}
