// E4X.java

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
        ENode result = (ENode)n.get( _what );
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
