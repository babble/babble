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
import java.util.regex.*;

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

            _prototype.set( "copy" , new JSFunctionCalls0() {
                    public Object call( Scope s, Object foo[] ) {
                        ENode n = (ENode)s.getThis();
                        boolean deep_copy = true;
                        return new ENode(n.node.cloneNode(deep_copy), n.parent);
                    }
                });

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
            children = new LinkedList<ENode>();
            node = n;
        }

        private ENode( Node n, ENode parent ) {
            this.children = new LinkedList<ENode>();
            this.node = n;
            this.parent = parent;
        }
        private ENode( ENode parent, Object o ) {
            if(parent.node != null)
                node = parent.node.getOwnerDocument().createElement(o.toString());
            this.children = new LinkedList<ENode>();
            this.parent = parent;
            this._dummy = true;
        }

        private ENode( List<ENode> n ) {
            children = n;
        }

        void buildENodeDom(ENode parent) {
            NodeList kids = parent.node.getChildNodes();
            for( int i=0; i<kids.getLength(); i++) {
                ENode n = new ENode(kids.item(i), parent);
                buildENodeDom(n);
                parent.children.add(n);
            }
        }

        void init( String s ){
            try {
                _document = XMLUtil.parse( s );
                children = new LinkedList<ENode>();
                node = _document.getDocumentElement();
                buildENodeDom(this);
            }
            catch ( Exception e ){
                throw new RuntimeException( "can't parse : " + e );
            }
        }

        public Object get( Object n ){
            //            System.out.println("in get: "+n+" node: "+node+" kids: "+children);
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
		Object o = _nodeGet( this, s );
                if(o == null) return new ENode( this, n );
                return o;
            }

            if ( n instanceof Query ) {
		Query q = (Query)n;
		List<ENode> matching = new ArrayList<ENode>();
		for ( ENode theNode : children ){
		    if ( q.match( theNode.node ) )
			matching.add( theNode );
		}
		return _handleListReturn( matching );
            }

            throw new RuntimeException( "can't handle : " + n.getClass() );
        }

        public Object set( Object k, Object v ) {
            if( v == null ) return "null";
            if(this.children == null ) this.children = new LinkedList<ENode>();

            // if v is already XML, we ignore k and just add v to this enode's children
            if( v instanceof ENode ) {
                this.children.add((ENode)v);
                return v;
            }

            // find out if this k/v pair exists
            ENode n = (ENode)get(k);

            Pattern num = Pattern.compile("-?\\d+");
            Matcher m = num.matcher(k.toString());

            // k is a number
            if( m.matches() ) {
                int index;
                // the index must be greater than 0
                if( ( index = Integer.parseInt(k.toString()) ) < 0)
                    return v;

                // this index is greater than the number of elements existing
                if( n == null ) {
                    Node content;
                    if(this.node != null)
                        content = this.node.getOwnerDocument().createTextNode(v.toString());
                    else if(this.children != null && this.children.size() > 0)
                        content = this.children.get(0).node.getOwnerDocument().createTextNode(v.toString());
                    // if there is no node or children, we're trying to create an element too deep
                    else
                        return v;

                    if( this._dummy ) {
                        this.node.appendChild(content);
                        this.parent.node.appendChild(this.node);
                        appendChild(this.node, this.parent);
                    }
                    else {
                        Node newNode = this.children.get(0).node.getOwnerDocument().createElement(this.children.get(0).node.getNodeName());
                        newNode.appendChild(content);

                        ENode newEn = new ENode(newNode, this);
                        buildENodeDom(newEn);
                        // add a sibling
                        children.get(0).parent.children.add( newEn );
                    }
                }
                // replace an existing element
                else {
                    // reset the child list
                    n.children = new LinkedList<ENode>();
                    NodeList kids = n.node.getChildNodes();
                    for( int i=0; kids != null && i<kids.getLength(); i++) {
                        n.node.removeChild(kids.item(i));
                    }
                    Node content = n.node.getOwnerDocument().createTextNode(v.toString());
                    appendChild(content, n);
                }
            }
            // k must be a string
            else {
                // if there is more than one matching child, delete them all and replace with the new k/v
                for( int i = 0; n != null && n.children != null && i < n.children.size(); i++ ) {
                    children.remove( n.children.get(i) );
                }

                Node newNode = node.getOwnerDocument().createElement(k.toString());
                Node content = node.getOwnerDocument().createTextNode(v.toString());
                newNode.appendChild(content);

                ENode newEn = new ENode(newNode, this);
                buildENodeDom(newEn);
                children.add( newEn );
            }
            return v;
        }

        public boolean appendChild(Node child, ENode parent) {
            if(parent.children == null) parent.children = new LinkedList<ENode>();
            ENode echild = new ENode(child, parent);
            buildENodeDom(echild);
            return parent.children.add(echild);
        }

        public Object removeField(Object o) {
            return o;
        }

        public ENode child( int idx ) {
            if( idx >= 0 && idx < children.size() )
                return children.get( idx );
            return null;
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
            Node parent = node.getParentNode();
            if( parent == null || parent.getNodeType() == Node.ATTRIBUTE_NODE ) return -1;

            NodeList sibs = parent.getChildNodes();
            for( int i=0; i<sibs.getLength(); i++ ) {
                if(sibs.item(i).isEqualNode(node)) return i;
            }

            return -1;
        }

        public ENode children() {
            return (ENode)get( "*" );
        }

        public ENode comments() {
            NodeList kids = node.getChildNodes();
            List<ENode> comments = new LinkedList<ENode>();

            for( int i=0; i<kids.getLength(); i++ ) {
                if(kids.item(i).getNodeType() == Node.COMMENT_NODE)
                    comments.add(new ENode(kids.item(i)));
            }

            return new ENode(comments);
        }

        // this is to spec, but doesn't seem right... it's "equals", not "contains"
        public boolean contains( ENode o ) {
            if( !(o instanceof ENode) )
                return false;
            return node.isEqualNode(o.node);
        }

        public ENode descendants() {
            return descendants( null );
        }

        public ENode descendants( String name ) {
            if(name == null) name = "*";

            List kids = new LinkedList<ENode>();

            ENode childs = (ENode)this.get(name);
            for( int i=0; i<childs.children.size(); i++) {
                kids.add(childs.children.get(i));
                ENode el = ((ENode)childs.children.get(i)).descendants(name);
                for( int j=0; j<el.children.size(); j++) {
                    kids.add(el.children.get(j));
                }
            }
            return new ENode(kids);
        }

        public String toString(){
            if ( node == null && children == null )
                return null;

            StringBuilder xml = new StringBuilder();
            // XML
            if( node != null ) {
                if( node.getNodeType() == Node.ATTRIBUTE_NODE || node.getNodeType() == Node.TEXT_NODE )
                    return node.getNodeValue();

                if ( node.getNodeType() == Node.ELEMENT_NODE &&
                     children != null &&
                     children.size() == 1 &&
                     children.get(0).node.getNodeType() == Node.TEXT_NODE ){
                    return children.get(0).node.getNodeValue();
                }

                append( this, xml, 0);
            }
            // XMLList
            else {
                for( int i=0; i<children.size(); i++ ) {
                    append( children.get( i ), xml, 0);
                }
            }

            // XMLUtil's toString always appends a "\n" to the end
            if( xml.length() > 0 && xml.charAt(xml.length() - 1) == '\n' ) {
                xml.deleteCharAt(xml.length()-1);
            }
            return xml.toString();
        }

        public static StringBuilder append( ENode n , StringBuilder buf , int level ){
            if ( n.node.getNodeType() == Node.ATTRIBUTE_NODE ||
                 n.node.getNodeType() == Node.TEXT_NODE )
                return _level( buf , level ).append( n.node.getNodeValue() ).append( "\n" );


            _level( buf , level ).append( "<" ).append( n.node.getNodeName() );
            NamedNodeMap attr = n.node.getAttributes();
            if ( attr != null ){
                String[] attrArr = new String[attr.getLength()];
                for ( int i=0; i<attr.getLength(); i++ ){
                    Node a = attr.item(i);
                    attrArr[i] = " " + a.getNodeName() + "=\"" + a.getNodeValue() + "\" ";
                }
                Arrays.sort(attrArr);
                for( String a : attrArr ) {
                    buf.append( a );
                }
            }

            List<ENode> children = n.children;
            if ( children == null || children.size() == 0 ) {
                return buf.append( "/>\n" );
            }
            buf.append( ">\n" );

            for ( int i=0; children != null && i<children.size(); i++ ){
                ENode c = children.get(i);
                if( c.node.getNodeType() == Node.COMMENT_NODE ||
                    c.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE )
                    continue;
                append( c , buf , level + 1 );
            }

            return _level( buf , level ).append( "</" ).append( n.node.getNodeName() ).append( ">\n" );
        }

        private static StringBuilder _level( StringBuilder buf , int level ){
            for ( int i=0; i<level; i++ )
                buf.append( "  " );
            return buf;
        }


        public NodeList getChildNodes() {
            return node.getChildNodes();
        }

        public int length(){
            if ( children == null )
                return 0;
            return children.size();
        }

        public NamedNodeMap getAttributes() {
            if(node == null && ( children == null || children.size() == 0)) return null;
            if(node != null) return node.getAttributes();

            NamedNodeMap map = children.get(0).getAttributes();
            for(int i=1; i<children.size(); i++) {
                NamedNodeMap temp = children.get(i).getAttributes();
                for(int j=0; j<temp.getLength(); j++) {
                    map.setNamedItem( temp.item(j) );
                }
            }
            return map;
        }

        private Document _document;

        private List<ENode> children;
        private ENode parent;
        private Node node;

        private boolean _dummy;
    }

    static Object _nodeGet( ENode start , String s ){
        List<ENode> ln = new LinkedList<ENode>();
        ln.add(start);
        return _nodeGet( ln, s );
    }

    static Object _nodeGet( List<ENode> start , String s ){
        final boolean search = s.startsWith( ".." );
        if ( search )
            s = s.substring(2);

        final boolean attr = s.startsWith( "@" );
        if ( attr )
            s = s.substring(1);

        final boolean all = s.endsWith("*");
        if( all )
            s = s.substring(0, s.length()-1);

        List<ENode> traverse = new LinkedList<ENode>();
	List<ENode> res = new ArrayList<ENode>();

        for(int k=0; k< start.size(); k++) {
            traverse.add( start.get(k) );

            while ( ! traverse.isEmpty() ){
                ENode n = traverse.remove(0);

                if ( attr ){
                    NamedNodeMap nnm = n.getAttributes();
                    if ( all ) {
                        for(int i=0; nnm != null && i<nnm.getLength(); i++) {
                            res.add(new ENode(nnm.item(i)));
                        }
                    }
                    if ( nnm != null ){
                        Node a = nnm.getNamedItem( s );
                        if ( a != null )
                            res.add( new ENode( a, n.parent ) );
                    }
                }

                List<ENode> kids = n.children;
                if ( kids.size() == 0 )
                    continue;

                for ( int i=0; i<kids.size(); i++ ){
                    ENode c = kids.get(i);

                    if ( ! attr && ( all || c.node.getNodeName().equals( s ) ) )
                        res.add( c );

                    if ( search )
                        traverse.add( c );
                }
            }
        }

	return _handleListReturn( res );
    }

    static Object _handleListReturn( List<ENode> lst ){
	if ( lst.size() == 0 )
	    return null;

	if ( lst.size() == 1 ){
            ENode n = lst.get(0);
            if ( n.node instanceof Attr )
                return n.node.getNodeValue();
	    return n;
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
	    return JSInternalFunctions.JS_eq( _nodeGet( new ENode(n) , _what ) , _match );
	}

	public String toString(){
	    return " [[ " + _what + " == " + _match + " ]] ";
	}

    }
}
