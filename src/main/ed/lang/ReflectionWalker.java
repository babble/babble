// ReflectionWalker.java


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

