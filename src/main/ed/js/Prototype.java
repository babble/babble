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

package ed.js;
import ed.log.*;
import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;
import ed.util.*;

/**  Methods borrowed from Prototype
 * @anonymous name : {Class.create} desc : {Used to create an psuedo-class in JavaScript, optionally setting the class's parent, prototype, constructor, and adding methods.}  param : {type : (function) name : (parent), desc : (parent class)}, param : { type : (Object), name : (methods), desc : (a collection of method name/body pairs to add to the class)} return : {type : (Class) desc : (the new class)}
 * @expose
 */
public class Prototype {

    /** Object.extend: copy properties from src to dest.
     *  @returns dest
     */
    public static class Object_extend extends JSFunctionCalls2 {
        public Object call( Scope s , Object dest , Object src , Object [] extra ){

            if ( dest == null )
                throw new RuntimeException( "Object.extends dest is null" );

            if ( src == null )
                throw new RuntimeException( "Object.extends src is null" );

            if ( ! ( dest instanceof JSObject ) )
                throw new RuntimeException( "Object.extends destination is not an object.  is a : " + dest.getClass() );

            if ( ! ( src instanceof JSObject ) )
                throw new RuntimeException( "Object.extends src is not an object.  is a : " + src.getClass() );

            JSObject jdest = (JSObject)dest;
            JSObject jsrc = (JSObject)src;
            for(String key : jsrc.keySet()){
                jdest.set(key, jsrc.get(key));
            }

            return dest;
        }
    }

    /** Object.keys: fetch all the property names from an object
     *  @returns a JavaScript Array, each element containing a key from the
     *  object
     */
    public static class Object_keys extends JSFunctionCalls1 {
        public Object call( Scope s , Object o , Object [] extra ){
            JSObject jo = (JSObject)o;
            JSArray jar = new JSArray();
            for(String key : jo.keySet()){
                jar.add(new JSString(key));
            }

            return jar;
        }
    }


    /** Object.values: fetch all the property values from an object
     *  @returns a JavaScript Array of the properties of the object
     */
    public static class Object_values extends JSFunctionCalls1 {
        public Object call( Scope s , Object o , Object [] extra ){
            JSObject jo = (JSObject)o;
            JSArray jar = new JSArray();
            for(String key : jo.keySet()){
                jar.add(jo.get(key));
            }

            return jar;
        }
    }

    /** $A: convert an object into an array
     *  prototype uses this all over, but I don't think we'll need it
    public static JSFunction dollarA = new JSFunctionCalls1(){
            public Object call( Scope s , Object iterable , Object [] extra ){
                if(iterable == null)
                    return new JSArray();
                JSObject jiterable = (JSObject)iterable;
                if(jiterable.get("toArray") != null){
                    JSObject jtoarray = (JSObject)jiterable.get("toArray");
                    if(jtoarray instanceof JSFunction){
                        JSFunction jfunc = (JSFunction)jtoarray;
                        return jfunc.call( s );
                    }
                }
                Object length = jiterable.get("length");
                int len = 0;
                if(length != null){
                    if(! (length instanceof Number) )
                        length = StringParseUtil.parseInt(length.toString(), 0);
                    len = ((Number)length).intValue();
                }

                JSArray ary = new JSArray(len);
                while (len-- > 0) ary.setInt(len, jiterable.get(len));
                return ary;
            }
        };
    */

    /** Function.bind: given a function and some arguments,
     *  return a new function that provides those arguments.
     *  i.e. g = f.bind(o, 1, 2);
     *  g(3) is now the same as f.call(o, 1, 2, 3).
     */
    public static class Function_bind extends JSFunctionCalls1 {

        public Object call( final Scope s , Object o , Object [] arguments ){

            final JSFunction f = (JSFunction)s.getThis();
            final Object t = o;
            final Object [] farguments = arguments;

            return new JSFunctionCalls0() {
                public Object call( Scope s , Object [] args ){
                    int totlen = 0;
                    if(farguments != null) totlen += farguments.length;
                    if(args != null) totlen += args.length;
                    Object [] total = new Object [totlen];

                    int j = 0; // where we are in total
                    if(farguments != null){
                        for(int i = 0; i < farguments.length; i++, j++){
                            total[j] = farguments[i];
                        }
                    }

                    if(args != null){
                        for(int i = 0; i < args.length; i++, j++){
                            total[j] = args[i];
                        }
                    }

                    s.setThis(t);
                    try {
                        Object res = f.call(s, total);
                        return res;
                    }
                    finally {
                        s.clearThisNormal( null );
                    }
                }
            };
        }
    }
    /** @unexpose  */
    public static final JSFunction _functionBind = new Function_bind();

