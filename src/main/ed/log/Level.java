// Level.java

package ed.log;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public enum Level {
    DEBUG , INFO , ERROR , FATAL ;

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

        me.set( "ALL" , DEBUG );
    }
}
