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

import java.util.Map;
import java.util.WeakHashMap;

import org.jruby.*;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.appserver.JSFileLibrary;
import ed.js.JSObject;
import ed.js.engine.Scope;

/**
 * The JSFileLibrary wrapper implements peek() in a way that makes sure the
 * file library object is not initialized. It also makes sure newly-defined
 * classes are created after every call to itself.
 */
public class RubyJSFileLibraryWrapper extends RubyJSFunctionWrapper {

    static Map<Ruby, RubyClass> klassDefs = new WeakHashMap<Ruby, RubyClass>();

    public static synchronized RubyClass getJSFileLibraryClass(Ruby runtime) {
	RubyClass jsFileLibraryClass = klassDefs.get(runtime);
	if (jsFileLibraryClass == null) {
	    jsFileLibraryClass = runtime.defineClass("JSFileLibrary", RubyJSFunctionWrapper.getJSFunctionClass(runtime), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
	    jsFileLibraryClass.kindOf = new RubyModule.KindOf() {
		    public boolean isKindOf(IRubyObject obj, RubyModule type) {
			return obj instanceof RubyJSFileLibraryWrapper;
		    }
		};
	    klassDefs.put(runtime, jsFileLibraryClass);
	}
	return jsFileLibraryClass;
    }

    RubyJSFileLibraryWrapper(Scope s, Ruby runtime, JSFileLibrary obj, String name, RubyModule attachTo) {
	this(s, runtime, obj, name, attachTo, null);
    }

    RubyJSFileLibraryWrapper(Scope s, Ruby runtime, JSFileLibrary obj, String name, RubyModule attachTo, JSObject jsThis) {
	super(s, runtime, obj, name, getJSFileLibraryClass(runtime), jsThis);
	if (RubyObjectWrapper.DEBUG_CREATE)
	    System.err.println("  creating RubyJSFileLibraryWrapper named " + name);
	JavaMethod jm = _makeCallMethod(attachTo);
	if (name != null && name.length() > 0) {
	    if (attachTo != null)
		_addMethod(name, jm, attachTo);
	}
	else if (attachTo != null)
	    attachTo.addMethod("call", jm);
    }

    /** Return the value at <var>key</var> without initializing it. */
    public Object peek(Object key) { return ((JSFileLibrary)_jsobj).get(key, false); }

    /** After calling this as a function, create any newly-defined classes. */
    protected JavaMethod _makeCallMethod(RubyModule attachTo) {
	if (attachTo == null)
	    return null;
	return new JavaMethod(attachTo, PUBLIC) {
	    public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
		Ruby runtime = context.getRuntime();
		if (RubyObjectWrapper.DEBUG_FCALL)
		    System.err.println("calling method " + module.getName() + "#" + name + " with " + args.length + " args");
		try {
		    Object result = _func.callAndSetThis(_scope, _this, RubyObjectWrapper.toJSFunctionArgs(_scope, runtime, args, 0, block));
		    if (RubyObjectWrapper.DEBUG_FCALL)
			System.err.println("func " + name + " returned " + result + ", which is " + (result == null ? "null" : ("of class " + result.getClass().getName())));
		    RubyJxpSource.createNewClasses(_scope, runtime);
		    return toRuby(result);
		}
		catch (Exception e) {
		    if (RubyObjectWrapper.DEBUG_SEE_EXCEPTIONS) {
			System.err.println("saw exception; going to raise Ruby error after printing the stack trace here");
			e.printStackTrace();
		    }
		    self.callMethod(context, "raise", new IRubyObject[] {runtime.newString(e.toString())}, Block.NULL_BLOCK);
		    return runtime.getNil(); // will never reach
		}
	    }
	};
    }
}
