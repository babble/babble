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
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import ed.js.func.*;
import ed.js.engine.*;
import ed.util.*;

public class E4X {

    public static JSFunction _cons = new XML();
    public static JSFunction _ns = new NamespaceCons();

    public static class NamespaceCons extends JSFunctionCalls1 {

        public JSObject newOne(){
            return new Namespace();
        }

        public Object call( Scope scope , Object str , Object [] args){
            Object blah = scope.getThis();

            Namespace n;
            if ( blah instanceof Namespace)
                n = (Namespace)blah;
            else {
                n = new Namespace( str.toString() );
            }
            n.init( str.toString() );
            return n;
        }
    }

    public static class XML extends JSFunctionCalls1 {

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

        public Object get( Object o ) {
            if( o.toString().equals( "ignoreComments" ) )
                return E4X.ignoreComments;
            if( o.toString().equals( "ignoreWhitespace" ) )
                return E4X.ignoreWhitespace;
            if( o.toString().equals( "ignoreProcessingInstructions" ) )
                return E4X.ignoreProcessingInstructions;
            if( o.toString().equals( "prettyPrinting" ) )
                return E4X.prettyPrinting;
            if( o.toString().equals( "prettyIndent" ) )
                return E4X.prettyIndent;
            return null;
        }

        public Object set( Object o, Object v ) {
            if( o.toString().equals( "ignoreComments" ) )
                E4X.ignoreComments = Boolean.parseBoolean( v.toString() );
            if( o.toString().equals( "ignoreWhitespace" ) )
                E4X.ignoreWhitespace = Boolean.parseBoolean( v.toString() );
            if( o.toString().equals( "ignoreProcessingInstructions" ) )
                E4X.ignoreProcessingInstructions = Boolean.parseBoolean( v.toString() );
            if( o.toString().equals( "prettyPrinting" ) )
                E4X.prettyPrinting = Boolean.parseBoolean( v.toString() );
            if( o.toString().equals( "prettyIndent" ) )
                E4X.prettyIndent = Integer.parseInt( v.toString() );
            return v;
        }

        public static JSObject settings() {
            return E4X.settings();
        }

        public static void setSettings( JSObject obj ) {
            E4X.setSettings( obj );
        }

        public static JSObject defaultSettings() {
            return E4X.defaultSettings();
        }

    }


    public static JSObject settings() {
        JSObjectBase sets = new JSObjectBase();
        sets.set("ignoreComments", ignoreComments);
        sets.set("ignoreProcessingInstructions", ignoreProcessingInstructions);
        sets.set("ignoreWhitespace", ignoreWhitespace);
        sets.set("prettyPrinting", prettyPrinting);
        sets.set("prettyIndent", prettyIndent);
        return sets;
    }

    public static void setSettings() {
        setSettings(null);
    }

    public static void setSettings( JSObject settings ) {
        if( settings == null ) {
            ignoreComments = true;
            ignoreProcessingInstructions = true;
            ignoreWhitespace = true;
            prettyPrinting = true;
            prettyIndent = 2;
            return;
        }

        Object setting = settings.get("ignoreComments");
        if(setting != null && setting instanceof Boolean)
            ignoreComments = ((Boolean)setting).booleanValue();
        setting = settings.get("ignoreProcessingInstructions");
        if(setting != null && setting instanceof Boolean)
            ignoreProcessingInstructions = ((Boolean)setting).booleanValue();
        setting = settings.get("ignoreWhitespace");
        if(setting != null && setting instanceof Boolean)
            ignoreWhitespace = ((Boolean)setting).booleanValue();
        setting = settings.get("prettyPrinting");
        if(setting != null && setting instanceof Boolean)
            prettyPrinting = ((Boolean)setting).booleanValue();
        setting = settings.get("prettyIndent");
        if(setting != null && setting instanceof Integer)
            prettyIndent = ((Integer)setting).intValue();
    }

    public static JSObject defaultSettings() {
        JSObjectBase sets = new JSObjectBase();
        sets.set("ignoreComments", true);
        sets.set("ignoreProcessingInstructions", true);
        sets.set("ignoreWhitespace", true);
        sets.set("prettyPrinting", true);
        sets.set("prettyIndent", 2);
        return sets;
    }

    public static boolean ignoreComments = true;
    public static boolean ignoreProcessingInstructions = true;
    public static boolean ignoreWhitespace = true;
    public static boolean prettyPrinting = true;
    public static int prettyIndent = 2;

    static class ENode extends JSObjectBase {
        private ENode(){
            this( new LinkedList<ENode>() );
        }

        private ENode( Node n ) {
            this( n, null );
        }

        private ENode( List<ENode> n ) {
            this( null, null, n );
        }

        private ENode( Node n, ENode parent ) {
            this( n, parent, null );
        }

        private ENode( Node n, ENode parent, List<ENode> children ) {
            if( n != null &&
                children == null &&
                n.getNodeType() != Node.TEXT_NODE &&
                n.getNodeType() != Node.ATTRIBUTE_NODE )
                this.children = new LinkedList<ENode>();
            else if( children != null ) {
                this.children = children;
            }

            this.node = n;
            this.parent = parent;
            this.inScopeNamespaces = new ArrayList<Namespace>();
            addNativeFunctions();
        }

