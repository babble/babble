// QName.java

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

public class QName extends JSObjectBase {

    public static JSFunction _cons = new Cons();

    public static class Cons extends JSFunctionCalls0 {

        public JSObject newOne(){
            return new QName();
        }

        public Object call( Scope scope , Object [] args){
            Object blah = scope.getThis();

            QName e;
            if ( blah instanceof QName ) {
                e = (QName)blah;
            }
            else {
                e = new QName();
            }

            if( args.length == 1 ) {
                e.init( args[0] );
            }
            else if( args.length > 1 ) {
                e.init( new Namespace( args[0] ), args[1] );
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
            if( foo.equals( localNameStr ) ||
                foo.equals( uriStr ) ||
                foo.equals( prefixStr ) )
                return true;
            return false;
        }
    }


    void init( Object s ) {
        init( null, s );
    }

    void init( Namespace namespace, Object name ) {
        if( name instanceof QName ) {
            if ( namespace == null ) {
                this.localName = ((QName)name).localName;
                this.uri = ((QName)name).uri;
                return;
            }
            else {
                name = ((QName)name).localName;
            }
        }
        this.localName = name == null ? "" : name.toString();
        if( namespace != null ) {
            this.uri = namespace.uri;
            this.prefix = namespace.prefix;
        }
        else {
            this.uri = "";
            this.prefix = "";
        }
    }

    public String localName;
    public String uri;
    public String prefix;

    private static String localNameStr = "localName";
    private static String uriStr = "uri";
    private static String prefixStr = "prefix";

    public QName() {
        this( null, null );
    }

    public QName( Object name )  {
        this( null, name );
    }

    public QName( Namespace namespace, Object name )  {
        super( _getCons() );
        init( namespace , name );
    }

    public static JSFunction _getCons() {
        return Scope.getThreadLocalFunction( "QName" , _cons );
    }

    public String toString() {
        String s = this.uri == null ? "*::" : ( this.uri.equals("") ? "" : this.uri + "::" );
        return s + this.localName;
    }

    public boolean equals( Object o ) {
        if( !( o instanceof QName ) )
            return false;

        QName q = (QName)o;
        if( ( ( this.localName == null && q.localName == null) || 
              ( this.localName != null && this.localName.equals( q.localName ) ) ) &&
            ( ( this.uri == null && q.uri == null ) || 
              ( this.uri != null && this.uri.equals( q.uri ) ) ) )
            return true;
        return false;
    }

    public Namespace getNamespace() {
        return getNamespace( null );
    }

    public Namespace getNamespace( ArrayList<Namespace> isn ) {
        if( this.uri == null )
            return null;

        for( Namespace ns : isn ) {
            if( ns.uri.equals( this.uri ) ) {
                return ns;
            }
        }
        return new Namespace( this.uri );
    }

    public Object get( Object n ) {
        if( n == null )
            return null;

        String str = n.toString();
        Object objFromProto = QName._cons.getPrototype().get( str );
        if( objFromProto != null && objFromProto instanceof JSFunction ) {
            return objFromProto;
        }
        else if( str.equals( uriStr ) ) {
            return new JSString(this.uri);
        }
        else if ( str.equals( prefixStr ) ) {
            return new JSString(this.prefix);
        }
        else if ( str.equals( localNameStr ) ) {
            return new JSString(this.localName);
        }
        else 
            return null;
    }

}
