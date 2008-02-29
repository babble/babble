package ed.js.engine;
import ed.log.*;
import ed.js.*;
import ed.js.func.*;

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
}