        // creates an empty node with a given parent and tag name
        private ENode( ENode parent, Object o ) {
            if(parent != null && parent.node != null)
                node = parent.node.getOwnerDocument().createElement(o.toString());
            this.children = new LinkedList<ENode>();
            this.parent = parent;
            this._dummy = true;
            this.inScopeNamespaces = new ArrayList<Namespace>();
            addNativeFunctions();
        }

        void addNativeFunctions() {
            nativeFuncs.put("addNamespace", new addNamespace());
            nativeFuncs.put("appendChild", new appendChild());
            nativeFuncs.put("attribute", new attribute());
            nativeFuncs.put("attributes", new attributes());
            nativeFuncs.put("child", new child());
            nativeFuncs.put("childIndex", new childIndex());
            nativeFuncs.put("children", new children());
            nativeFuncs.put("comments", new comments());
            nativeFuncs.put("contains", new contains());
            nativeFuncs.put("copy", new copy());
            nativeFuncs.put("descendants", new descendants());
            nativeFuncs.put("elements", new elements());
            nativeFuncs.put("hasOwnProperty", new hasOwnProperty());
            nativeFuncs.put("hasComplexContent", new hasComplexContent());
            nativeFuncs.put("hasSimpleContent", new hasSimpleContent());
            nativeFuncs.put("inScopeNamespaces", new inScopeNamespaces());
            nativeFuncs.put("insertChildAfter", new insertChildAfter());
            nativeFuncs.put("insertChildBefore", new insertChildBefore());
            nativeFuncs.put("length", new length());
            nativeFuncs.put("localName", new localName());
            nativeFuncs.put("name", new name());
            nativeFuncs.put("namespace", new namespace());
            nativeFuncs.put("namespaceDeclarations", new namespaceDeclarations());
            nativeFuncs.put("nodeKind", new nodeKind());
            nativeFuncs.put("normalize", new normalize());
            nativeFuncs.put("parent", new parent());
            nativeFuncs.put("processingInstructions", new processingInstructions());
            nativeFuncs.put("prependChild", new prependChild());
            nativeFuncs.put("propertyIsEnumerable", new propertyIsEnumerable());
            nativeFuncs.put("removeNamespace", new removeNamespace());
            nativeFuncs.put("replace", new replace());
            nativeFuncs.put("setChildren", new setChildren());
            nativeFuncs.put("setLocalName", new setLocalName());
            nativeFuncs.put("setName", new setName());
            nativeFuncs.put("setNamespace", new setNamespace());
            nativeFuncs.put("text", new text());
            nativeFuncs.put("toString", new toString());
            nativeFuncs.put("toXMLString", new toXMLString());
            nativeFuncs.put("valueOf", new valueOf());
        }

        void buildENodeDom(ENode parent) {
            NamedNodeMap attr = parent.node.getAttributes();
            for( int i=0; attr != null && i< attr.getLength(); i++) {
                parent.children.add( new ENode(attr.item(i), parent ) );
            }
            if( parent.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ) {
                Properties piProp = new Properties();
                try {
                    piProp.load(new ByteArrayInputStream(((ProcessingInstruction)parent.node).getData().replaceAll("\" ", "\"\n").getBytes("UTF-8")));
                    for (Enumeration e = piProp.propertyNames(); e.hasMoreElements();) {
                        String propName = e.nextElement().toString();
                        String propValue = piProp.getProperty( propName );
                        Attr pi = parent.node.getOwnerDocument().createAttribute(propName.toString());
                        pi.setValue( propValue.substring(1, propValue.length()-1) );
                        parent.children.add( new ENode( pi ) );
                    }
                }
                catch (Exception e) {
                    System.out.println("no processing instructions for you.");
                    e.printStackTrace();
                }
            }
            NodeList kids = parent.node.getChildNodes();
            for( int i=0; i<kids.getLength(); i++) {
                if( ( kids.item(i).getNodeType() == Node.COMMENT_NODE && E4X.ignoreComments ) ||
                    ( kids.item(i).getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && E4X.ignoreProcessingInstructions ) )
                    continue;
                ENode n = new ENode(kids.item(i), parent);
                buildENodeDom(n);
                parent.children.add(n);
            }
        }

        void init( String s ){
            try {
                // get rid of newlines and spaces if ignoreWhitespace is set (default)
                if( E4X.ignoreWhitespace ) {
                    Pattern p = Pattern.compile("\\>\\s+\\<");
                    Matcher m = p.matcher(s);
                    s = m.replaceAll("><");
                }
                _document = XMLUtil.parse( s );
                children = new LinkedList<ENode>();
                node = _document.getDocumentElement();
                buildENodeDom(this);
            }
            catch ( Exception e ){
                throw new RuntimeException( "can't parse : " + e );
            }
        }

        Hashtable<String, ENodeFunction> nativeFuncs = new Hashtable<String, ENodeFunction>();

        public Object get( Object n ){
            if ( n == null )
                return null;

            if ( n instanceof Number )
                return child( ((Number)n).intValue() );

            if ( n instanceof String || n instanceof JSString ){
                String s = n.toString();

                if( nativeFuncs.containsKey(s) )
                    return nativeFuncs.get( s );

                if(s.equals("tojson")) return null;

                Object o = _nodeGet( this, s );
                return (o == null && E4X.isXMLName(s)) ? new ENode( this, s ) : o;
            }

            if ( n instanceof Query ) {
		Query q = (Query)n;
		List<ENode> matching = new ArrayList<ENode>();
		for ( ENode theNode : children ){
		    if ( q.match( theNode ) ) {
			matching.add( theNode );
                    }
		}
		return _handleListReturn( matching );
            }

            throw new RuntimeException( "can't handle : " + n.getClass() );
        }

