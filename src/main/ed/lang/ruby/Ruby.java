// Ruby.java

package ed.lang.ruby;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public class Ruby {
    
    public static final String RUBY_V_CALL = "_rubyVCall";

    public static void install( Scope s ){
        s.put( RUBY_V_CALL , new JSFunctionCalls1(){
                public Object call( Scope s , Object foo , Object extra[] ){

                    if ( foo == null )
                        return null;
                    
                    if ( foo instanceof JSFunction )
                        return ((JSFunction)foo).call( s );
                
                    return foo;
                }
            } , true );
    }
}
