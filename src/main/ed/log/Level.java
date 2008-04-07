// Level.java

package ed.log;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public enum Level {
    DEBUG , INFO , ERROR , FATAL ;

    JSFunction func = new JSFunctionCalls2(){
            public Object call( Scope s , Object msgObject , Object excObject , Object extra[] ){

		if ( msgObject == null )
		    msgObject = "null";

                Object t = s.getThis();
                if ( t == null || ! ( t instanceof Logger ) )
                    throw new RuntimeException( "has to be called with this being a Logger" );
                
                Logger l = (Logger)t;
                l.log( Level.this , msgObject.toString() , (Throwable)excObject );
                return true;
            }
        };
    
    static Level forName( String name ){
        if ( name.equalsIgnoreCase( "debug" ) )
            return DEBUG;

        if ( name.equalsIgnoreCase( "info" ) )
            return INFO;
        
        if ( name.equalsIgnoreCase( "error" ) )
            return ERROR;

        if ( name.equalsIgnoreCase( "fatal" ) )
            return FATAL;

        throw new RuntimeException( "unknown level : " + name );
    }

    static JSObject me = new JSObjectBase();
    static {
        me.set( "DEBUG" , DEBUG );
        me.set( "INFO" , INFO );
        me.set( "ERROR" , ERROR );
        me.set( "FATAL" , FATAL );
    }
}