        public Object set( Object k, Object v ) {
            if( v == null ) 
                v = "null";
            if(this.children == null ) this.children = new LinkedList<ENode>();

            if( k.toString().startsWith("@") )
                return setAttribute(k.toString(), v.toString());

            // attach any dummy ancestors to the tree
            if( this._dummy ) {
                ENode topParent = this;
                this._dummy = false;
                while( topParent.parent._dummy ) {
                    topParent = topParent.parent;
                    topParent._dummy = false;
                }
                topParent.parent.children.add(topParent);
            }

            // if v is already XML and it's not an XML attribute, just add v to this enode's children
            if( v instanceof ENode ) {
                if( k.toString().equals("*") ) {
                    this.children = new LinkedList<ENode>();
                    this.children.add((ENode)v);
                }
                else {
                    this.children.add((ENode)v);
                }
                return v;
            }

            // find out if this k/v pair exists
            ENode n;
            Object obj = get(k);
            if( obj instanceof ENode )
                n = ( ENode )obj;
            else {
                n = (( ENodeFunction )obj).cnode;
                if( n == null ) {
                    n = new ENode();
                }
            }

            Pattern num = Pattern.compile("-?\\d+");
            Matcher m = num.matcher(k.toString());

            // k is a number
            if( m.matches() ) {
                int index;
                // the index must be greater than 0
                if( ( index = Integer.parseInt(k.toString()) ) < 0)
                    return v;

                // this index is greater than the number of elements existing
                if( index >= n.children.size() ) {
                    Node content;
                    if(this.node != null)
                        content = this.node.getOwnerDocument().createTextNode(v.toString());
                    else if(this.children != null && this.children.size() > 0)
                        content = this.children.get(0).node.getOwnerDocument().createTextNode(v.toString());
                    else
                        return null;

                    // if k/v doesn't really exist, "get" returns a dummy node, an emtpy node with nodeName = key
                    if( n._dummy ) {
                        // if there is a list of future siblings, get the last one
                        ENode rep = this.parent == null ? this.children.get(this.children.size()-1) : this;
                        ENode attachee = rep.parent;
                        n.node = rep.node.getOwnerDocument().createElement(rep.node.getNodeName());
                        n.children.add( new ENode( content, n ) );
                        n.parent = attachee;
                        attachee.children.add( rep.childIndex()+1, n );
                        n._dummy = false;
                    }
                    else {
                        Node newNode;
                        ENode refNode = ( this.node == null ) ? this.children.get( this.children.size() - 1 ) : this.parent;
                        newNode = refNode.node.getOwnerDocument().createElement(refNode.node.getNodeName());
                        newNode.appendChild(content);

                        ENode newEn = new ENode(newNode, this);
                        buildENodeDom(newEn);

                        // get the last sibling's position & insert this new one there
                        int childIdx = refNode.childIndex();
                        refNode.parent.children.add( childIdx+1, newEn );
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
                // XMLList
                if(this.node == null ) {
                    return v;
                }
                int index = this.children.size();

                if( n.node != null && n.node.getNodeType() != Node.ATTRIBUTE_NODE) {
                    index = this.children.indexOf( n );
                    this.children.remove( n );
                }
                else {
                // there are a list of children, so delete them all and replace with the new k/v
                    for( int i=0; n != null && n.children != null && i<n.children.size(); i++) {
                        if( n.children.get(i).node.getNodeType() == Node.ATTRIBUTE_NODE ) 
                            continue;
                        // find the index of this node in the tree
                        index = this.children.indexOf( n.children.get(i) );
                        // remove it from the tree
                        this.children.remove( n.children.get(i) ) ;
                    }
                }

                n = new ENode(this.node.getOwnerDocument().createElement(k.toString()), this);
                Node content = this.node.getOwnerDocument().createTextNode(v.toString());
                n.children.add( new ENode( content, n ) );
                if( !this.children.contains( n ) )
                    if( index >= 0 )
                        this.children.add( index, n );
                    else
                        this.children.add( n );
            }
            return v;
        }

        private Object setAttribute( String k, String v ) {
            if( !k.startsWith("@") )
                return v;

            Object obj = get(k);
            k = k.substring(1);

            // create a new attribute
            if( obj == null ) {
                Attr newNode = node.getOwnerDocument().createAttribute(k);
                newNode.setValue( v );
                this.children.add( new ENode(newNode, this) );
            }
            // change an existing attribute
            else {
                List<ENode> list = this.getAttributes();
                for( ENode n : list ) {
                    if( ((Attr)n.node).getName().equals( k ) )
                        ((Attr)n.node).setValue( v );
                }
            }
            return v;
        }

        /**
         * Called for delete xml.prop
         */
        public Object removeField(Object o) {
            ENode n = (ENode)get(o);

            if(n.node != null)
                return n.parent.children.remove(n);

            for(ENode e : n.children)
                children.remove(e);

            return true;
        }

        public class addNamespace extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                throw new RuntimeException("not yet implemented");
            }
        }

        private ENode appendChild(Node child, ENode parent) {
            if(parent.children == null)
                parent.children = new LinkedList<ENode>();

            ENode echild = new ENode(child, parent);
            buildENodeDom(echild);
            parent.children.add(echild);
            return this;
        }

