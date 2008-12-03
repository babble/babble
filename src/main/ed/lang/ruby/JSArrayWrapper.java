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

import java.lang.reflect.Array;
import java.util.*;

import org.jruby.*;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.builtin.Variable;
import org.jruby.util.IdUtil;

import ed.db.ObjectId;
import ed.js.*;
import ed.js.engine.Scope;
import ed.util.IdentitySet;
import ed.util.StringParseUtil;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;

/**
 * JSArrayWrapper acts as a bridge between Ruby arrays and JSArrays. This
 * is a JSArray that contains and forwards value changes to a RubyArray.
 *
 * @see RubyJSArrayWrapper
 * @see JSArray
 */
public class JSArrayWrapper extends JSArray {

    private Scope _scope;
    private RubyArray _rarray;
    private WrappedRuby _wrappedRuby;

    public JSArrayWrapper(Scope scope, RubyArray rarray) {
        super(rarray.getLength());
        _scope = scope;
        _rarray = rarray;
        _wrappedRuby = new WrappedRuby(rarray);

        // Copy values into _array
        int len = _rarray.getLength();
        Ruby runtime = getRuntime();
        for (int i = 0; i < len; ++i)
            super.setInt(i, toJS(_scope, _rarray.aref(runtime.newFixnum(i))));
    }

    public RubyObject getRubyObject() { return _rarray; }

    protected Ruby getRuntime() { return _rarray.getRuntime(); }

    protected ThreadContext context() { return _rarray.getRuntime().getCurrentContext(); }

    /** See {@link JSArray#set} */
    public Object set(Object n, Object v) {
        int i = _getInt(n);
        if (i >= 0)
            return setInt(i, v);
        if ("".equals(n.toString())) {
            _rarray.append(toRuby(_scope, getRuntime(), v));
            super.set(n, v);
        }
        // TODO handle n == "length"
        throw new IllegalArgumentException("JSArrayWrapper can't handle set(" + n + ", " + v + ")");
    }

    public Object setInt(int n, Object v) {
        _rarray.aset(getRuntime().newFixnum(n), toRuby(_scope, getRuntime(), v));
        return super.setInt(n, v);
    }

    public boolean addAll(Collection c) {
        Ruby runtime = getRuntime();
        for (Object o : c)
            _rarray.append(toRuby(_scope, runtime, o));
        return super.addAll(c);
    }

    public boolean addAll(int i, Collection c) {
        Ruby runtime = getRuntime();
        RubyFixnum start = runtime.newFixnum(i);
        RubyFixnum length = runtime.newFixnum(0); // for splicing; not length of c

        RubyArray a = new RubyArray(runtime, runtime.getArray());
        for (Object o : c)
            a.append(toRuby(_scope, runtime, o));

        _rarray.aset(start, length, a);

        return super.addAll(i, c);
    }

    public void addAll(Enumeration e) {
        Ruby runtime = getRuntime();
        ArrayList listToAddToJS = new ArrayList();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            _rarray.append(toRuby(_scope, runtime, o));
            listToAddToJS.add(o);
        }
        super.addAll(listToAddToJS);
    }

    public void addAll(Iterator i) {
        Ruby runtime = getRuntime();
        ArrayList listToAddToJS = new ArrayList();
        while (i.hasNext()) {
            Object o = i.next();
            _rarray.append(toRuby(_scope, runtime, o));
            listToAddToJS.add(o);
        }
        super.addAll(listToAddToJS);
    }

    @SuppressWarnings("unchecked")
    public List subList(int start, int end) {
        List rubySubList = _rarray.subList(start, end);
        List a = new ArrayList();
        Ruby runtime = getRuntime();
        for (Object o : rubySubList)
            a.add(toRuby(_scope, runtime, o));
        return a;
    }

    public Object remove(int i) {
        RubyFixnum rf = getRuntime().newFixnum(i);
        IRubyObject ro = _rarray.aref(rf);
        _rarray.aset(rf, getRuntime().getNil());
        return super.remove(i);
    }

    public boolean remove(Object o) {
        _rarray.remove(toRuby(_scope, getRuntime(), o));
        return super.remove(o);
    }

    public JSArray shuffle() {
        super.shuffle();
        int len = _rarray.getLength();
        Ruby runtime = getRuntime();
        for (int i = 0; i < len; ++i)
            super.setInt(i, toJS(_scope, _rarray.aref(runtime.newFixnum(i))));
        return this;
    }

    public void clear() {
        _rarray.rb_clear();
        super.clear();
    }

    public Object removeField(Object n) {
        int i = _getInt(n);
        if (i < 0) {
            Object o = get(n);
            _wrappedRuby.removeIvarIfExists(n.toString());
            _wrappedRuby.removeMethodIfExists(n.toString());
            return o;
        }
        if (i >= _rarray.getLength())
            return null;

        RubyFixnum rf = getRuntime().newFixnum(i);
        IRubyObject val = _rarray.aref(rf);
        _rarray.aset(rf, getRuntime().getNil());
        return super.removeField(n);
    }

    // TODO ignore top-level (Kernel) methods defined from JavaScript

    public boolean containsKey(String s) {
        return containsKey(s , true);
    }

    /** See {@link JSArray#containsKey(String)} */
    public boolean containsKey(String s, boolean includePrototype) {
        if ("length".equals(s))
            return true;
        
        int i = StringParseUtil.parseIfInt(s , -1);
        if (i >= 0)
            return i < _rarray.getLength();

        return _rarray.hasInstanceVariable("@" + s) ||
            _wrappedRuby.respondsToAndIsNotXGen(s);
    }

    public Set<String> keySet(boolean includePrototype) {
        Set<String> names = new HashSet<String>();

        for (int i = _rarray.getLength() - 1; i >= 0; --i)
            names.add(String.valueOf(i));

        // DEBUG
//         names.add("length");
//         names.add("size");

        /* We don't add all of the array's methods or instance variables
         * because they are not needed on the JS side and because the runtime
         * doesn't need them here to ask for them via get() in any case. */

        return names;
    }

    public JSFunction getConstructor() {
        return new JSRubyClassWrapper(_scope, _rarray.type());
    }

    /* Can't return super (?) because Ruby super != JavaScript super. */
    public JSObject getSuper() {
        return null;
    }

    public JSFunction getFunction(String name) {
        if (_wrappedRuby.respondsToAndIsNotXGen(name))
            return (JSFunction)toJS(_scope, _rarray.method(getRuntime().newString(name)));
        else
            return null;
    }

    /** See {@link JSArray#toString} */
    public String toString() {
        Ruby runtime = getRuntime();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < _rarray.getLength(); ++i) {
            if (i > 0)
                buf.append(',');
            Object val = toJS(_scope, _rarray.aref(runtime.newFixnum(i)));
            buf.append(val == null ? "" : JSInternalFunctions.JS_toString(val));
        }
        return buf.toString();
    }
}
