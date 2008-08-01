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

package ed.js;

import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import ed.js.func.*;
import ed.js.engine.*;
import ed.util.*;

public class E4X {

    public static JSFunction _cons = new Cons();

    public static class Cons extends JSFunctionCalls1 {

        public JSObject newOne(){
            return new ENode();
        }

        public Object call( Scope scope , Object str , Object [] args){
            Object blah = scope.getThis();

            ENode e;
            if ( blah instanceof ENode)
                e = (ENode)blah;
            else
                e = new ENode();
            e.init( str.toString() );
            return e;
        }

        protected void init() {

            final JSFunctionCalls1 bar = this;

            set( "settings" , new JSFunctionCalls0(){
                    public Object call( Scope s, Object foo[] ){
                        JSObjectBase sets = new JSObjectBase();
                        sets.set("ignoreComments", bar.get("ignoreComments"));
                        sets.set("ignoreProcessingInstructions", bar.get("ignoreProcessingInstructions"));
                        sets.set("ignoreWhitespace", bar.get("ignoreWhitespace"));
                        sets.set("prettyPrinting", bar.get("prettyPrinting"));
                        sets.set("prettyIndent", bar.get("prettyIndent"));
                        return sets;
                    }
                } );

            set( "setSettings", new JSFunctionCalls0() {
                    public Object call( Scope s, Object foo[] ){
                        JSObject settings = new JSObjectBase();
                        if(foo.length > 0 && foo[0] instanceof JSObjectBase) {
                            settings = (JSObject)foo[0];
                            Object setting = settings.get("ignoreComments");
                            if(setting != null && setting instanceof Boolean) {
                                bar.set( "ignoreComments" , ((Boolean)setting).booleanValue());
                            }
                            setting = settings.get("ignoreProcessingInstructions");
                            if(setting != null && setting instanceof Boolean)
                                bar.set( "ignoreProcessingInstructions" , ((Boolean)setting).booleanValue());
                            setting = settings.get("ignoreWhitespace");
                            if(setting != null && setting instanceof Boolean)
                                bar.set( "ignoreWhitespace" , ((Boolean)setting).booleanValue());
                            setting = settings.get("prettyPrinting");
                            if(setting != null && setting instanceof Boolean)
                                bar.set( "prettyPrinting" , ((Boolean)setting).booleanValue());
                            setting = settings.get("prettyIndent");
                            if(setting != null && setting instanceof Integer)
                                bar.set( "prettyIndent" , ((Integer)setting).intValue());
                        }
                        else {
                            bar.set( "ignoreComments" , true);
                            bar.set( "ignoreProcessingInstructions" , true);
                            bar.set( "ignoreWhitespace" , true);
                            bar.set( "prettyPrinting" , true);
                            bar.set( "prettyIndent" , 2);
                        }
                        return null;
                    }
                } );

            set( "defaultSettings" , new JSFunctionCalls0(){
                    public Object call( Scope s, Object foo[] ){
                        JSObjectBase sets = new JSObjectBase();
                        sets.set("ignoreComments", true);
                        sets.set("ignoreProcessingInstructions", true);
                        sets.set("ignoreWhitespace", true);
                        sets.set("prettyPrinting", true);
                        sets.set("prettyIndent", 2);
                        return sets;
                    }
                } );

            set( "ignoreComments" , true);
            set( "ignoreProcessingInstructions" , true);
            set( "ignoreWhitespace" , true);
            set( "prettyPrinting" , true);
            set( "prettyIndent" , 2);

            _prototype.dontEnumExisting();
        }
    };

    static class ENode extends JSObjectBase {
        private ENode(){}

        private ENode( Node n ) {
            this( n, true );
        }

        private ENode( Node n, boolean lock ) {
            _lst = new LinkedList<Node>();
            _lst.add(n);
            if( lock ) oneElement();
        }

        private ENode( List<Node> n ) {
            _lst = n;
        }

        void init( String s ){
            //            _raw = s;
            try {
                _document = XMLUtil.parse( s );
                _lst = new LinkedList<Node>();
                _lst.add(_document.getDocumentElement());
                oneElement();
            }
            catch ( Exception e ){
                throw new RuntimeException( "can't parse : " + e );
            }
        }

        public Object get( Object n ){
            if ( n == null )
                return null;

            if ( n instanceof Number )
                return child( ((Number)n).intValue() );

            if ( n instanceof String || n instanceof JSString ){
                String s = n.toString();

                if ( s.equals( "length" ) ||
                     s.equals( "toString" ) ||
                     s.equals( "tojson" ) ||
                     s.equals( "child" ) )
                    return null;

		return _nodeGet( _lst, s );
            }

            if ( n instanceof Query ) {
		Query q = (Query)n;
		List<Node> matching = new ArrayList<Node>();
		for ( Node theNode : _lst ){
		    if ( q.match( theNode ) )
			matching.add( theNode );
		}
		return _handleListReturn( matching );
            }

            throw new RuntimeException( "can't handle : " + n.getClass() );
        }

        public ENode child( int idx ) {
            return new ENode( _lst.get( idx ), false );
        }

        public ENode child( Object propertyName ) {
            try {
                int x = Integer.parseInt(propertyName.toString());
                return (ENode)((ENode)this.get( "*" )).get(Integer.parseInt(propertyName.toString()));
            }
            catch( NumberFormatException nfe ) {
                return (ENode)get(propertyName.toString());
            }
        }

        public int childIndex() {
            Node parent = _lst.get(0).getParentNode();
            if( parent == null || parent.getNodeType() == Node.ATTRIBUTE_NODE ) return -1;

            NodeList children = parent.getChildNodes();
            for( int i=0; i<children.getLength(); i++ ) {
                if(children.item(i).isEqualNode(_lst.get(0))) return i;
            }

            return -1;
        }

