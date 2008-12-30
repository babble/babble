// NodeUtil.java

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
