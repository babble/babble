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

import org.jruby.Ruby;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;

import ed.lang.Language;
import ed.js.engine.Scope;

class RubyShellSource extends RubyJxpSource {

    static final Ruby RUNTIME = Ruby.newInstance(RuntimeEnvironment.config);

    protected String _code;
    RubyShellSource(String code) {
        super(null, RUNTIME);
        _code = code;
    }
    protected String getContent() { return _code; }
}

/**
 * Used by the {@lang ed.js.Shell} to run Ruby code.
 */
public class RubyLanguage extends Language {

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB");

    public RubyLanguage() { super("ruby"); }

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
        finally {
            return result;
        }
    }

    /**
     * Provides a Ruby REPL by running IRB.
     */
    public void repl(Scope s, String rubyFile) {
        StringBuilder code = new StringBuilder();
        code.append("require 'irb'\n")
            .append("ARGV[0] = '--simple-prompt'\n");
        if (rubyFile != null)
            code.append("ARGV[1] = '").append(rubyFile).append("'\n");
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
