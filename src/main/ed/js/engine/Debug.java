// Debug.java

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

        System.out.print( Token.name( n.getType() ) + " [" + n.getClass().getName().replaceAll( "ed.ext.org.mozilla.javascript." , "" ) + "]"  );

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

        StringBuffer sb = new StringBuffer();
        n.handlePropList( sb );
        System.out.print( " " + sb );

        System.out.print( " line:" + n.getLineno() );

        System.out.println();
        
        printTree( n.getFirstChild() , indent + 1 );
        printTree( n.getNext() , indent );
    }
    

}
