/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.lang.ruby;

import org.jruby.exceptions.RaiseException;

import ed.lang.Language;
import ed.io.LineReader;
import ed.js.engine.Scope;
import ed.appserver.jxp.JxpSource;
import ed.appserver.adapter.AdapterType;
import ed.appserver.AppContext;
import ed.appserver.JSFileLibrary;

import java.io.File;

/**
 * Used by the {@link ed.js.Shell} to run Ruby code. Provides an IRB REPL for
 * the shell.
 */
public class RubyLanguage extends Language {

    static class RubyShellSource extends RubyJxpSource {

        protected String _code;
        RubyShellSource(String code) {
            super(null);
            _code = code;
        }
        protected String getContent() { return _code; }
    }

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB");

    public RubyLanguage() { super("ruby"); }

    public JxpSource getAdapter(AdapterType type, File f, AppContext context, JSFileLibrary lib) {
        /* If we're still in init, treat everything as a .rb file. For
         *  example, an import initialized in an _init.rb would be mightily
         *  disturbed to be treated like a CGI script. */
        if (context != null && context.inScopeSetup())
            return new RubyJxpSource(f);

        switch (type) {
        case CGI:
            return new RubyCGIAdapter(f);
        case DIRECT_10GEN:
            return new RubyJxpSource(f);
        default:
            throw new RuntimeException("ERROR : unsupported AdapterType : " + type);
        }
    }

    public Object eval(Scope s, String code, boolean[] hasReturn) {
        RubyJxpSource source = new RubyShellSource(code);
        Object result = null;
        try {
            result = RubyObjectWrapper.toJS(s, source._doCall(source.parseContent("(shell)"), s, RuntimeEnvironment.EMPTY_OBJECT_ARRAY));
            hasReturn[0] = true;
        }
        catch (RaiseException re) {
            re.printStackTrace();
        }
        catch (Exception e) {
            System.err.println(e.toString());
            if (DEBUG)
                e.printStackTrace();
        }
        return result;
    }

    /**
     * Provides a Ruby REPL by running IRB.
     */
    public void repl(Scope s, String rubyFile, boolean exit) throws java.io.IOException {
        StringBuilder code = new StringBuilder();
        code.append("require 'irb'\n")
            .append("class JSObject; def inspect; tojson(self); end; end\n")
            .append("ARGV[0] = '--simple-prompt'\n");
        if (rubyFile != null) {
            for (String line : new LineReader(rubyFile))
                code.append(line).append("\n");
        }
        if (!exit)
            code.append("IRB.start\n");
        RubyJxpSource source = new RubyShellSource(code.toString());

        try {
            RuntimeEnvironment runenv = new RuntimeEnvironment(null);
            runenv.setup(s, null, null);
            runenv.commonRun(source.parseContent("(shell)"));
        }
        catch (RaiseException re) {
            re.printStackTrace();
        }
        catch (Exception e) {
            System.err.println(e.toString());
            if (DEBUG)
                e.printStackTrace();
        }
    }
}
