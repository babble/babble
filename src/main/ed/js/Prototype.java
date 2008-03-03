package ed.js;
import ed.log.*;
import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;
import ed.util.*;

// Methods borrowed from Prototype
public class Prototype {

    /** Object.extend: copy properties from src to dest.
     *  @returns dest
     */
    public static class Object_extend extends JSFunctionCalls2 {
        public Object call( Scope s , Object dest , Object src , Object [] extra ){
            JSObject jdest, jsrc;
            jdest = (JSObject)dest;
            jsrc = (JSObject)src;
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
                    Object [] total = new Object [farguments.length + args.length];

                    for(int i = 0; i < farguments.length; i++){
                        total[i] = farguments[i];
                    }
                    for(int i = 0; i < args.length; i++){
                        total[i+farguments.length] = args[i];
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
    public static final JSFunction _functionBind = new Function_bind();

    /** Function.wrap: ???
     * 
     */
    public static class Function_wrap extends JSFunctionCalls1 {
        public Object call( Scope s , Object f , Object [] arguments){
            return null;
        }
    }
}
