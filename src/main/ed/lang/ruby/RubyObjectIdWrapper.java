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

import org.jruby.*;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;

import ed.db.ObjectId;

/**
 * A wrapper for ObjectId instances that come into Ruby.
 */
@SuppressWarnings("serial")
public class RubyObjectIdWrapper extends RubyObject {

    static final ObjectAllocator OBJECT_ID_ALLOCATOR = new ObjectAllocator() {
            public IRubyObject allocate(Ruby runtime, RubyClass klass) {
                /* Allocates but sets _oid to null. Assumes initialize will set ObjectId value. */
                return new RubyObjectIdWrapper(runtime);
            }
        };

    protected static final String NULL_OBJECT_ID_STRING = "ObjectId(null)";

    public static synchronized RubyClass getObjectIdClass(final Ruby runtime) {
        RubyClass klazz = runtime.getClass("ObjectId");
        if (klazz == null) {
            klazz = runtime.defineClass("ObjectId", runtime.getObject(), OBJECT_ID_ALLOCATOR);
            klazz.defineAnnotatedMethods(RubyObjectIdWrapper.class);
            klazz.kindOf = new RubyModule.KindOf() {
                    public boolean isKindOf(IRubyObject obj, RubyModule type) {
                        return obj instanceof RubyObjectIdWrapper;
                    }
                };
            klazz.defineAnnotatedMethods(RubyObjectIdWrapper.class);
        }
        return klazz;
    }

    protected ObjectId _oid;

    private RubyObjectIdWrapper(Ruby runtime) {
        super(runtime, getObjectIdClass(runtime));
        _oid = null;
    }

    RubyObjectIdWrapper(Ruby runtime, ObjectId oid) {
        super(runtime, getObjectIdClass(runtime));
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("creating RubyObjectIdWrapper");
        if (oid == null)
            throw new IllegalArgumentException("RubyObjectIdWrapper: oid must not be null");
        _oid = oid;
    }

    @JRubyMethod(name = "initialize", required = 1)
    public IRubyObject initialize(IRubyObject oidString) {
        if (_oid == null) {                       // Never stomp on existing value
            String javaOidString = oidString.convertToString().toString();
            if (!ObjectId.isValid(javaOidString))
                throw getRuntime().newArgumentError("bad object id: " + javaOidString);
            _oid = new ObjectId(javaOidString);
        }
        return this;
    }

    public ObjectId getObjectId() { return _oid; }

    public IRubyObject to_s() {
        return getRuntime().newString(_oid == null ? NULL_OBJECT_ID_STRING : _oid.toString());
    }

    /** "==" */
    public IRubyObject op_equal(ThreadContext context, IRubyObject obj) {
        if (this == obj)
            return getRuntime().getTrue();
        if (!(obj instanceof RubyObjectIdWrapper))
            return getRuntime().getFalse();
        RubyObjectIdWrapper other = (RubyObjectIdWrapper)obj;
        if (this._oid == null)
            return other._oid == null ? getRuntime().getTrue() : getRuntime().getFalse();
        return this._oid.equals(other._oid) ? getRuntime().getTrue() : getRuntime().getFalse();
    }

    /** "equal?" */
    public IRubyObject equal_p(ThreadContext context, IRubyObject obj) { return op_equal(context, obj); }

    /** Used for Hash key comparison. */
    public boolean eql(IRubyObject other) { return op_equal(getRuntime().getCurrentContext(), other).isTrue(); }

    /** "eql?" */
    public IRubyObject eql_p(IRubyObject obj) { return op_equal(getRuntime().getCurrentContext(), obj); }

    /* "===" */
    public IRubyObject op_eqq(ThreadContext context, IRubyObject other) { return op_equal(context, other); }

    /** Only for use by ObjectId.marshal_load, defined in oid.rb. */
    @JRubyMethod(name = "_internal_oid_set", required = 1, visibility = org.jruby.runtime.Visibility.PRIVATE)
    public IRubyObject internal_oid_set(ThreadContext context, IRubyObject value) {
        _oid = new ObjectId(value.toString());
        return this;
    }
}