    /** Function.wrap: creates a wrapper function around this function
     *  See: http://www.prototypejs.org/api/function/wrap
     *  @param f  the function to wrap around this function
     */
    public static class Function_wrap extends JSFunctionCalls1 {
        public Object call( final Scope s , final Object f , final Object [] arguments){
            final JSFunction __method = (JSFunction)s.getThis();
            final JSFunction wrapper = (JSFunction)f;
            return new JSFunctionCalls0() {
                public Object call( Scope s , Object [] args ){
                    int totlen = 1;
                    if(args != null) totlen += args.length;
                    Object [] total = new Object [totlen];

                    Object tempthis = s.getThis();
                    s.setThis(__method);
                    try {
                        total[0] = _functionBind.call(s, tempthis, null);
                    }
                    finally {
                        s.clearThisNormal(null);
                    }

                    if(args != null){
                        for(int i = 0; i < args.length; i++){
                            total[i+1] = args[i];
                        }
                    }

                    //return new Integer(5);
                    return wrapper.call(s, total);
                }
            };

        }
    }
    /** @unexpose  */
    public static final JSFunction _functionWrap = new Function_wrap();

    public static class Class_create extends JSFunctionCalls0 {
        public Object call( Scope s, Object [] properties){
            int i = 0;
            Object parent = null;
            if(properties != null && properties.length > 0 && properties[0] instanceof JSFunction) {
                parent = properties[i++];
            }
            JSFunction klass = new JSFunctionCalls0 () {
                    public Object call( Scope s, Object [] extra){
                        JSObject t = (JSObject)s.getThis();
                        JSFunction init = (JSFunction)t.get("initialize");
                        init.call(s, extra);
                        return null;
                    }
                };
            klass.set("superclass", parent);
            // add subclasses attribute here

            if(parent != null) {
                JSFunction subclass = new JSFunctionCalls0 () {
                        public Object call( Scope s, Object [] extra){
                            return null;
                        }
                    };

                subclass.set("prototype", ((JSObject)parent).get("prototype"));
                klass.set("prototype", new JSObjectBase(subclass));
                // push subclass here
            }

            klass.set("addMethods", Prototype._classAddMethods);

            s.setThis(klass);
            if(properties != null)
                for(; i<properties.length; i++){
                    _classAddMethods.call(s, properties[i]);
                }
            s.clearThisNormal(true);

            JSObject prototype = (JSObject)klass.get("prototype");
            if(prototype.get("initialize") == null){
                prototype.set("initialize", new JSFunctionCalls0 () {
                        public Object call( Scope s, Object [] extra){
                            return null;
                        }
                    });
            }
            
            prototype.set("constructor", klass);

            return klass;

        }
    }

    /** @unexpose  */
    public static final JSFunction _classCreate = new Class_create();

    public static class Class_addMethods extends JSFunctionCalls1 {
        public Object call( Scope s, Object source, Object [] extra){
            JSObject t = (JSObject)s.getThis();
            Object anc = null;
            if(t.get("superclass") instanceof JSObject)
                anc = ((JSObject)t.get("superclass")).get("prototype");
            final Object ancestor = anc;
            JSObject jo = (JSObject)source;
            for(String property : jo.keySet()){
                Object value = jo.get(property);
                if (ancestor != null && value instanceof JSFunction){
                    JSFunction valf = (JSFunction)value;
                    Integer args = (Integer)valf.argumentNames().get("length");
                    if(args > 0 &&
                       ((JSFunction)value).argumentNames().getInt(0).equals("$super")) {
                        final String methodname = property;
                        final Object method = value;

                        value = new JSFunctionCalls0 () {
                                public Object call( Scope s, Object [] extra){
                                    JSFunction m = (JSFunction)((JSObject)ancestor).get(methodname);
                                    return m.call(s, extra);
                                }
                            };

                        s.setThis(value);
                        value = Prototype._functionWrap.call(s, method);

                        s.clearThisNormal(true);

                        ((JSObject)value).set("valueOf", new JSFunctionCalls0 () {
                                public Object call( Scope s, Object [] extra){
                                    return method;
                                }
                            });
                        ((JSObject)value).set("toString", new JSFunctionCalls0 () {
                                public Object call( Scope s, Object [] extra){
                                    return method.toString();
                                }
                            });
                    }
                }
                JSObject prototype = (JSObject)t.get("prototype");
                prototype.set(property, value);
            }
            return this;
        }
    }

    /** @unexpose  */
    public static final JSFunction _classAddMethods = new Class_addMethods();
    static {
	_classAddMethods.lock();
    }

    public static JSObjectBase newCopy(){
	JSObjectBase c = new JSObjectBase();
        
	JSObject m = new JSObjectBase();
        m.set("addMethods", _classAddMethods);
	
        c.set("Methods", m);
        c.set("create", _classCreate);

	return c;
    }

}
