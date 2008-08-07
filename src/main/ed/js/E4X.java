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
        private ENode(){
            children = new LinkedList<ENode>();
        }

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
            if(parent != null && parent.node != null)
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
            nativeFuncs.put("copy", new copy());
        }

        Hashtable<String, ENodeFunction> nativeFuncs = new Hashtable<String, ENodeFunction>();

        public ENodeFunction getENodeFunction( String name ) {
            return nativeFuncs.get( name );
            /*            ENode cnode = nativeFuncs.get( name ).cnode;
            if(cnode == null)
                cnode = new ENode(this, "copy");
                return cnode;*/
        }

        public Object get( Object n ){
            if ( n == null )
                return null;

            if ( n instanceof Number )
                return child( ((Number)n).intValue() );

            if ( n instanceof String || n instanceof JSString ){
                String s = n.toString();

                if( s.equals( "copy" ) )
                    return getENodeFunction( "copy" );

                if ( s.equals( "length" ) ||
                     s.equals( "toString" ) ||
                     s.equals( "tojson" ) ||
                     s.equals( "child" ))
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
            //            ENode n = (ENode)get(k);
            ENode n;
            Object obj = get(k);
            if( obj == null || obj instanceof ENode )
                n = (ENode)obj;
            else
                n = (( ENodeFunction )obj).cnode;

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

                if(this.node == null ) {
                    System.out.println("debug: this: "+this+" k: "+k+" v: "+v);
                    return v;
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
            ENode n = (ENode)get(o);

            if(n.node != null)
                return n.parent.children.remove(n);

            for(ENode e : n.children)
                children.remove(e);

            return true;
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

        public class copy extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                ENode n = (ENode)s.getThis();
                boolean deep_copy = true;
                ENode newn = new ENode(n.node.cloneNode(deep_copy), n.parent);
                buildENodeDom(newn);
                return newn;
            }
        };

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



        public ENode elements( String name ) {
            if(this.children == null || this.children.size() == 0)
                return null;

            if(name == null || name == "") {
                name = "*";
            }

            ENode list = new ENode();
            for( ENode n : this.children ) {
                if( n.node != null && n.node.getNodeType() == Node.ELEMENT_NODE && (name.equals( "*" ) || n.node.getNodeName().equals(name)) )
                    list.children.add( n );
            }

            return list;
        }
        public ENode elements() {
            return elements(null);
        }

        public boolean hasOwnProperty( String prop ) {
            if(this.children == null || this.children.size() == 0)
                return false;

            for( ENode n : this.children ) {
                if( n.node != null && n.node.getNodeName().equals(prop) )
                    return true;
            }

            return false;
        }

        private boolean isSimpleTypeNode( short type ) {
            if( type == Node.ATTRIBUTE_NODE ||
                type == Node.PROCESSING_INSTRUCTION_NODE ||
                type == Node.COMMENT_NODE ||
                type == Node.TEXT_NODE )
                return true;
            return false;
        }

        /**
         * Returns if this node contains complex content.  That is, if this node has child nodes that are element-type nodes.
         */
        public boolean hasComplexContent( ) {
            if( isSimpleTypeNode(this.node.getNodeType()) )
                return false;

            for( ENode n : this.children ) {
                if( n.node.getNodeType() == Node.ELEMENT_NODE )
                    return false;
            }
            return true;
        }

        /**
         * Returns if this node contains simple content.  An XML node is considered to have simple content if it represents a text or attribute node or an XML element with no child elements.
         */
        public boolean hasSimpleContent( ) {
            short type = this.node.getNodeType();
            if( type == Node.PROCESSING_INSTRUCTION_NODE ||
                type == Node.COMMENT_NODE )
                return false;

            for( ENode n : this.children ) {
                if( n.node.getNodeType() == Node.ELEMENT_NODE )
                    return false;
            }
            return true;
        }

        public JSArray inScopeNamespace() {
            throw new RuntimeException("inScopeNamespace not yet implemented");
        }

        public ENode insertChildAfter( Object child1, ENode child2 ) {
            return _insertChild(child1, child2, 1);
        }

        public ENode insertChildBefore( Object child1, ENode child2 ) {
            return _insertChild(child1, child2, 0);
        }

        private ENode _insertChild( Object child1, ENode child2, int j ) {
            if( isSimpleTypeNode(this.node.getNodeType() ) ) return null;
            if( child1 == null ) {
                this.children.add( 0, child2 );
                return this;
            }
            else if ( child1 instanceof ENode ) {
                for( int i=0; i<children.size(); i++) {
                    if( children.get(i) == child1 ) {
                        children.add(i+j, child2);
                        return this;
                    }
                }
            }
            return null;
        }

        public int length(){
            if ( node != null )
                return 1;
            return children.size();
        }

        public String localName() {
            if(this.node == null) return "";
            return this.node.getLocalName();
        }

        public String name() {
            if(this.node == null) return "";
            return this.node.getNodeName();
        }

        public String namespace( String prefix ) {
            if( prefix == null )
                return this.node.getNamespaceURI();

            return this.node.lookupNamespaceURI( prefix );
        }

        public JSArray namespaceDeclarations() {
            JSArray a = new JSArray();
            if( isSimpleTypeNode( this.node.getNodeType() ) )
                return a;

            Node y = this.node.getParentNode();
            throw new RuntimeException("namespaceDeclarations not yet implemented");
        }

        public String nodeKind() {
            switch ( this.node.getNodeType() ) {
            case Node.ELEMENT_NODE :
                return "element";
            case Node.COMMENT_NODE :
                return "comment";
            case Node.ATTRIBUTE_NODE :
                return "attribute";
            case Node.TEXT_NODE :
                return "text";
            case Node.PROCESSING_INSTRUCTION_NODE :
                return "processing-instruction";
            default :
                return "unknown";
            }
        }

        /** Merges adjacent text nodes and eliminates empty text nodes */
        public ENode normalize() {
            int i=0;
            while( i< this.children.size()) {
                if( children.get(i).node.getNodeType() == Node.ELEMENT_NODE ) {
                    children.get(i).normalize();
                    i++;
                }
                else if( children.get(i).node.getNodeType() == Node.TEXT_NODE )  {
                    while( i+1 < children.size() && children.get(i+1).node.getNodeType() == Node.TEXT_NODE ) {
                        children.get(i).node.setNodeValue( children.get(i).node.getNodeValue() + children.get(i+1).node.getNodeValue());
                        children.remove(i+1);
                    }
                    if( children.get(i).node.getNodeValue().length() == 0 ) {
                        children.remove(i);
                    }
                    else {
                        i++;
                    }
                }
                else {
                    i++;
                }
            }
            return this;
        }

        public ENode parent() {
            return this.parent;
        }

        public ENode processingInstructions( String name ) {
            boolean all = ( name == null || name == "*" );

            ENode list = new ENode();
            for( ENode n : this.children ) {
                if ( n.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && ( all || name.equals(n.node.getLocalName()) ) ) {
                    list.children.add( n );
                }
            }
            return list;
        }

        /** Inserts the given child into this object prior to the existing XML properties.
         */
        public ENode prependChild( ENode value ) {
            return _insertChild( null, value, 0 );
        }

        /**
         * So, the spec says that this should only return toString(prop) == "0".  However, the Rhino implementation returns true
         * whenever prop is a valid index, so I'm going with that.
         */
        public boolean propertyIsEnumerable( String prop ) {
            ENode n = (ENode)this.get(prop);
            if(n == null) return false;

            Pattern num = Pattern.compile("\\d+");
            Matcher m = num.matcher(prop);
            if( m.matches() )
                return true;

            return false;
        }

        public ENode removeNamespace( String namespace ) {
            throw new RuntimeException("not yet implemented");
        }

        public Object replace(String name, Object value) {
            ENode exists = (ENode)this.get(name);
            if( exists == null )
                return this;

            return set(name, value);
        }

        /** not right */
        public Object setChildren( Object value ) {
            if ( node != null ) {
                return set( node.getNodeName(), value );
            }
            for( ENode n : this.children ) {
                set( n.node.getNodeName(), value );
            }
            return this;
        }

        /** FIXME: implement QName
         */
        public void setLocalName( Object name ) {
            if( this.node == null ||
                this.node.getNodeType() == Node.TEXT_NODE ||
                this.node.getNodeType() == Node.COMMENT_NODE )
                return;

        }

        /** FIXME: implement QName
         */
        public void setName( Object name ) {
            if( this.node == null ||
                this.node.getNodeType() == Node.TEXT_NODE ||
                this.node.getNodeType() == Node.COMMENT_NODE )
                return;
        }

        public ENode setNamespace( String namespace ) {
            throw new RuntimeException("not yet implemented");
        }

        public ENode text() {
            ENode list = new ENode();
            if( this.node != null && this.node.getNodeType() == Node.TEXT_NODE ) {
                list.children.add( this );
                return list;
            }

            for ( ENode n : this.children ) {
                if( n.node.getNodeType() == Node.TEXT_NODE ) {
                    list.children.add( n );
                }
            }
            return list;
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

        /** too painful to do right now */
        public String toXMLString() {
            return "hi there";
            //            throw new RuntimeException("not yet implemented");
        }

        public ENode valueOf() {
            return this;
        }

        public static StringBuilder append( ENode n , StringBuilder buf , int level ){
            if ( n.node.getNodeType() == Node.ATTRIBUTE_NODE )
                return _level( buf , level ).append( n.node.getNodeValue() );
            if ( n.node.getNodeType() == Node.TEXT_NODE )
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

        public ArrayList<ArrayList> getAttributes() {
            if(node == null && ( children == null || children.size() == 0)) return null;

            ArrayList<String> keys = new ArrayList<String>();
            ArrayList<Node> vals = new ArrayList<Node>();
            if(node != null) {
                NamedNodeMap temp = node.getAttributes();
                for(int j=0; temp != null && j<temp.getLength(); j++) {
                    Node n = temp.item(j);
                    keys.add( n.getNodeName() );
                    vals.add( n );
                }
            }
            else {
                for(int i=0; i<children.size(); i++) {
                    NamedNodeMap temp = children.get(i).node.getAttributes();
                    for(int j=0; temp != null && j<temp.getLength(); j++) {
                        Node n = temp.item(j);
                        keys.add( n.getNodeName() );
                        vals.add( n );
                    }
                }
            }
            ArrayList<ArrayList> r = new ArrayList<ArrayList>();
            r.add(keys);
            r.add(vals);
            return r;
        }

        public abstract class ENodeFunction extends JSFunctionCalls0 {
            public ENodeFunction() {
                super();
            }

            public Object call( Scope s, Object foo[] ) {
                return new Object();
            }

            public String toString() {
                if( cnode == null )
                    cnode = (ENode)E4X._nodeGet(ENode.this, "copy");
                return cnode == null ? "" : cnode.toString();
            }

            public Object get( Object n ) {
                if ( cnode == null ) {
                    cnode = (ENode)E4X._nodeGet(ENode.this, "copy");
                }

                return cnode.get( n );
            }

            public Object set( Object n, Object v ) {
                if( cnode == null ) {
                    // there's this stupid thing where set is called for every xml node created
                    if( ENode.this.toString() == null && n.equals("prototype") ) {
                        return null;
                    }
                    cnode = (ENode)E4X._nodeGet(ENode.this, "copy");
                }
                if( cnode == null ) return null;

                return cnode.set( n, v );
            }

            ENode cnode;
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
        if( all ) {
            if( s.length() > 1) return null;
            s = "";
        }

        List<ENode> traverse = new LinkedList<ENode>();
	List<ENode> res = new ArrayList<ENode>();

        for(int k=0; k< start.size(); k++) {
            traverse.add( start.get(k) );

            while ( ! traverse.isEmpty() ){
                ENode n = traverse.remove(0);

                if ( attr ){
                    List<ArrayList> nnm = n.getAttributes();
                    for(int i=0; nnm != null && i < nnm.get(1).size(); i++) {
                        if( all || ((String)nnm.get(0).get(i)).equals( s ) ) {
                            res.add( new ENode( (Node)nnm.get(1).get(i) ) );
                        }
                    }
                }

                List<ENode> kids = n.children;
                if ( kids == null || kids.size() == 0 )
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

