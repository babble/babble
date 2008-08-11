// Level.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.log;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public enum Level {
    DEBUG , INFO , WARN , ERROR , FATAL ;

    static Level forName( String name ){
        if ( name.equalsIgnoreCase( "debug" ) )
            return DEBUG;

        if ( name.equalsIgnoreCase( "info" ) )
            return INFO;

        if ( name.equalsIgnoreCase( "warn" ) )
            return WARN;
	
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
        me.set( "WARN" , WARN );
        me.set( "ERROR" , ERROR );
        me.set( "FATAL" , FATAL );
	
        me.set( "ALL" , DEBUG );
    }
}
