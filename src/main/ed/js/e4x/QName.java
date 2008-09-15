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
            if ( blah instanceof QName) {
                e = (QName)blah;
            }
            else {
                if( args.length == 0 ) {
                    e = new QName();
                }
                else if( args.length == 1 ) {
                    e = new QName( args[0] );
                }
                else {
                    e = new QName( new Namespace( args[0] ), args[1] );
                }
            }

            return e;
        }
    }


    void init( String s ) {
        this.uri = s;
    }

    void init( String p, String s ) {
        this.localName = p;
        this.uri = s;
    }

    public String localName;
    public String uri;
    public String prefix;

    public QName() {
        this( null, null );
    }

    public QName( Object name )  {
        this( null, name );
    }

    public QName( Namespace namespace, Object name )  {
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
    }

    public String toString() {
        String s = this.uri == null ? "*::" : ( this.uri.equals("") ? "" : this.uri + "::" );
        return s + this.localName;
    }

    public boolean equals( QName o ) {
        if( ( ( this.localName == null && o.localName == null) || (this.localName != null && this.localName.equals( o.localName ) ) ) &&
            ( ( this.uri == null && o.uri == null ) || ( this.uri != null && this.uri.equals( o.uri ) ) ) &&
            ( ( this.prefix == null && o.prefix == null ) || ( this.prefix != null && this.prefix.equals( o.prefix ) ) ) )
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

    public String get( Object n ) {
        if( n.toString().equals( "uri" ) ) {
            return this.uri;
        }
        else if ( n.toString().equals( "prefix" ) ) {
            return this.prefix;
        }
        else 
            return null;
    }
}
