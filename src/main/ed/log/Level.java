// Level.java

package ed.log;

import ed.js.*;

public enum Level {
    DEBUG , INFO , ERROR , FATAL ;
    
    static JSObject me = new JSObjectBase();
    static {
        me.set( "DEBUG" , DEBUG );
        me.set( "INFO" , INFO );
        me.set( "ERROR" , ERROR );
        me.set( "FATAL" , FATAL );
    }
}
