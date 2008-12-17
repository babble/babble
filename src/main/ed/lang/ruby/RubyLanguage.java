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
            source._doCall(source.parseContent("(shell)"), s, RuntimeEnvironment.EMPTY_OBJECT_ARRAY);
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
