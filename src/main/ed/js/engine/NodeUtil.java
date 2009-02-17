// NodeUtil.java

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

package ed.js.engine;

import ed.ext.org.mozilla.javascript.*;

import java.util.*;

public class NodeUtil {
    

    /**
     * returns an iterator over all children and siblings
     * order is not-defined
     */
    static Iterator<Node> childIterator( final Node start ){

        final List<Node> toSearch = new LinkedList<Node>();
        
        toSearch.add( start );
        
        return new Iterator<Node>(){
            
            public boolean hasNext(){
                return ! toSearch.isEmpty();
            }

            public Node next(){
                Node n = toSearch.remove( 0 );

                if ( n.getNext() != null )
                    toSearch.add( n.getNext() );
                if ( n.getFirstChild() != null )
                    toSearch.add( n.getFirstChild() );
                
                return n;
                
            }
            
            public void remove(){
                throw new RuntimeException( "remove not supported" );
            }

        };
        
    }

    static boolean hasString( Node n ){
        return n.getClass().getName().indexOf( "StringNode" ) >= 0;
    }


    static int hash( Node n ){
        
        if ( n == null )
            return 0;

        if ( n.getType() == Token.TARGET )
            n = n.getNext();
        
        if ( n == null )
            return 1;

        int hash = n.getClass().getName().toString().hashCode();
        
        if ( NodeUtil.hasString( n ) )
            hash += ( 7 * n.getString().hashCode() );

        if ( n.getType() == Token.NUMBER )
            hash += (int)( 11 * n.getDouble() );
        
        if ( n.getType() == Token.FUNCTION )
            hash += ( 711 * n.getIntProp( Node.FUNCTION_PROP , -17 ) );
        
        hash += n.getLineno() * 17;
        
        Node child = n.getFirstChild();
        while ( child != null ){
            hash += 11 * hash( child );
            child = child.getNext();
        }

        return hash;
    }


}
