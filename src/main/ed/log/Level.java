// Level.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.log;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public enum Level {

    DEBUG_7 , DEBUG_6 , DEBUG_5 , DEBUG_4 , DEBUG_3 , DEBUG_2 , DEBUG_1 , 
	DEBUG , INFO , WARN , ERROR , ALERT , FATAL ;
    
    public static Level forDebugId( int id ){
	int idx = ( -1 * id ) + DEBUG_LEVELS;

	if ( idx < 0 )
	    idx = 0;
	
	if ( idx >= LEVELS.length )
	    idx = LEVELS.length - 1;

	return LEVELS[ idx ];
    }
    
    public static Level forId( int id ){
	return LEVELS[ id + DEBUG_LEVELS ];
    }
    
    public static Level forName( String name ){
        if ( name.equalsIgnoreCase( "debug" ) )
            return DEBUG;

        if ( name.equalsIgnoreCase( "info" ) )
            return INFO;

        if ( name.equalsIgnoreCase( "warn" ) )
            return WARN;
	
        if ( name.equalsIgnoreCase( "error" ) )
            return ERROR;

        if ( name.equalsIgnoreCase( "alert" ) )
            return ALERT;

        if ( name.equalsIgnoreCase( "fatal" ) )
            return FATAL;

        throw new RuntimeException( "unknown level : " + name );
    }

    static final JSObject me = new JSObjectBase();
    static final Level[] LEVELS;
    static final int DEBUG_LEVELS;
    static {
        me.set( "DEBUG" , DEBUG );
        me.set( "INFO" , INFO );
        me.set( "WARN" , WARN );
        me.set( "ERROR" , ERROR );
        me.set( "ALERT" , ALERT );
        me.set( "FATAL" , FATAL );
        
        me.set( "ALL" , DEBUG );
	
	LEVELS = Level.values();
	int debug = 0;
	for ( int i=0; i<LEVELS.length; i++ )
	    if ( LEVELS[i].name().equalsIgnoreCase( "info" ) )
		debug = i;
	DEBUG_LEVELS = debug;
    }
}
