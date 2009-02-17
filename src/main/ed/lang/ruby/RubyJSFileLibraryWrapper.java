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
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.appserver.JSFileLibrary;
import ed.js.JSObject;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;
import static ed.lang.ruby.RubyObjectWrapper.toJSFunctionArgs;

/**
 * Wraps JSFileLibrary objects for Ruby.
 * <p>
 * This wrapper implements peek() in a way that makes sure the file library
 * object is not initialized. It also makes sure newly-defined classes are
 * created after every call to itself.
 */
@SuppressWarnings("serial")
public class RubyJSFileLibraryWrapper extends RubyJSFunctionWrapper {

    public static synchronized RubyClass getJSFileLibraryClass(Ruby runtime) {
        RubyClass klazz = runtime.getClass("JSFileLibrary");
        if (klazz == null) {
            klazz = runtime.defineClass("JSFileLibrary", RubyJSFunctionWrapper.getJSFunctionClass(runtime), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
            klazz.kindOf = new RubyModule.KindOf() {
                    public boolean isKindOf(IRubyObject obj, RubyModule type) {
                        return obj instanceof RubyJSFileLibraryWrapper;
                    }
                };
        }
        return klazz;
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
                    Object result = _func.callAndSetThis(_scope, _this, toJSFunctionArgs(_scope, runtime, args, 0, block));
                    if (RubyObjectWrapper.DEBUG_FCALL)
                        System.err.println("func " + name + " returned " + result + ", which is " + (result == null ? "null" : ("of class " + result.getClass().getName())));
                    RuntimeEnvironment.createNewClassesAndXGenMethods();
                    return toRuby(_scope, runtime, result);
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