        private ENode appendChild(ENode child) {
            return appendChild(child.node, this);
        }

        public class appendChild extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                Object obj = s.getThis();
                ENode parent = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;

                if( foo.length == 0 )
                    return parent;

                ENode child = toXML( foo[0] );
                return child == null ? parent : parent.appendChild(child);
            }
        }

        private String attribute( String prop ) {
            Object o = this.get("@"+prop);
            return (o == null) ? "" : o.toString();
        }

        public class attribute extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                if(foo.length == 0)
                    return null;

                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.attribute(foo[0].toString());
            }
        }
        public class attributes extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                Object obj = s.getThis();
                if(obj instanceof ENode)
                    return ((ENode)obj).get("@*");
                else
                    return ((ENodeFunction)obj).cnode.get("@*");
            }
        }


        private ENode child(Object propertyName) {
            Pattern num = Pattern.compile("-?\\d+(\\.\\d+)?");
            Matcher m = num.matcher(propertyName.toString());
            if( m.matches() ) {
                int i = Integer.parseInt(propertyName.toString());
                if(i < 0 )
                    return null;

                if( i < this.children.size() )
                    return (ENode)this.children.get(i);
                else {
                    return new ENode( this, this.node == null ? null : this.node.getNodeName() );
                }
            }
            else {
                return (ENode)this.get(propertyName);
            }
        }

        public class child extends ENodeFunction {
            public Object call( Scope s,  Object foo[]) {
                if( foo.length == 0 )
                    return null;
                Object obj = s.getThis();
                if( obj instanceof ENode )
                    return ((ENode)obj).child( foo[0].toString() );
                else
                    return ((ENodeFunction)obj).cnode.child( foo[0].toString() );
            }
        }

        private int childIndex() {
            if( parent == null || parent.node.getNodeType() == Node.ATTRIBUTE_NODE )
                return -1;

            List<ENode> sibs = parent.children;
            for( int i=0; i<sibs.size(); i++ ) {
                if(sibs.get(i).equals(this))
                    return i;
            }
            return -1;
        }

        public class childIndex extends ENodeFunction {
            public Object call (Scope s, Object foo[] ) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.childIndex();
            }
        }

        private ENode children() {
            ENode childrenNode = this.copy();
            childrenNode.node = null;
            return childrenNode;
        }

        public class children extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.children();
            }
        }


        public ENode clone() {
            ENode newNode = new ENode(this.node, this.parent);
            newNode.children.addAll(this.children);
            return newNode;
        }

        public ENode comments() {
            ENode comments = new ENode();

            for( ENode child : this.children ) {
                if( child.node.getNodeType() == Node.COMMENT_NODE )
                    comments.children.add( child );
            }
            return comments;
        }

        public class comments extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                Object obj = s.getThis();
                ENode t = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return t.comments();
            }
        }

        // this is to spec, but doesn't seem right... it's "equals", not "contains"
        public class contains extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                if( foo.length == 0 || !(foo[0] instanceof ENode) )
                    return false;
                ENode o = (ENode)foo[0];
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.node.isEqualNode(o.node);
            }
        }

        private ENode copy() {
            return (ENode)this.clone();
        }

        public class copy extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.copy();
            }
        };


        private ENode descendants( String name ) {
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

        public class descendants extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                String name = ( foo.length == 0) ? "*" : foo[0].toString();
                return enode.descendants(name);
            }
        }

        public class elements extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                String name = (foo.length == 0) ? "*" : foo[0].toString();

                if( enode.children == null || enode.children.size() == 0)
                    return null;

                if(name == null || name == "") {
                    name = "*";
                }

                ENode list = new ENode();
                for( ENode n : enode.children ) {
                    if( n.node != null && n.node.getNodeType() == Node.ELEMENT_NODE && (name.equals( "*" ) || n.node.getNodeName().equals(name)) )
                    list.children.add( n );
                }

                return list;
            }
        }

        public class hasOwnProperty extends ENodeFunction {
            public Object call(Scope s, Object foo[] ) {
                Object obj = s.getThis();
                ENode en = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;

                if(foo.length == 0 || en.children == null || en.children.size() == 0)
                    return false;

                String prop = foo[0].toString();

                for( ENode n : en.children ) {
                    if( n.node != null && n.node.getNodeName().equals(prop) )
                        return true;
                }
                return false;
            }
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
        public class hasComplexContent extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode en = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                if( isSimpleTypeNode(en.node.getNodeType()) )
                    return false;

                for( ENode n : en.children ) {
                    if( n.node.getNodeType() == Node.ELEMENT_NODE )
                        return false;
                }
                return true;
            }
        }

        /**
         * Returns if this node contains simple content.  An XML node is considered to have simple content if it represents a text or attribute node or an XML element with no child elements.
         */
        public class hasSimpleContent extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode en = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                short type = en.node.getNodeType();
                if( type == Node.PROCESSING_INSTRUCTION_NODE ||
                    type == Node.COMMENT_NODE )
                    return false;

                for( ENode n : en.children ) {
                    if( n.node.getNodeType() == Node.ELEMENT_NODE )
                        return false;
                }
                return true;
            }
        }

        private JSObject _getUniqueNamespaces() {
            JSObject inScopeNS = new JSObjectBase();
            ENode y = this.copy();
            while( y != null ) {
                for( Namespace ns : y.inScopeNamespaces ) {
                    if( inScopeNS.get( ns.prefix ) != null )
                        inScopeNS.set( ns.prefix.toString(), ns );
                }
                y = y.parent;
            }
            return inScopeNS;
        }

        private JSArray inScopeNamespaces() {
            JSObject inScopeNS = _getUniqueNamespaces();
            JSArray a = new JSArray();
            Iterator k = (inScopeNS.keySet()).iterator();
            while(k.hasNext()) {
                a.add( inScopeNS.get(k).toString() );
            }
            return a;
        }

        public class inScopeNamespaces extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.inScopeNamespaces();
            }
        }

        private ENode insertChildAfter(Object child1, ENode child2) {
            return _insertChild(child1, child2, 1);
        }

        private ENode insertChildBefore(Object child1, ENode child2) {
            return _insertChild(child1, child2, 0);
        }

        public class insertChildAfter extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                if(foo.length == 0 )
                    return enode.insertChildAfter((Object)null, (ENode)null);
                if(foo.length == 1 )
                    return enode.insertChildAfter((Object)foo[0], (ENode)null);
                return enode.insertChildAfter((Object)foo[0], (ENode)foo[1]);
            }
        }

        public class insertChildBefore extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                if(foo.length == 0 )
                    return enode.insertChildBefore((Object)null, (ENode)null);
                if(foo.length == 1 )
                    return enode.insertChildBefore((Object)foo[0], (ENode)null);
                return enode.insertChildBefore((Object)foo[0], (ENode)foo[1]);
            }
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

        public class length extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return ( enode.node != null ) ? 1 : enode.children.size();
            }
        }

        private String localName() {
            return this.name == null ? null : this.name.localName;
        }

        public class localName extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.localName();
            }
        }

        private QName name() {
            return this.name;
        }

        public class name extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.name();
            }
        }

        private Namespace namespace( String prefix ) {
            JSObject obj = _getUniqueNamespaces();
            if( prefix == null ) {
                if( isSimpleTypeNode( this.node.getNodeType() ) )
                    return null;
                return this.name.getNamespace( obj );
            }
            else {
                return (Namespace)obj.get( prefix );
            }
        }

        public class namespace extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                String prefix = (foo.length > 0) ? foo[0].toString() : null;
                return enode.namespace( prefix );
            }
        }


        private JSArray namespaceDeclarations() {
            JSArray a = new JSArray();
            if( isSimpleTypeNode( this.node.getNodeType() ) )
                return a;

            ArrayList<Namespace> declaredNS = (ArrayList<Namespace>)this.parent.inScopeNamespaces.clone();
            for( int i=0; i < this.inScopeNamespaces.size(); i++) {
                declaredNS.remove( this.inScopeNamespaces.get(i) );
            }
            for( int i=0; i < declaredNS.size(); i++) {
                a.add( declaredNS.get(i) );
            }
            return a;
        }

        public class namespaceDeclarations extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.namespaceDeclarations();
            }
        }

        public class nodeKind extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode n = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                switch ( n.node.getNodeType() ) {
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
        }

        private ENode normalize() {
            int i=0;
            while( i< this.children.size()) {
                if( this.children.get(i).node.getNodeType() == Node.ELEMENT_NODE ) {
                    this.children.get(i).normalize();
                    i++;
                }
                else if( this.children.get(i).node.getNodeType() == Node.TEXT_NODE )  {
                    while( i+1 < this.children.size() && this.children.get(i+1).node.getNodeType() == Node.TEXT_NODE ) {
                        this.children.get(i).node.setNodeValue( this.children.get(i).node.getNodeValue() + this.children.get(i+1).node.getNodeValue());
                        this.children.remove(i+1);
                    }
                    if( this.children.get(i).node.getNodeValue().length() == 0 ) {
                        this.children.remove(i);
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

        /** Merges adjacent text nodes and eliminates empty text nodes */
        public class normalize extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode n = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return n.normalize();
            }
        }

        public class parent extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode n = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return n.parent;
            }
        }

        public ENode processingInstructions( String name ) {
            boolean all = name.equals( "*" );

            ENode list = new ENode();
            for( ENode n : this.children ) {
                if ( n.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && ( all || name.equals(n.node.getLocalName()) ) ) {
                    list.children.add( n );
                }
            }
            return list;
        }

        public class processingInstructions extends ENodeFunction {
            public Object call(Scope s, Object foo[] ) {
                Object obj = s.getThis();
                ENode en = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;

                String name = (foo.length == 0 ) ? "*" : foo[0].toString();
                return en.processingInstructions(name);
            }
        }

        /** Inserts the given child into this object prior to the existing XML properties.
         */
        public class prependChild extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode en = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return en._insertChild( (Object)null, (ENode)foo[0], 0 );
            }
        }

        /**
         * So, the spec says that this should only return toString(prop) == "0".  However, the Rhino implementation returns true
         * whenever prop is a valid index, so I'm going with that.
         */
        private boolean propertyIsEnumerable( String prop ) {
            Pattern num = Pattern.compile("\\d+");
            Matcher m = num.matcher(prop);
            if( m.matches() ) {
                ENode n = this.child(prop);
                return !n._dummy;
            }
            return false;
        }

        public class propertyIsEnumerable extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                String prop = foo[0].toString();
                return enode.propertyIsEnumerable( prop );
            }
        }

        public class removeNamespace extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                throw new RuntimeException("not yet implemented");
            }
        }

        public class replace extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;

                String name = foo[0].toString();
                Object value = foo[1];
                ENode exists = (ENode)enode.get(name);
                if( exists == null )
                    return this;

                return enode.set(name, value);
            }
        }

        private Object setChildren( Object value ) {
            this.set("*", value);
            return this;
        }

        /** not right */
        public class setChildren extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                Object value = foo[0];
                return enode.setChildren(value);
            }
        }

        private void setLocalName( Object name ) {
            if( this.node == null ||
                this.node.getNodeType() == Node.TEXT_NODE ||
                this.node.getNodeType() == Node.COMMENT_NODE )
                return;
            this.name.localName = ( name instanceof QName ) ? ((QName)name).localName : name.toString();
        }

        public class setLocalName extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode n = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                Object name = (foo.length > 0) ? foo[0] : null;
                if(name == null)
                    return null;
                n.setLocalName( name );
                return null;
            }
        }

        private void setName( Object name ) {
            if( this.node == null ||
                this.node.getNodeType() == Node.TEXT_NODE ||
                this.node.getNodeType() == Node.COMMENT_NODE )
                return;
            if ( name instanceof QName && ((QName)name).uri.equals("") )
                name = ((QName)name).localName;
            QName n = new QName( name );
            if( this.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE )
                n.uri = "";
            this.name = n;
            Namespace ns = new Namespace( n.prefix, n.uri );
            if( this.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
                if( this.parent == null )
                    return;
                this.parent.addInScopeNamespace( ns );
            }
            if( this.node.getNodeType() == Node.ELEMENT_NODE )
                this.addInScopeNamespace( ns );
        }

        public class setName extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode n = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                if (foo.length > 0 )
                    n.setName(foo[0].toString());
                return null;
            }
        }

        private void setNamespace( Object ns) {
            if( this.node == null ||
                this.node.getNodeType() == Node.TEXT_NODE ||
                this.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ||
                this.node.getNodeType() == Node.COMMENT_NODE )
                return;
            Namespace ns2 = new Namespace( ns );
            this.name = new QName( ns2, this.name );
            if( this.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
                if (this.parent == null )
                    return;
                this.parent.addInScopeNamespace(ns2 );
            }
            if( this.node.getNodeType() == Node.ELEMENT_NODE ) {
                this.addInScopeNamespace(ns2 );
            }
        }

        public class setNamespace extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                if( foo.length == 0 )
                    return null;

                Object obj = s.getThis();
                ENode n = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                n.setNamespace( foo[0] );
                return null;
            }
        }

        public class text extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode en = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                ENode list = new ENode();
                if( en.node != null && en.node.getNodeType() == Node.TEXT_NODE ) {
                    list.children.add( en );
                    return list;
                }

                for ( ENode n : en.children ) {
                    if( n.node.getNodeType() == Node.TEXT_NODE ) {
                        list.children.add( n );
                    }
                }
                return list;
            }
        }

        public String toString() {
            if ( this.node == null && this.children == null )
                return null;

            StringBuilder xml = new StringBuilder();
            // XML
            if( this.node != null || this.children.size() == 1 ) {
                ENode singleNode = ( this.node != null ) ? this : this.children.get(0);
                List<ENode> kids = singleNode.printableChildren();

                // if this is an empty top level element, return nothing
                if( singleNode.node.getNodeType() == Node.ELEMENT_NODE && ( kids == null || kids.size() == 0 ) )
                    return "";

                if( singleNode.node.getNodeType() == Node.ATTRIBUTE_NODE || singleNode.node.getNodeType() == Node.TEXT_NODE )
                    return singleNode.node.getNodeValue();

                if ( singleNode.node.getNodeType() == Node.ELEMENT_NODE &&
                     singleNode.children != null &&
                     singleNode.childrenAreTextNodes() ) {
                    for( ENode n : singleNode.children )
                        xml.append( n.node.getNodeValue() );
                    return xml.toString();
                }

                append( singleNode, xml, 0);
            }
            // XMLList
            else {
                for( int i=0; i<this.children.size(); i++ ) {
                    append( this.children.get( i ), xml, 0);
                }
            }

            // XMLUtil's toString always appends a "\n" to the end
            if( xml.length() > 0 && xml.charAt(xml.length() - 1) == '\n' ) {
                xml.deleteCharAt(xml.length()-1);
            }
            return xml.toString();
        }

        public StringBuilder append( ENode n , StringBuilder buf , int level ){
            switch (n.node.getNodeType() ) {
            case Node.ATTRIBUTE_NODE:
                return _level(buf, level).append( E4X.prettyPrinting ? n.node.getNodeValue().trim() : n.node.getNodeValue() );
            case Node.TEXT_NODE:
                return _level(buf, level).append( E4X.prettyPrinting ? n.node.getNodeValue().trim() : n.node.getNodeValue() ).append("\n");
            case Node.COMMENT_NODE:
                return _level(buf, level).append( "<!--"+n.node.getNodeValue()+"-->" ).append("\n");
            case Node.PROCESSING_INSTRUCTION_NODE:
                return _level(buf, level).append( "<?"+n.node.getNodeName() + attributesToString( n )+"?>").append("\n");
            }

            _level( buf , level ).append( "<" ).append( n.node.getNodeName() );
            buf.append(attributesToString( n ));

            List<ENode> kids = n.printableChildren();
            if ( kids == null || kids.size() == 0 ) {
                return buf.append( "/>\n" );
            }

            buf.append(">");
            if( (kids.size() == 1 && kids.get(0).node.getNodeType() == Node.ELEMENT_NODE) ||
                kids.size() > 1 ) {
                buf.append( "\n" );
            }
            else {
                return buf.append( E4X.prettyPrinting ? kids.get(0).node.getNodeValue().trim() : kids.get(0).node.getNodeValue() ).append( "</" ).append( n.node.getNodeName() ).append( ">\n" );
            }

            for ( int i=0; i<kids.size(); i++ ){
                ENode c = kids.get(i);
                if( ( E4X.ignoreComments && c.node.getNodeType() == Node.ATTRIBUTE_NODE ) ||
                    ( E4X.ignoreComments && c.node.getNodeType() == Node.COMMENT_NODE ) ||
                    ( E4X.ignoreProcessingInstructions && c.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ) )
                    continue;
                else {
                    append( c , buf , level + 1 );
                }
            }

            return _level( buf , level ).append( "</" ).append( n.node.getNodeName() ).append( ">\n" );
        }

        private StringBuilder _level( StringBuilder buf , int level ){
            for ( int i=0; i<level; i++ ) {
                for( int j=0; j<E4X.prettyIndent; j++) {
                    buf.append( " " );
                }
            }
            return buf;
        }

        private String attributesToString( ENode n ) {
            StringBuilder buf = new StringBuilder();
            ArrayList<ENode> attr = n.getAttributes();
            String[] attrArr = new String[attr.size()];
            for( int i = 0; i< attr.size(); i++ ) {
                attrArr[i] = " " + attr.get(i).node.getNodeName() + "=\"" + attr.get(i).node.getNodeValue() + "\"";
            }
            Arrays.sort(attrArr);
            for( String a : attrArr ) {
                buf.append( a );
            }
            return buf.toString();
        }

        private List<ENode> printableChildren() {
            List list = new LinkedList<ENode>();
            for ( int i=0; this.children != null && i<this.children.size(); i++ ){
                ENode c = this.children.get(i);
                if( c.node.getNodeType() == Node.ATTRIBUTE_NODE ||
                    ( c.node.getNodeType() == Node.COMMENT_NODE && E4X.ignoreComments ) ||
                    ( c.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && E4X.ignoreProcessingInstructions ) )
                    continue;
                list.add(c);
            }
            return list;
        }

        private boolean childrenAreTextNodes() {
            for( ENode n : this.children ) {
                if( n.node.getNodeType() != Node.TEXT_NODE )
                    return false;
            }
            return true;
        }

        public class toString extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode n = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;

                return n.toString();
            }
        }

        /** too painful to do right now */
        public class toXMLString extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode n = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;

                throw new RuntimeException("not yet implemented");
            }
        }

        public class valueOf extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                return ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
            }
        }


        public NodeList getChildNodes() {
            return node.getChildNodes();
        }


        private void addInScopeNamespace( Namespace n ) {
            if (this.node == null)
                return;
            short type = this.node.getNodeType();
            if( type == Node.COMMENT_NODE ||
                type == Node.PROCESSING_INSTRUCTION_NODE ||
                type == Node.TEXT_NODE ||
                type == Node.ATTRIBUTE_NODE )
                return;

            if( n.prefix == null )
                return;

            if( n.prefix.equals("") && (this.name == null || this.name.uri.equals("") ))
                return;

            Namespace match = null;
            for( Namespace ns : this.inScopeNamespaces ) {
                if( n.prefix.equals( ns.prefix ) ) {
                    match = ns;
                }
            }
            if( match != null && !match.uri.equals(n.uri) )
                this.inScopeNamespaces.remove(match);
            this.inScopeNamespaces.add( n );

            if( this.name.prefix.equals( n.prefix ) )
                this.name.prefix = null;
        }

        public ArrayList getAttributes() {
            if(node == null && ( children == null || children.size() == 0)) return null;

            ArrayList<ENode> list = new ArrayList<ENode>();

            if(this.node != null && this.children != null) {
                for( ENode child : this.children ) {
                    if( child.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
                        list.add(child);
                    }
                }
            }
            else if (this.children != null ) {
                for( ENode child : this.children ) {
                    if( child.node.getNodeType() == Node.ELEMENT_NODE ) {
                        list.addAll(child.getAttributes());
                    }
                }
            }
            return list;
        }

        public ENode toXML( Object input ) {
            if( input == null )
                return null;

            if( input instanceof Boolean ||
                input instanceof Number ||
                input instanceof JSString )
                return toXML(input.toString());
            else if( input instanceof String )
                return new ENode(this.node.getOwnerDocument().createTextNode((String)input));
            else if( input instanceof Node )
                return new ENode((Node)input);
            else if( input instanceof ENode )
                return (ENode)input;
            else
                return null;
        }

        public abstract class ENodeFunction extends JSFunctionCalls0 {
            public String toString() {
                if( cnode == null ) {
                    cnode = (ENode)E4X._nodeGet(ENode.this, this.getClass().getSimpleName());
                }
                return cnode == null ? "" : cnode.toString();
            }

            public Object get( Object n ) {
                if ( cnode == null ) {
                    cnode = (ENode)E4X._nodeGet(ENode.this, this.getClass().getSimpleName());
                }
                return cnode.get( n );
            }

            public Object set( Object n, Object v ) {
                // there's this stupid thing where set is called for every xml node created
                if( n.equals("prototype") && v instanceof JSObjectBase)
                    return null;

                if( cnode == null ) {
                    cnode = (ENode)E4X._nodeGet(ENode.this, this.getClass().getSimpleName());
                    if( cnode == null )
                        return null;
                }
                return cnode.set( n, v );
            }

            ENode cnode;
        }

        private Document _document;

        private List<ENode> children;
        private ENode parent;
        private Node node;

        private boolean _dummy;
        private ArrayList<Namespace> inScopeNamespaces;
        private QName name;
    }

    /*    static class XMLList implements List {
        private List<ENode> nodes;
    }
    */
    static Object _nodeGet( ENode start , String s ){
        List<ENode> ln = new LinkedList<ENode>();
        if( start.node == null )
            for(int i=0; i<start.children.size(); i++) {
                ln.add(start.children.get(i));
            }
        else
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
                    ArrayList<ENode> nnm = n.getAttributes();
                    for(int i=0; i < nnm.size(); i++) {
                        if( all || nnm.get(i).node.getNodeName().equals( s ) ) {
                            res.add( nnm.get(i) );
                        }
                    }
                }

                List<ENode> kids = n.children;
                if ( kids == null || kids.size() == 0 )
                    continue;

                for ( int i=0; i<kids.size(); i++ ){
                    ENode c = kids.get(i);
                    if ( ! attr && c.node.getNodeType() != Node.ATTRIBUTE_NODE && ( all || c.node.getNodeName().equals( s ) ) ) {
                        res.add( c );
                    }

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
            return lst.get(0);
        }
        return new ENode(lst);
    }

    public static abstract class Query {
	public Query( String what , JSString match ){
	    _what = what;
	    _match = match;
	}

	abstract boolean match( ENode n );

	final String _what;
	final JSString _match;
    }

    public static class Query_EQ extends Query {

	public Query_EQ( String what , JSString match ){
	    super( what , match );
	}

	boolean match( ENode n ){
            ENode result = (ENode)n.get( _what );
            if( result._dummy )
                return false;
            if( result.node.getNodeType() == Node.ATTRIBUTE_NODE )
                return result.node.getNodeValue().equals( _match.toString() );
            else
                return JSInternalFunctions.JS_eq( _nodeGet( n , _what ) , _match );
	}

	public String toString(){
	    return " [[ " + _what + " == " + _match + " ]] ";
	}

    }

    public static boolean isXMLName( String name ) {
        Pattern invalidChars = Pattern.compile("[@\\s\\{\\/\\']|(\\.\\.)|(\\:\\:)");
        Matcher m = invalidChars.matcher( name );
        if( m.find() ) {
            return false;
        }
        return true;
    }

    static class QName {
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
                    this.localName = ((QName)name).localName;
                }
            }
            if( name == null ) {
                this.localName = "";
            }
            else {
                this.localName = name.toString();
            }
            if( namespace == null ) {
                if( this.localName.equals("*") ) {
                    namespace = null;
                }
                else {
                    namespace = E4X.getDefaultNamespace();
                }
            }
            if( namespace == null ) {
                this.uri = null;
            }
            else {
                namespace = new Namespace(namespace);
                this.uri = namespace.uri;
            }
        }

        public String toString() {
            String s = "";
            if( !this.uri.equals("") ) {
                if( this.uri == null ) {
                    s = "*::";
                }
                else {
                    s = this.uri + "*::";
                }
            }
            return s + this.localName;
        }

        public Namespace getNamespace( JSObject inScopeNS ) {
            if( this.uri == null )
                return null;

            if( inScopeNS == null )
                inScopeNS = new JSObjectBase();

            Namespace ns = (Namespace)inScopeNS.get( this.uri );
            if( ns == null )
                return new Namespace( this.uri );
            return ns;
        }
    }

    static class Namespace extends JSObjectBase {

        void init( String s ) {
            this.uri = s;
            defaultNamespace = new Namespace( s );
        }

        public String prefix;
        public String uri;

        public Namespace() {
            this(null, null);
        }

        public Namespace( Object uri) {
            this(null, uri);
        }

        public Namespace( String prefix, Object uri) {
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
                    if( this.uri.equals("") )
                        this.prefix = "";
                    else
                        this.prefix = null;
                }
            }
            else {
                if( uri instanceof QName && ((QName)uri).uri != null) {
                    this.uri = ((QName)uri).uri;
                }
                else {
                    this.uri = uri.toString();
                }
                if( this.uri.equals("") ) {
                    if( prefix == null || prefix.equals("") ) {
                        this.prefix = "";
                    }
                    else {
                        return;
                    }
                }
                else if( prefix == null ||  !E4X.isXMLName( prefix ) ) {
                    this.prefix = null;
                }
                else {
                    this.prefix = prefix;
                }
            }
        }

        public String toString() {
            return this.uri;
        }
    }

    private static Namespace defaultNamespace;

    public static Namespace getDefaultNamespace() {
        if(defaultNamespace == null)
            defaultNamespace = new Namespace();
        return defaultNamespace;
    }

    public static Namespace setAndGetDefaultNamespace(Object o) {
        if( o instanceof Namespace )
            defaultNamespace = (Namespace)o;
        return defaultNamespace;
    }

}
