// XMLList.java

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
            _prototype.set( "attribute", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).attribute( ENode.getOneArg( foo ).toString() );
                    }
                });
            _prototype.set( "attributes", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).attributes();
                    }
                });
            _prototype.set( "child", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).child( ENode.getOneArg( foo ).toString() );
                    }
                });
            _prototype.set( "children", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).children();
                    }
                });
            _prototype.set( "comments", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).comments();
                    }
                });
            _prototype.set( "contains", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).contains( ENode.getOneArg( foo ) );
                    }
                });
            _prototype.set( "copy", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).copy();
                    }
                });
            _prototype.set( "descendants", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).descendants();
                    }
                });
            _prototype.set( "elements", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).elements( ENode.getOneArg( foo ).toString() );
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
            _prototype.set( "normalize", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).normalize();
                    }
                });
            _prototype.set( "parent", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).parent();
                    }
                });
            _prototype.set( "processingInstructions", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).processingInstructions( ENode.getOneArg( foo ).toString() );
                    }
                });
            _prototype.set( "propertyIsEnumerable", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).propertyIsEnumerable( ENode.getOneArg( foo ).toString() );
                    }
                });
            _prototype.set( "text", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return ((XMLList)s.getThis()).text();
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

    public XMLList( ENodeFunction node ) {
        super( _getListCons() );
        this.children = new LinkedList<ENode>();
        init( node.cnode );
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
            this.addAll( (XMLList)nodes );
            /*for( ENode child : (XMLList)nodes ) {
                ENode temp = child.copy();
                this.add( temp );
                }*/
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

    public Object get( Object o ) {
        return super.get( o );
    }

    public ENode get( int index ) {
        if( index >= size() ) {
            if( index == 0 ) {
                ENode foo = new ENode();
                foo.setDummy( true );
                return foo;
            }
            return new ENode( this.parent(), get( 0 ).name() );
        }
        return children.get(index);
    }

    public XMLList children() {
        return children( true );
    }

    private XMLList children( boolean copy ) {
        if( copy ) {
            XMLList kids = new XMLList();
            for( ENode n : this ) {
                kids.addAll( n.children() );
            }
            return kids;
        }
        else {
            return this;
        }
    }

    public XMLList comments() {
        return this.getOne().comments();
    }

    public boolean contains( Object o ) { 
        if( this.equals( o ) ) 
            return true;
        for( ENode n : this ) {
            if( JSInternalFunctions.JS_eq( n, o ) )
                return true;
        }
        return false; 
    }

    public int length() {
        if( this.size() == 1 &&
            this.getOne().isDummy() )
            return 0;
        return this.size();
    }

    public ENode parent() {
        ENode parent = null;
        for( int i=0; i < this.size(); i++ ) {
            if( i == 0 ) {
                parent = this.get( i ).parent();
            }
            if( this.get( i ).parent() != parent )
                return null;
        }
        return parent;
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

    public ENode set(int index, ENode o) { 
        return children.set(index, o); 
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
    public List<ENode> subList(int from, int to) { return children.subList(from, to); }
    public Object[] toArray() { return children.toArray(); }
    public <T> T[] toArray(T[] a) { return children.toArray(a); }
}
