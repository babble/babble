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

import java.util.Set;
import java.util.HashSet;

import org.jruby.Ruby;
import org.jruby.RubyProc;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.runtime.IAccessor;
import org.jruby.runtime.builtin.IRubyObject;

import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;

/**
 * Delegates to existing global variables, adds scope to them, and keeps scope
 * and globals synchronized. Does not synchronize variables with "special"
 * names&mdash;those that do not start with a letter.
 */
public class ScopeGlobalVariables extends GlobalVariables {

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB.GLOBALS");

    private Scope _scope;
    private Ruby _runtime;
    private GlobalVariables _delegate;
    private IRubyObject _defaultSeparator;

    public ScopeGlobalVariables(Scope scope, Ruby runtime) {
        super(runtime);
        _scope = scope;
        _runtime = runtime;

        _delegate = _runtime.getGlobalVariables();
        while (_delegate instanceof ScopeGlobalVariables)
            _delegate = ((ScopeGlobalVariables)_delegate)._delegate;

        Set<String> delegateNamesCopy = new HashSet<String>(_delegate.getNames());
        for (Object key : RubyJSObjectWrapper.jsKeySet(_scope)) { // Add scope vars to Ruby globals
            if (!isSpecial(key.toString())) {
                Object val = _scope.get(key);
                _delegate.set("$" + key.toString(), toRuby(_scope, _runtime, val, "$" + key));
            }
        }
    }

    /**
     * Returns <code>true</code> if <var>jsKey</var> is "special"&mdash;has a
     * non-letter first character.
     */
    public boolean isSpecial(String jsKey) {
        return !Character.isLetter(jsKey.charAt(0));
    }

    public void define(String name, IAccessor accessor) { _delegate.define(name, accessor); }

    public void defineReadonly(String name, IAccessor accessor) { _delegate.defineReadonly(name, accessor); }

    public boolean isDefined(String name) { return _delegate.isDefined(name); }

    public void alias(String name, String oldName) { _delegate.alias(name, oldName); }

    public IRubyObject get(String name) {
        String key = globalNameToJSKey(name);
        if (isSpecial(key))     // no read from scope, no debug output
            return _delegate.get(name);

        if (DEBUG)
            System.err.println("ScopeGlobalVariables.get(" + name + ")");
        Object val = _scope.get(key);
        IRubyObject ro = val == null ? _delegate.get(name) : toRuby(_scope, _runtime, val, key);
        if (DEBUG) {
            System.err.println("  value in scope = " + val);
            if (val == null)
                System.err.println("  got delegate value " + ro);
        }
        return ro;
    }

    public IRubyObject set(String name, IRubyObject value) {
        IRubyObject val = _delegate.set(name, value);

        String key = globalNameToJSKey(name);
        if (isSpecial(key))
            return val;         // no save to scope, no debug output

        if (DEBUG)
            System.err.println("ScopeGlobalVariables.set(" + name + ")");
        _scope.put(key, toJS(_scope, val));
        return val;
    }

    public void setTraceVar(String name, RubyProc proc) { _delegate.setTraceVar(name, proc); }

    public boolean untraceVar(String name, IRubyObject command) { return _delegate.untraceVar(name, command); }

    public void untraceVar(String name) { _delegate.untraceVar(name); }

    public Set<String> getNames() { return _delegate.getNames(); }

    public IRubyObject getDefaultSeparator() {
        return _delegate.getDefaultSeparator();
    }

    public void setDefaultSeparator(IRubyObject defaultSeparator) {
        _delegate.setDefaultSeparator(defaultSeparator);
    }

    private String globalNameToJSKey(String name) {
        assert name != null;
        assert name.startsWith("$");
        return name.substring(1);
    }
}
