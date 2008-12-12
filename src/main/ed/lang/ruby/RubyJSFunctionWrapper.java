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

import java.util.*;

import org.jruby.*;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.callback.Callback;
import org.jruby.util.IdUtil;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.js.*;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;
import static ed.lang.ruby.RubyObjectWrapper.toJSFunctionArgs;

/**
 * RubyJSFunctionWrapper acts as a bridge between Ruby code and JavaScript
 * functions. An instance of RubyJSFunctionWrapper is a Ruby object that can
 * call a JSFunction. If we are given a function name and a class, then a
 * method with that name is added to the class.
 *
 * @see JSFunctionWrapper
 */
@SuppressWarnings("serial")
public class RubyJSFunctionWrapper extends RubyJSObjectWrapper {

    /**
     * Class names that should <em>not</em> be turned in to Ruby classes when
     * looking at scope contents.
     */
    static final String[] IGNORE_JS_CLASSES = {
        "Array", "Base64", "Class", "Date", "Exception", "Math", "Number", "Object", "ObjectId", "Regexp", "String", "XML"
    };
    /**
     * Function names that should <em>not</em> become aliases.
     */
    static final String[] IGNORE_FUNCTIONS = {
        "object_id"
    };

    protected JSFunction _func;
    protected JSObject _this;
    protected RubyModule _module;

