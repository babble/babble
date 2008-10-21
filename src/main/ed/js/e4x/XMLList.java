// XMLList.java

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

public class XMLList extends ENode implements List<ENode>, Iterable<ENode> {

    public static JSFunction c = new ListCons();

    public static class ListCons extends JSFunctionCalls0 {
        public JSObject newOne(){
            _new = true;
            return new XMLList();
        }

        public Object call( Scope scope , Object [] args){
            Object o = scope.getThis();

            if ( o != null && o instanceof XMLList ) {
                XMLList list = (XMLList)o;
                if( args.length > 0 && args[0] != null && args[0] instanceof XMLList ) {
                    if( _new ) {
                        _new = false;
                        list.init( args[0] );
                        return list;
                    }
                    else {
                        return (XMLList)args[0];
                    }
                }
                else if( args.length == 0 ) {
                    return new XMLList();
                }
            }
            ENode n = (ENode)(new ENode.Cons()).call( scope, args );
            XMLList x = new XMLList( n );
            
            if( o instanceof XMLList ) {
                ((XMLList)o).addAll( x );
                return o;
            }
            
            return x;
        }

        private boolean _new = false;

        protected void init() {
            final JSObjectBase thisPrototype = _prototype;
            _prototype.set( "child", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).child( ENode.getOneArg( foo ).toString() );
                    }
                });
            _prototype.set( "hasSimpleContent", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).hasSimpleContent();
                    }
                });
            _prototype.set( "hasComplexContent", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).hasComplexContent();
                    }
                });
            _prototype.set( "hasOwnProperty", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        Object o = s.getThis();
                        if( o instanceof XMLList ) 
                            return ((XMLList)s.getThis()).hasOwnProperty( ENode.getOneArg( foo ).toString() );
                        else if( o == thisPrototype )
                            return thisPrototype.get( ENode.getOneArg( foo ).toString() ) != null;
                        else
                            return false;
                    }
                });
            _prototype.set( "length", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).length();
                    }
                });
            _prototype.set( "toString", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return new JSString(((XMLList)s.getThis()).toString());
                    }
                });
            _prototype.set( "toXMLString", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).toXMLString();
                    }
                });

            }
        };

    public static JSFunction _getListCons() {
        return Scope.getThreadLocalFunction( "XMLList" , c );
    }

    public List<ENode> children;

    public XMLList() {
        super( _getListCons() );
        children = new LinkedList<ENode>();
    }

    public XMLList( ENode node ) {
        super( _getListCons() );
        this.children = new LinkedList<ENode>();
        init( node );
    }

    public XMLList( List<ENode> list ) {
        super( _getListCons() );
        children = list;
    }

    public void init( Object nodes ) {
        if( nodes == null ) {
            return;
        }
        // make a copy of an existing xmllist
        else if( nodes instanceof XMLList ) {
            for( ENode child : (XMLList)nodes ) {
                ENode temp = child.copy();
                this.add( temp );
            }
        }
        else if( nodes instanceof ENode ) {
            if( ((ENode)nodes).node == null && 
                ((ENode)nodes).children().size() > 0 ) {
                this.addAll( ((ENode)nodes).children() );
            }
            else if( ((ENode)nodes).node != null ) {
                this.add( (ENode)nodes );
            }
        }
    }

    public Iterator<ENode> iterator() {
        return children.iterator();
    }

    public int size() {
        return children.size();
    }

    public ENode get( int index ) {
        return children.get(index);
    }

    public XMLList comments() {
        XMLList comments = new XMLList();

        for( ENode child : children ) {
            if( child.node.getNodeType() == Node.COMMENT_NODE )
                comments.add( child );
        }
        return comments;
    }

    public boolean contains( Object o ) { 
        if( this.equals( o ) ) 
            return true;
        return children.contains( o ); 
    }

    public int length() {
        return this.size();
    }

    public XMLList text() {
        XMLList list = new XMLList();
        for ( ENode n : this ) {
            list.addAll( n.text() );
        }
        return list;
    }

    public String toString() {
        if( children.size() == 1 ) {
            return children.get(0).toString();
        }
        return this.toXMLString();
    }

    public String toXMLString() {
        StringBuilder xml = new StringBuilder();
        for( ENode n : this ) {
            xml.append( n.append( new StringBuilder(), 0, new ArrayList<Namespace>() ).toString() );
            if( this.hasComplexContent() && ((Cons)this._getCons()).prettyPrinting )
                xml.append( "\n" );
        }
        if( xml.length() > 0 && xml.charAt(xml.length() - 1) == '\n' ) {
            xml.deleteCharAt(xml.length()-1);
        }
        return xml.toString();
    }

    public boolean addAll(XMLList list) { 
        for( ENode n : list ) 
            children.add( n ); 
        return true;
    }

    public boolean add( ENode n ) { 
        return children.add(n); 
    }

    public void add( int index, ENode n) { children.add( index, n); }
    public boolean addAll( Collection<? extends ENode> list ) { return children.addAll( list ); }
    public boolean addAll( int index, Collection<? extends ENode> list ) { return children.addAll( index, list ); }
    public void clear() {  children.clear(); }
    public boolean containsAll( Collection o ) { return  children.containsAll( o ); }
    public boolean equals( Object o ) { return children.equals(o); }
    public int hashCode( IdentitySet seen ) { return children.hashCode(); }
    public int indexOf( Object o ) { return children.indexOf(o); }
    public boolean isEmpty() { return children.isEmpty(); }
    public int lastIndexOf( Object o) { return children.lastIndexOf( o ); }
    public ListIterator<ENode> listIterator() { return children.listIterator(); }
    public ListIterator<ENode> listIterator( int index) { return children.listIterator(index); }
    public ENode remove(int index) { return children.remove( index); }
    public boolean remove(Object o) { return children.remove( o ); }
    public boolean removeAll(Collection c) { return children.removeAll(c); }
    public boolean retainAll(Collection c) { return children.retainAll(c); }
    public ENode set(int index, ENode o) { return children.set(index, o); }
    public List<ENode> subList(int from, int to) { return children.subList(from, to); }
    public Object[] toArray() { return children.toArray(); }
    public <T> T[] toArray(T[] a) { return children.toArray(a); }
}
