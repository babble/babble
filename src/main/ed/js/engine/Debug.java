// Debug.java

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
