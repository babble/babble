// ReflectionWalker.java


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

import java.util.*;
import java.lang.reflect.*;

public class ReflectionWalker {

    public ReflectionWalker( ReflectionVisitor visitor ){
        _visitor = visitor;
    }

    public void walk( final Object start ){
        
        if ( start == null )
            return;

        LinkedList togo = new LinkedList();
        togo.add( start );

        while ( ! togo.isEmpty() ){

            final Object o = togo.removeLast();

            if ( o == null )
                continue;
            
            final Class c = o.getClass();

            if ( ! _visitor.visit( o , c ) )
                continue;
            
            if ( c.isArray() ){
                final int length = Array.getLength( o );
                for ( int i=0; i<length; i++ )
                    if ( _visitor.follow( o , c , null ) )
                        togo.add( Array.get( o , i ) );
            }
            
            Field[] fields = c.getDeclaredFields();
            for ( int i=0; i<fields.length; i++ ){
                
                if ( ! _visitor.follow( o , c , fields[i] ) )
                    continue;
                
                try {
                    fields[i].setAccessible( true );
                    togo.add( fields[i].get( o ) );
                }
                catch ( IllegalAccessException e ){
                    throw new RuntimeException( "can't access a field" , e );
                }
            }
        }
        
    }

    final ReflectionVisitor _visitor;

    public static List<Object> shortestPath( Object from , Object to ){
        ReflectionVisitor.ShortestPathFinder spf = new ReflectionVisitor.ShortestPathFinder( to );
        ReflectionWalker walker = new ReflectionWalker( spf );
        walker.walk( from );
        return null;
    }

    
}

