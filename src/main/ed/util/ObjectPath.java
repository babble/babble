// ObjectPath.java

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

package ed.util;

import java.util.*;

import ed.js.*;
import ed.js.engine.*;

public class ObjectPath extends ArrayList {
    
    static String pathElementToString( Object o ){
        String s = o.getClass().getName();
        
        if ( o instanceof Scope  
             || o instanceof Number
             || o instanceof JSString
             || o instanceof String )
            s += "(" + o + ")";
        
        if ( o instanceof JSObjectBase )
            s += "(" + ((JSObjectBase)o)._getName() + ")";
        
        return s;
    }

    public String toString(){
        StringBuilder buf = new StringBuilder( size() * 20 );
        buf.append( "[" );
        for ( int i=0; i<size(); i++ ){
            if ( i > 0 )
                buf.append( ", " );
            buf.append( pathElementToString( get(i ) ) );
        }
        buf.append( "]" );

        return buf.toString();
    }

}
