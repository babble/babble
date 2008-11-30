// SeenPath.java

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

public class SeenPath extends IdentityHashMap {
    
    public boolean shouldVisit( Object toVisit , Object from ){
        
        if ( toVisit == null )
            return false;

        if ( inSpecialDontTraverse( toVisit ) )
            return false;

        if ( from == null )
            from = UNKNOWN;
        
        final Object prev = get( toVisit );
        if ( prev == null ){
            put( toVisit , from );
            return true;
        }

        if ( prev == UNKNOWN ){
            // we want to add some pathing info, but not follow
            put( toVisit , from );
            return false;
        }
        
        return false;
    }
    
    public boolean contains( Object o ){

        if ( inSpecialDontTraverse( o ) )
            return true;

        return containsKey( o );
    }

    public void visited( Object toVisit ){
        if ( containsKey( toVisit ) )
            return;
        put( toVisit , UNKNOWN );
    }

    public void removeAll( Set objects ){
        for ( Object o : objects )
            remove( o );
    }

    public void pushSpecialDontTraverse( Set s ){
        _specialDontTraverse.add( s );
    }

    public void popSpecialDontTraverse(){
        _specialDontTraverse.remove( _specialDontTraverse.size() - 1 );
    }
    
    boolean inSpecialDontTraverse( Object o ){
        for ( Set s : _specialDontTraverse )
            if ( s.contains( o ) )
                return true;
        return false;
    }

    final List<Set> _specialDontTraverse = new ArrayList<Set>();

    public static final Object UNKNOWN = new Object(){
            public String toString(){
                return "unkown path";
            }
        };
}
