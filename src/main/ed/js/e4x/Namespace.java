// Namespace.java

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
import java.util.regex.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.util.*;

public class Namespace extends JSObjectBase {

    public static JSFunction _cons = new Cons();

    public static class Cons extends JSFunctionCalls0 {

        public JSObject newOne(){
            return new Namespace();
        }

        public Object call( Scope scope , Object [] args){
            Object blah = scope.getThis();

            Namespace e;
            if ( blah instanceof Namespace) {
                e = (Namespace)blah;
             }
            else {
                e = new Namespace();
            }

            if( args.length == 1 ) {
                e.init( args[0].toString() );
            }
            if( args.length == 2 ) {
                e.init( args[0].toString(), args[1].toString() );
            }
           return e;
        }

        protected void init() {
            _prototype.set( "hasOwnProperty" , new JSFunctionCalls1() {
                    public Object call( Scope s, Object foo, Object extra[] ) {
                        if( foo == null )
                            throw new JSException( "hasOwnProperty must take a non-null argument." );
                        return propFunc( foo.toString() );
                    }
                });
            _prototype.set( "propertyIsEnumerable" , new JSFunctionCalls1() {
                    public Object call( Scope s, Object foo, Object extra[] ) {
                        if( foo == null )
                            throw new JSException( "propertyIsEnumerable must take a non-null argument." );
                        return propFunc( foo.toString() );
                    }
                });
        }

        private boolean propFunc( String foo ) {
            if( foo.equals( uriStr ) ||
                foo.equals( prefixStr ) )
                return true;
            return false;
        }
    }


    void init( Object s ) {
        init( null, s );
    }

    void init( String prefix, Object uri ) {
        if(prefix == null && uri == null) {
            this.prefix = "";
            this.uri = "";
        }
        else if (prefix == null) {
            if ( uri instanceof Namespace ) {
                this.prefix = ((Namespace)uri).prefix;
                this.uri = ((Namespace)uri).uri;
            }
            else if( uri instanceof QName ) {
                this.uri = ((QName)uri).uri;
            }
            else {
                this.uri = uri.toString();
                this.prefix = this.uri.equals("") ? "" : null;
            }
        }
        else {
            if( uri instanceof QName && ((QName)uri).uri != null) {
                this.uri = ((QName)uri).uri;
            }
            else {
                this.uri = uri == null ? "" : uri.toString();
            }
            if( this.uri.equals("") ) {
                if( prefix == null || prefix.equals("") ) {
                    this.prefix = "";
                }
                else {
                    return;
                }
            }
            else if( prefix == null || !E4X.isXMLName( prefix ) ) {
                this.prefix = null;
            }
            else {
                this.prefix = prefix;
            }
        }
    }

    public String prefix;
    public String uri;

    private static String prefixStr = "prefix";
    private static String uriStr = "uri";

    public Namespace() {
        this(null, null);
    }

    public Namespace( Object uri) {
        this(null, uri);
    }

    public Namespace( String prefix, Object uri) {
        super( _getCons() );
        init( prefix, uri );
    }

    public static JSFunction _getCons() {
        return Scope.getThreadLocalFunction( "Namespace" , _cons );
    }

    public boolean equals( Namespace ns ) {
        if( ( ns.prefix == null && this.prefix != null ) ||
            ( ns.prefix != null && this.prefix == null ) ||
            ( ns.uri == null && this.uri != null ) ||
            ( ns.uri != null && this.uri == null ) )
            return false;

        if( ( ns.prefix == null || ns.prefix.equals( this.prefix ) ) &&
            ( ns.uri == null || ns.uri.equals( this.uri ) ) )
            return true;
        return false;
    }

    public String toString() {
        return this.uri;
    }

    public boolean containedIn( ArrayList<Namespace> list ) {
        for( Namespace ns : list ) {
            if( ( ns.prefix == null && this.prefix != null ) ||
                ( ns.prefix != null && this.prefix == null ) ||
                ( ns.uri == null && this.uri != null ) ||
                ( ns.uri != null && this.uri == null ) )
                continue;

            if( ns.prefix == null && this.prefix == null ) {
                if( ns.uri == null && this.uri == null || ns.uri.equals( this.uri ) ) {
                    return true;
                }
                return false;
            }
            if( ns.prefix.equals( this.prefix ) && ns.uri.equals( this.uri ) )
                return true;
        }
        return false;
    }

    public boolean containsPrefix( ArrayList<Namespace> list ) {
        for( Namespace ns : list ) {
            if( ( ns.prefix == null && this.prefix != null ) ||
                ( ns.prefix != null && this.prefix == null ) )
                continue;
            if( ( ns.prefix == null && this.prefix == null ) ||
                ns.prefix.equals( this.prefix ) )
                return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return this.uri == null || this.uri.equals( "" );
    }

    public String getPrefix() {
        String prefix = this.uri;
        while( prefix.endsWith("/") || prefix.endsWith(".xml") ) {
            if ( prefix.endsWith( "/" ) )
                prefix.substring( 0, prefix.length() - 1 );
            if ( prefix.endsWith( ".xml" ) )
                prefix.substring( 0, prefix.length() - 4 );
        }
        prefix = prefix.substring( prefix.lastIndexOf("/") + 1 );
        prefix = prefix.substring( prefix.lastIndexOf(".") + 1 );
        return prefix;
    }

    public Object get( Object n ) {
        if( n == null ) 
            return null;

        String str = n.toString();
        Object objFromProto = Namespace._cons.getPrototype().get( str );
        if( objFromProto != null && objFromProto instanceof JSFunction ) {
            return objFromProto;
        }
        else if( str.equals( uriStr ) ) {
            return new JSString(this.uri);
        }
        else if ( str.equals( prefixStr ) ) {
            return new JSString(this.prefix);
        }
        else {
            return null;
        }
    }
}