    public static synchronized RubyClass getJSFunctionClass(Ruby runtime) {
        RubyClass klazz = runtime.getClass("JSFunction");
        if (klazz == null) {
            klazz = runtime.defineClass("JSFunction", RubyJSObjectWrapper.getJSObjectClass(runtime), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
            klazz.kindOf = new RubyModule.KindOf() {
                    public boolean isKindOf(IRubyObject obj, RubyModule type) {
                        return obj instanceof RubyJSFunctionWrapper;
                    }
                };
        }
        return klazz;
    }

    public static boolean canBeNewClass(Ruby runtime, String name) {
        if (!IdUtil.isConstant(name))
            return false;
        for (int i = 0; i < IGNORE_JS_CLASSES.length; ++i)
            if (IGNORE_JS_CLASSES[i].equals(name))
                return false;
        return runtime.getClass(name) == null;
    }

    RubyJSFunctionWrapper(Scope s, Ruby runtime, JSFunction obj, String name, RubyModule attachTo) {
        this(s, runtime, obj, name, attachTo, null);
    }

    RubyJSFunctionWrapper(Scope s, Ruby runtime, JSFunction obj, String name, RubyModule attachTo, JSObject jsThis) {
        super(s, runtime, obj, getJSFunctionClass(runtime));
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("  creating RubyJSFunctionWrapper named " + name);
        _func = obj;
        _this = jsThis;
        if (name != null && name.length() > 0) {
            if (attachTo != null)
                _addMethod(name, _makeCallMethod(attachTo), attachTo);
            if (canBeNewClass(runtime, name))
                _createJSObjectSubclass(s, name);
        }
        else if (attachTo != null)
            attachTo.addMethod("call", _makeCallMethod(attachTo));
    }

    /** For use by RubyJSFileLibraryWrapper subclass. */
    protected RubyJSFunctionWrapper(Scope s, Ruby runtime, JSFunction func, RubyClass klazz, JSObject jsThis) {
        super(s, runtime, func, klazz);
        _func = func;
        _this = jsThis;
    }

    /**
     * This is only used by {@link RubyJSObjectWrapper#_replaceFunctionMethod}
     * when it is rebuilding an object.
     */
    void setFunction(JSFunction func) {
        _func = func;
    }

    protected JavaMethod _makeCallMethod(RubyModule module) {
        if (module == null)
            return null;
        return new JavaMethod(module, PUBLIC) {
            public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
                Ruby runtime = context.getRuntime();
                if (RubyObjectWrapper.DEBUG_FCALL)
                    System.err.println("calling method " + module.getName() + "#" + name + " with " + args.length + " args");
                try {
                    Object result = _func.callAndSetThis(_scope, _this, toJSFunctionArgs(_scope, runtime, args, 0, block));
                    if (RubyObjectWrapper.DEBUG_FCALL)
                        System.err.println("func " + name + " returned " + result + ", which is " + (result == null ? "null" : ("of class " + result.getClass().getName())));
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

    /** Adds this method to <var>module</var>. */
    protected void _addMethod(String name, JavaMethod jm, RubyModule module) {
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("adding method named " + name + " to module " + module.getName());
        String internedName = name.intern();
        RubySymbol sym = getRuntime().fastNewSymbol(internedName);
        module.addMethod(internedName, jm);
        module.callMethod(getRuntime().getCurrentContext(), "method_added", sym);

        if (!Character.isUpperCase(name.charAt(0))) { // don't alias class names (which can also be function names)
            String rubyName = JavaUtil.getRubyCasedName(name);
            if (!name.equals(rubyName) && _okToAlias(rubyName))
                module.alias_method(getRuntime().getCurrentContext(), getRuntime().fastNewSymbol(rubyName.intern()), sym);
        }
    }

    protected boolean _okToAlias(String rubyName) {
        for (String badName : IGNORE_FUNCTIONS)
            if (badName.equals(rubyName))
                return false;
        return true;
    }

    /** An allocator for objects created using JSFunction constructors. */
    class JSObjectAllocator implements ObjectAllocator {
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            if (RubyObjectWrapper.DEBUG_CREATE || RubyObjectWrapper.DEBUG_FCALL)
                System.err.println("allocating an instance of " + klass.name());
            JSObject jsobj = _func.newOne();
            RubyObject r = (RubyObject)new RubyJSObjectWrapper(_scope, runtime, jsobj, klass);
            /* Eigenclass has been created and used in RubyJSObjectWrapper ctor, so make that the metaclass here. */
            r.makeMetaClass(r.getSingletonClass());
            if (RubyObjectWrapper.DEBUG_CREATE || RubyObjectWrapper.DEBUG_FCALL)
                System.err.println("  wrapped new object inside a " + r.getClass().getName());
            return r;
        }
    }
    protected JSObjectAllocator jsObjectAllocator = new JSObjectAllocator();

    /** Creates a subclass of JSObject for that uses this method as its constructor. */
    protected void _createJSObjectSubclass(final Scope scope, String name) {
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("adding class named " + name);
        _module = getRuntime().defineClass(name, RubyJSObjectWrapper.getJSObjectClass(getRuntime()), jsObjectAllocator);
        // FIXME
        _module.kindOf = new RubyModule.KindOf() {
                    public boolean isKindOf(IRubyObject obj, RubyModule type) {
                        return obj instanceof RubyJSObjectWrapper;
                    }
            };
        _module.defineMethod("initialize", new Callback() {
                public IRubyObject execute(IRubyObject self, IRubyObject[] args, Block block) {
                    if (RubyObjectWrapper.DEBUG_FCALL) {
                        System.err.println("calling " + ((RubyObject)self).type().name() + "#initialize with " + args.length + " args" + (args.length == 0 ? "" : ':'));
                        for (IRubyObject iro : args) System.err.println("  " + iro.toString());
                    }
                    JSObject jsobj = ((RubyJSObjectWrapper)self).getJSObject();
                    Ruby runtime = self.getRuntime();
                    try {
                        _func.callAndSetThis(scope, jsobj, toJSFunctionArgs(scope, runtime, args, 0, block)); // initialize it by calling _func
                        if (RubyObjectWrapper.DEBUG_FCALL)
                            System.err.println("back from initialize");
                    }
                    catch (Exception e) {
                        if (RubyObjectWrapper.DEBUG_SEE_EXCEPTIONS) {
                            System.err.println("saw exception; going to raise Ruby error after printing the stack trace here");
                            e.printStackTrace();
                        }
                        self.callMethod(runtime.getCurrentContext(), "raise", new IRubyObject[] {runtime.newString(e.toString())}, Block.NULL_BLOCK);
                    }
                    return self;
                }
                public Arity getArity() { return Arity.OPTIONAL; }
            });
    }
}
