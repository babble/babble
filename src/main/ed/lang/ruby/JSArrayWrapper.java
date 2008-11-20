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
        _scope = scope;
        _rarray = rarray;
        _wrappedRuby = new WrappedRuby(rarray);
    }

    public RubyObject getRubyObject() { return _rarray; }

    protected Ruby getRuntime() { return _rarray.getRuntime(); }

    protected ThreadContext context() { return _rarray.getRuntime().getCurrentContext(); }

    /** See {@link JSArray#set} */
    public Object set(Object n, Object v) {
        int i = _getInt(n);
        if (i >= 0)
            return setInt(i, v);
        if ("".equals(n.toString()))
            return _rarray.append(toRuby(_scope, getRuntime(), v));
        throw new IllegalArgumentException("JSArrayWrapper can't handle set(" + n + ", " + v + ")");
    }

    /** See {@link JSArray#get} */
    public Object get(Object n) {
        if (n != null)
            if (n instanceof JSString || n instanceof String)
                if ("length".equals(n.toString()))
                    return toJS(_scope, _rarray.length());

        int i = _getInt(n);
        if (i >= 0)
            return toJS(_scope, _rarray.aref(getRuntime().newFixnum(i)));

        String skey = n.toString();
        if (_rarray.respondsTo(skey))
            return toJS(_scope, _rarray.callMethod(context(), skey, JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY, Block.NULL_BLOCK));
        else if (skey.equals("_id")) {
            IRubyObject val = _rarray.instance_variable_get(context(), _wrappedRuby.ivarName(skey));
            return (val == null || val.isNil()) ? toJS(_scope, null) : new ObjectId(val.toString());
        }
        else if (skey.startsWith("_")) // Assume it's an internal field; return the ivar value
            return toJS(_scope, _rarray.instance_variable_get(context(), _wrappedRuby.ivarName(skey)));
        else
            return toJS(_scope, null);
    }

    public Object setInt(int n, Object v) {
        _rarray.aset(getRuntime().newFixnum(n), toRuby(_scope, getRuntime(), v));
        return v;
    }

    public Object getInt(int n) {
        return toJS(_scope, ((RubyArray)_rarray).aref(getRuntime().newFixnum(n)));
    }

    public boolean addAll(Collection c) {
        Ruby runtime = getRuntime();
        for (Object o : c)
            _rarray.append(toRuby(_scope, runtime, o));
        return c.size() > 0;
    }

    public boolean addAll(int i, Collection c) {
        Ruby runtime = getRuntime();
        RubyFixnum start = runtime.newFixnum(i);
        RubyFixnum length = runtime.newFixnum(0); // for splicing; not length of c

        RubyArray a = new RubyArray(runtime, runtime.getArray());
        for (Object o : c)
            a.append(toRuby(_scope, runtime, o));

        _rarray.aset(start, length, a);

        return c.size() > 0;
    }

    public void addAll(Enumeration e) {
        Ruby runtime = getRuntime();
        int i = 0;
        while (e.hasMoreElements()) {
            _rarray.append(toRuby(_scope, runtime, e.nextElement()));
            ++i;
        }
    }

    public void addAll(Iterator i) {
        Ruby runtime = getRuntime();
        while (i.hasNext())
            _rarray.append(toRuby(_scope, runtime, i.next()));
    }

    public boolean containsAll(Collection c) {
        Ruby runtime = getRuntime();
        for (Object o : c)
            if (!_rarray.contains(toRuby(_scope, runtime, o)))
                return false;
        return true;
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

    final static class RubyArrayConversionListIterator extends RubyArrayConversionIterator implements ListIterator {
        public RubyArrayConversionListIterator(JSArrayWrapper wrapper) { super(wrapper); }
        public RubyArrayConversionListIterator(JSArrayWrapper wrapper, int index) { super(wrapper); this.index = index; }
        public boolean hasPrevious() { return index >= 0; }
        public Object previous() {
            last = --index;
            IRubyObject ro = (last < 0 || last >= wrapper._rarray.getLength()) ? wrapper.getRuntime().getNil() : wrapper._rarray.entry(last);
            return toJS(wrapper._scope, ro);
        }
        public int nextIndex() { return index; }
        public int previousIndex() { return index - 1; }
        public void set(Object obj) {
            if (last == -1) throw new IllegalStateException();
            wrapper._rarray.store(last, toRuby(wrapper._scope, wrapper.getRuntime(), obj));
        }
        public void add(Object obj) {
            Ruby runtime = wrapper.getRuntime();
            wrapper._rarray.insert(new IRubyObject[] { RubyFixnum.newFixnum(runtime, index++), toRuby(wrapper._scope, runtime, obj) });
            last = -1;
        }
    }

    public ListIterator listIterator() { return new RubyArrayConversionListIterator(this); }

    public ListIterator listIterator(int i) { return new RubyArrayConversionListIterator(this, i); }

    @SuppressWarnings("unchecked")
    static class RubyArrayConversionEnumeration<E> implements Enumeration<E> {
        protected JSArrayWrapper wrapper;
        protected int index = 0;
        public RubyArrayConversionEnumeration(JSArrayWrapper wrapper) { this.wrapper = wrapper; }
        public boolean hasMoreElements() { return index < wrapper._rarray.getLength(); }
        public E nextElement() { return (E)toJS(wrapper._scope, wrapper._rarray.aref(wrapper.getRuntime().newFixnum(index++))); }
    }

    public Enumeration getEnumeration() { return new RubyArrayConversionEnumeration(this); }

    public int lastIndexOf(Object n) { return _rarray.lastIndexOf(toRuby(_scope, getRuntime(), n)); }

    public int indexOf(Object n) { return _rarray.indexOf(toRuby(_scope, getRuntime(), n)); }

    public boolean contains(Object n) { return _rarray.contains(toRuby(_scope, getRuntime(), n)); }

    public boolean isEmpty() { return _rarray.isEmpty(); }

    public Object remove(int i) {
        RubyFixnum rf = getRuntime().newFixnum(i);
        IRubyObject ro = _rarray.aref(rf);
        _rarray.aset(rf, getRuntime().getNil());
        return toJS(_scope, ro);
    }

    public boolean remove(Object o) { return _rarray.remove(toRuby(_scope, getRuntime(), o)); }

    static class RubyArrayConversionIterator implements Iterator {
        protected JSArrayWrapper wrapper;
        protected int index = 0;
        protected int last = -1;

        public RubyArrayConversionIterator(JSArrayWrapper wrapper) { this.wrapper = wrapper; }

        public boolean hasNext() { return index < wrapper._rarray.getLength(); }

        public Object next() {
            IRubyObject element = wrapper._rarray.aref(wrapper.getRuntime().newFixnum(index));
            last = index++;
            return toJS(wrapper._scope, element);
        }

        public void remove() {
            if (last == -1) throw new IllegalStateException();

            wrapper._rarray.delete_at(wrapper.getRuntime().newFixnum(last));
            if (last < index) index--;

            last = -1;
        }
    }

    public Iterator iterator() { return new RubyArrayConversionIterator(this); }

    public Object[] toArray() {
        Object[] array = new Object[_rarray.getLength()];
        Ruby runtime = getRuntime();
        for (int i = 0; i < _rarray.getLength(); ++i)
            array[i] = toJS(_scope, _rarray.aref(runtime.newFixnum(i)));
        return array;
    }

    public Object[] toArray(Object[] array) {
        if (array.length < _rarray.getLength()) {
            Class type = array.getClass().getComponentType();
            array = (Object[])Array.newInstance(type, _rarray.getLength());
        }
        Ruby runtime = getRuntime();
        for (int i = 0; i < _rarray.getLength(); ++i)
            array[i] = toJS(_scope, _rarray.aref(runtime.newFixnum(i)));
        return array;
    }

    public JSArray shuffle() {
        /* Algorithm from http://www.maths.abdn.ac.uk/~igc/tch/mx4002/notes/node83.html */
        java.util.Random rand = new java.util.Random();
        Ruby runtime = getRuntime();
        for (int i = _rarray.getLength() - 1; i <= 1; --i) {
            RubyFixnum ri = runtime.newFixnum(i);
            RubyFixnum rk = runtime.newFixnum(rand.nextInt(i));
            // Could we use Ruby parallel assignment instead of a tmp?
            IRubyObject tmp = _rarray.aref(ri);
            _rarray.aset(ri, _rarray.aref(rk));
            _rarray.aset(rk, tmp);
        }
        return this;
    }

    public void clear() {
        _rarray.rb_clear();
    }

    public int hashCode(IdentitySet seen) {
        return _rarray.hashCode();
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
        return toJS(_scope, val);
    }

    // TODO ignore top-level (Kernel) methods defined from JavaScript

    public boolean containsKey(String s) {
        return containsKey( s , true );
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

        /* Add each ivar's name iff the ivar has a setter or a getter. */
        for (Variable<IRubyObject> var : _rarray.getInstanceVariables().getInstanceVariableList()) {
            String name = var.getName().substring(1); // Strip off leading "@"
            if (_rarray.respondsTo(name) || _rarray.respondsTo(name + "="))
                names.add(name);
        }

        if (includePrototype)
            for (Object name : ((RubyArray)_rarray.methods(context(), JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY)))
                if (_wrappedRuby.respondsToAndIsNotXGen(name.toString()))
                    names.add(name.toString());

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
