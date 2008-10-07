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
import org.jruby.runtime.callback.Callback;
import static org.jruby.runtime.Visibility.PRIVATE;

import ed.db.ObjectId;

/**
 * RubyDBCursorWrapper is a RubyArray.
 */
public class RubyObjectIdWrapper extends RubyObject {

    static Map<Ruby, WeakReference<RubyClass>> klassDefs = new WeakHashMap<Ruby, WeakReference<RubyClass>>();

    public static synchronized RubyClass getObjectIdClass(final Ruby runtime) {
        WeakReference<RubyClass> ref = klassDefs.get(runtime);
        if (ref == null) {
            RubyClass klazz = runtime.defineClass("ObjectId", runtime.getObject(), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
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

    RubyObjectIdWrapper(Ruby runtime, ObjectId oid) {
        super(runtime, getObjectIdClass(runtime));
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("creating RubyObjectIdWrapper");
        _oid = oid;
    }

    public ObjectId getObjectId() { return _oid; }

    protected RubyArray notImplemented(String what) {
        throw getRuntime().newNotImplementedError("Can not call " + what + " on database cursors");
    }

    public IRubyObject to_s() {
        return getRuntime().newString(_oid.toString());
    }
}
