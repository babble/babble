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

        private ENode( Node n ){
            _node = n;
        }

        void init( String s ){
            _raw = s;
            try {
                _document = XMLUtil.parse( s );
                _node = _document.getDocumentElement();
            }
            catch ( Exception e ){
                throw new RuntimeException( "can't parse : " + e );
            }
        }

        public Object get( Object n ){
            if ( n == null )
                return null;

            if ( n instanceof String || n instanceof JSString ){
		return _nodeGet( _node , n.toString() );
            }

            throw new RuntimeException( "can't handle : " + n.getClass() );
        }



        public String toString(){
            if ( _raw != null )
                return _raw;

            if ( _node == null )
                return null;

            if ( _node.getChildNodes() != null &&
                 _node.getChildNodes().getLength() == 1 &&
                 _node.getChildNodes().item(0) instanceof CharacterData )
                return XMLUtil.toString( _node.getChildNodes().item(0) );

            return XMLUtil.toString( _node );
        }

        private String _raw;
        private Document _document;

        private Node _node;
    }

    static class EList extends JSObjectBase {
        private EList( List<Node> lst ){
            _lst = lst;
        }

        public Object get( Object n ){
            if ( n == null )
                return null;

            if ( n instanceof Number )
                return child( ((Number)n).intValue() );

            if ( n instanceof JSString || n instanceof String ){
                String s = n.toString();

                if ( s.equals( "length" ) ||
                     s.equals( "toString" ) ||
                     s.equals( "child" ) )
                    return null;
            }

	    if ( n instanceof Query ){
		Query q = (Query)n;
		List<Node> matching = new ArrayList<Node>();
		for ( Node theNode : _lst ){
		    if ( q.match( theNode ) )
			matching.add( theNode );
		}
		return _handleListReturn( matching );
	    }


            throw new RuntimeException( "can't handle [" + n + "] from a list" );
        }

        public ENode child( int num ){
            return new ENode( _lst.get( num ) );
        }

        public int length(){
            if ( _lst == null )
                return -1;
            return _lst.size();
        }

        public String toString(){
            StringBuilder buf = new StringBuilder();
            for ( Node n : _lst )
                XMLUtil.append( n , buf , 0 );
            return buf.toString();
        }

        final private List<Node> _lst;
    }

    static Object _nodeGet( Node start , String s ){

        final boolean search = s.startsWith( ".." );
        if ( search )
            s = s.substring(2);

        final boolean attr = s.startsWith( "@" );
        if ( attr )
            s = s.substring(1);

        List<Node> traverse = new LinkedList<Node>();
        traverse.add( start );

	List<Node> res = new ArrayList<Node>();

        while ( ! traverse.isEmpty() ){
            Node n = traverse.remove(0);

            if ( attr ){
                NamedNodeMap nnm = n.getAttributes();
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

                if ( ! attr && c.getNodeName().equals( s ) )
                    res.add( c );

                if ( search )
                    traverse.add( c );
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
	    return new ENode( n );
        }

	return new EList( lst );
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
