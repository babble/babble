// JSCompiledScript.java

package ed.js.engine;

import java.util.*;

import org.mozilla.javascript.*;

import ed.lang.*;
import ed.util.*;
import ed.js.*;
import ed.js.func.*;

public abstract class JSCompiledScript extends JSFunctionCalls0 {
 
    protected abstract Object _call( Scope scope , Object extra[] ) throws Exception;
    
    public Object call( Scope scope , Object extra[] ){
        try {
            return _call( scope, extra );
        }
        catch ( RuntimeException re ){
            if ( Convert.DJS ) re.printStackTrace();
            _convert.fixStack( re );
            throw re;
        }
        catch ( Exception e ){
            e.printStackTrace();
            if ( Convert.DJS ) e.printStackTrace();
            _convert.fixStack( e );
            throw new RuntimeException( "weird error : " + e.getClass().getName() , e );
        }
    }
    
    protected void _throw( Object foo ){
        
        if ( foo instanceof JSException )
            throw ( JSException)foo;
        
        if ( foo instanceof Throwable )
            throw new JSException( foo.toString() , (Throwable)foo );
        
        throw new JSException( foo );
    }

    public Language getFileLanguage(){
        if ( _convert == null )
            return Language.JS;
        return _convert._sourceLanguage;
    }

    Convert _convert;
    protected ed.js.JSRegex _regex[];
    protected ed.js.JSString _strings[];
}
