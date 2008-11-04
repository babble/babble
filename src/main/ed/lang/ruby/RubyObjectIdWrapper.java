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

import java.lang.ref.WeakReference;
import java.util.*;

import org.jruby.*;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import static org.jruby.runtime.Visibility.PRIVATE;

import ed.db.ObjectId;

@SuppressWarnings("serial")
public class RubyObjectIdWrapper extends RubyObject {

    static Map<Ruby, WeakReference<RubyClass>> klassDefs = new WeakHashMap<Ruby, WeakReference<RubyClass>>();
    static final ObjectAllocator OBJECT_ID_ALLOCATOR = new ObjectAllocator() {
            public IRubyObject allocate(Ruby runtime, RubyClass klass) {
                /* Allocates but sets _oid to null. Assumes initialize will set ObjectId value. */
                return new RubyObjectIdWrapper(runtime);
            }
        };

    public static synchronized RubyClass getObjectIdClass(final Ruby runtime) {
        WeakReference<RubyClass> ref = klassDefs.get(runtime);
        if (ref == null) {
            RubyClass klazz = runtime.defineClass("ObjectId", runtime.getObject(), OBJECT_ID_ALLOCATOR);
            klazz.defineAnnotatedMethods(RubyObjectIdWrapper.class);
            klazz.kindOf = new RubyModule.KindOf() {
                    public boolean isKindOf(IRubyObject obj, RubyModule type) {
                        return obj instanceof RubyObjectIdWrapper;
                    }
                };
            runtime.getKernel().defineAnnotatedMethods(RubyObjectIdWrapper.class);
            klassDefs.put(runtime, ref = new WeakReference<RubyClass>(klazz));
        }
        return ref.get();
    }

    protected ObjectId _oid;

    @JRubyMethod(name = "ObjectId", required = 1, module = true, visibility = PRIVATE)
    public static IRubyObject new_objectid(ThreadContext context, IRubyObject recv, IRubyObject object) {
        ObjectId oid = new ObjectId(object.toString());
        return new RubyObjectIdWrapper(context.getRuntime(), oid);
    }

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
        return getRuntime().newString(_oid == null ? "ObjectId(null)" : _oid.toString());
    }
}
