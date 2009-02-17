// E4X.java

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

package ed.js.e4x;

import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import ed.js.*;
/*import ed.js.func.*;
import ed.js.engine.*;
import ed.util.*;
*/
public class Query {
    private enum types { 
        LT() {
            public boolean attributeCompare( String foo, String bar ) {
                return foo.compareTo( bar ) < 0;
            }
            public boolean valueCompare( Object o, String s ) {
                return JSInternalFunctions.JS_lt( o , s );
            }
        }, LE() {
            public boolean attributeCompare( String foo, String bar ) {
                return foo.compareTo( bar ) <= 0;
            }
            public boolean valueCompare( Object o, String s ) {
                return JSInternalFunctions.JS_le( o , s );
            }
        }, GT() {
            public boolean attributeCompare( String foo, String bar ) {
                return foo.compareTo( bar ) > 0;
            }
            public boolean valueCompare( Object o, String s ) {
                return JSInternalFunctions.JS_gt( o , s );
            }
        }, GE() {
            public boolean attributeCompare( String foo, String bar ) {
                return foo.compareTo( bar ) >= 0;
            }
            public boolean valueCompare( Object o, String s ) {
                return JSInternalFunctions.JS_ge( o, s );
            }
        }, EQ() {
            public boolean attributeCompare( String foo, String bar ) {
                return foo.compareTo( bar ) == 0;
            }
            public boolean valueCompare( Object o, String s ) {
                return JSInternalFunctions.JS_eq( o, s );
            }
        }, NAME() {
            public boolean attributeCompare( String foo, String bar ) {
                return true;
            }
            public boolean valueCompare( Object o, String s ) {
                return true;
            }
        };
        /** Do the comparison op we know & love */
        public abstract boolean attributeCompare( String foo, String bar );
        public abstract boolean valueCompare( Object o, String s );
    };


    public Query( String what , String match, String type ){
        _what = what;
        _match = match;
        _type = types.valueOf( type );
    }

    boolean match( ENode n ){
        ENode result = E4X.getENode( ((XMLList)n.get( _what )).get( 0 ) );

        if( result == null || result.isDummy() ) {
            return false;
        }
        if( result.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
            return _type.attributeCompare( result.node.getNodeValue() , _match );
        }
        return _type.valueCompare( E4X._nodeGet( n, _what ), _match );
    }

    public String toString(){
        return " [[ " + _what + " " + _type + " " + _match + " ]] ";
    }

    final String _what;
    final String _match;
    final types _type;
}
