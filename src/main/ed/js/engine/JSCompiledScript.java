// JSCompiledScript.java

package ed.js.engine;

import java.util.*;

import org.mozilla.javascript.*;

import ed.util.*;
import ed.js.*;
import ed.js.func.*;

public abstract class JSCompiledScript extends JSFunctionCalls0 {
 
    protected abstract Object _call( Scope scope , Object extra[] );
    
    public Object call( Scope scope , Object extra[] ){
        try {
            return _call( scope, extra );
        }
        catch ( RuntimeException re ){
            if ( Convert.D ) re.printStackTrace();
            _convert.fixStack( re );
            throw re;
        }
    }

    protected void _throw( Object foo ){
        if ( foo instanceof JSException )
            throw ( JSException)foo;
        throw new JSException( foo );
    }

    Convert _convert;
    protected ed.js.JSRegex _regex[];
    protected ed.js.JSString _strings[];
}
