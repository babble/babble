// NodeUtil.java

package ed.js.engine;

import org.mozilla.javascript.*;

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

}