        public ENode children() {
            return (ENode)get( "*" );
        }

        public ENode comments() {
            NodeList children = _lst.get(0).getChildNodes();
            List<Node> comments = new LinkedList<Node>();

            for( int i=0; i<children.getLength(); i++ ) {
                if(children.item(i).getNodeType() == Node.COMMENT_NODE)
                    comments.add(children.item(i));
            }

            return new ENode(comments);
        }

        // this is to spec, but doesn't seem right... it's "equals", not "contains"
        public boolean contains( ENode o ) {
            if( !(o instanceof ENode) )
                return false;
            return this._lst.get(0).isEqualNode(o._lst.get(0));
        }

        public ENode copy() {
            boolean deep_copy = true;
            return new ENode(this._lst.get(0).cloneNode(deep_copy));
        }

        public ENode descendants() {
            return descendants( null );
        }

        public ENode descendants( String name ) {
            if(name == null) name = "*";

            List kids = new LinkedList<Node>();

            ENode children = (ENode)this.get(name);
            for( int i=0; i<children.length(); i++) {
                kids.add(children.get(i));
                ENode el = ((ENode)children.get(i)).descendants(name);
                for( int j=0; j<el.length(); j++) {
                    kids.add(el.get(j));
                }
            }
            return new ENode(kids);
        }

        public String toString(){
            if ( _lst == null )
                return null;

            StringBuffer xml = new StringBuffer();
            /*            if( this.length() == 1 && _document == null ) {
                NodeList children = _lst.get(0).getChildNodes();
                for( int j = 0; children != null && j < children.getLength(); j++) {
                    xml.append(XMLUtil.toString( children.item(j) ));
                }
            }
            else {*/
                for( int i=0; i<_lst.size(); i++ ) {

                    ///   System.out.println(_lst.get(i).hasChildNodes()+": "+_lst.get(i).getNodeValue());

                    if ( _lst.get(i).getNodeType() == Node.COMMENT_NODE ) {
                        continue;
                    }
                    else if ( _lst.get(i).getNodeType() == Node.ATTRIBUTE_NODE ) {
                        xml.append( _lst.get(i).getNodeValue() );
                    }
                    else if ( _lst.get(i).getChildNodes() != null &&
                              _lst.get(i).getChildNodes().getLength() == 1 &&
                              _lst.get(i).getChildNodes().item(0) instanceof CharacterData ) {
                        //   System.out.println("and here.");
                        xml.append(XMLUtil.toString( _lst.get(i).getChildNodes().item(0) ));
                    }
                    else {
                        xml.append(XMLUtil.toString( _lst.get( i ) ));
                    }
                }
                //            }

            // XMLUtil's toString always appends a "\n" to the end
            if( xml.length() > 0 && xml.charAt(xml.length() - 1) == '\n' )
                xml.deleteCharAt(xml.length()-1);
            return xml.toString();
        }

        public int length(){
            if ( _lst == null )
                return 0;
            else if ( this.isSingleNode() )
                return 1;
            return _lst.size();
        }

        private void oneElement() {
            this._lock = true;
        }

        private boolean isSingleNode() {
            return this._lock;
        }

        // if this list cannot have multiple nodes
        // so, false : multi-node list
        // true : single node
        private boolean _lock = false;

        private String _raw;
        private Document _document;

        private List<Node> _lst;
    }

    static Object _nodeGet( Node start , String s ){
        List<Node> ln = new LinkedList<Node>();
        ln.add(start);
        return _nodeGet( ln, s );
    }

    static Object _nodeGet( List<Node> start , String s ){
        final boolean search = s.startsWith( ".." );
        if ( search )
            s = s.substring(2);

        final boolean attr = s.startsWith( "@" );
        if ( attr )
            s = s.substring(1);

        final boolean all = s.endsWith("*");
        if( all )
            s = s.substring(0, s.length()-1);

        List<Node> traverse = new LinkedList<Node>();
	List<Node> res = new ArrayList<Node>();

        for(int k=0; k< start.size(); k++) {
            traverse.add( start.get(k) );

            while ( ! traverse.isEmpty() ){
                Node n = traverse.remove(0);

                if ( attr ){
                    NamedNodeMap nnm = n.getAttributes();
                    if ( all ) {
                        for(int i=0; i<nnm.getLength(); i++) {
                            res.add(nnm.item(i));
                        }
                    }
                    if ( nnm != null ){
                        Node a = nnm.getNamedItem( s );
                        if ( a != null )
                            res.add( a );
                    }
                }

                NodeList children = n.getChildNodes();
                if ( children == null )
                    continue;

                for ( int i=0; i<children.getLength(); i++ ){
                    Node c = children.item(i);

                    if ( ! attr && ( all || c.getNodeName().equals( s ) ) )
                        res.add( c );

                    if ( search )
                        traverse.add( c );
                }
            }
        }

	return _handleListReturn( res );
    }

    static Object _handleListReturn( List<Node> lst ){
	if ( lst.size() == 0 )
	    return null;

	if ( lst.size() == 1 ){
            Node n = lst.get(0);
            if ( n instanceof Attr )
                return n.getNodeValue();
	    return new ENode(n, false);
        }

	return new ENode( lst );
    }

    public static abstract class Query {
	public Query( String what , JSString match ){
	    _what = what;
	    _match = match;
	}

	abstract boolean match( Node n );

	final String _what;
	final JSString _match;
    }

    public static class Query_EQ extends Query {

	public Query_EQ( String what , JSString match ){
	    super( what , match );
	}

	boolean match( Node n ){
	    return JSInternalFunctions.JS_eq( _nodeGet( n , _what ) , _match );
	}

	public String toString(){
	    return " [[ " + _what + " == " + _match + " ]] ";
	}

    }
}
