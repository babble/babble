// Debug.java

package ed.js.engine;

import org.mozilla.javascript.*;

public class Debug {

    public static void print( ScriptOrFnNode sn , int indent ){
        for ( int i=0; i<sn.getFunctionCount(); i++ ){
            FunctionNode fn = sn.getFunctionNode( i );
            print( fn , indent );
        }
        printTree( sn , indent );
    }

    public static void printTree( Node n , int indent ){
        if ( n == null )
            return;
        
        for ( int i=0; i<indent; i++ )
            System.out.print( "  " );

        System.out.print( Token.name( n.getType() ) + " [" + n.getClass().getName().replaceAll( "org.mozilla.javascript." , "" ) + "]"  );

        if ( n instanceof FunctionNode )
            System.out.print( " " + ((FunctionNode)n).getFunctionName() );


        if ( n.getType() == Token.FUNCTION ){
            int id = n.getIntProp( Node.FUNCTION_PROP , -17 );
            if ( id != -17 )
                System.out.print( " functionId=" + id );
        }
        
        if ( NodeUtil.hasString( n ) )
            System.out.print( " [" + n.getString() + "]" );
        
        if ( n.getType() == Token.NUMBER )
            System.out.print( " NUMBER:" + n.getDouble() );
        
        //System.out.print( " " + n.toString().replaceAll( "^\\w+ " , "" ) );
        
        if ( n instanceof Node.Jump ){
            Node.Jump j = (Node.Jump)n;
            if ( j.target != null )
                System.out.print( " -> " + j.target.hashCode() );
        }

        System.out.print( " (" + n.hashCode() + ")" );

        System.out.print( " line:" + n.getLineno() );

        System.out.println();
        
        printTree( n.getFirstChild() , indent + 1 );
        printTree( n.getNext() , indent );
    }
    

}
