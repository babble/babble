// ReflectionVisitor.java


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

package ed.lang;

import java.lang.reflect.*;

import ed.util.*;

public interface ReflectionVisitor {
    
    public boolean visit( Object o , Class c );
    public boolean follow( Object o , Class c , Field f );


    public static class Reachable implements ReflectionVisitor {
        
        public boolean visit( Object o , Class c ){
            if ( _seen.contains( o ) )
                return false;

            _seen.add( o );
            return true;
        }
        
        public boolean follow( Object o , Class c , Field f ){
            if ( o instanceof Number ||
                 o instanceof Boolean || 
                 o instanceof String || 
                 o instanceof Byte || 
                 o instanceof Character )
                return false;
            return true;
        }

        public int seenSize(){
            return _seen.size();
        }
        
        final IdentitySet _seen = new IdentitySet();
    }
}

