// JSBoolean.java

package ed.js;

import java.util.*;

import ed.util.*;
import ed.js.func.*;
import ed.js.engine.*;

public class JSBoolean {


    public static JSObjectBase functions = new JSObjectBase();
    public static JSFunction getFunction( String name ){
        return (JSFunction)functions.get( name );
    }

    static {

    }
}

