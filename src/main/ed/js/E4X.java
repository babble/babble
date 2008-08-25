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

    public static JSFunction _cons = new Cons();
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

    public static class Cons extends JSFunctionCalls1 {

        public JSObject newOne(){
            return new ENode( this, defaultNamespace );
        }

        public Object call( Scope scope , Object str , Object [] args){
            Object blah = scope.getThis();

            ENode e;
            if ( blah instanceof ENode)
                e = (ENode)blah;
            else
                e = new ENode( this, defaultNamespace );
            e.init( str.toString() );
            return e;
        }

        public JSObject settings() {
            JSObjectBase sets = new JSObjectBase();
            sets.set("ignoreComments", ignoreComments);
            sets.set("ignoreProcessingInstructions", ignoreProcessingInstructions);
            sets.set("ignoreWhitespace", ignoreWhitespace);
            sets.set("prettyPrinting", prettyPrinting);
            sets.set("prettyIndent", prettyIndent);
            return sets;
        }

        public void setSettings() {
            setSettings(null);
        }

        public void setSettings( JSObject settings ) {
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

        public JSObject defaultSettings() {
            JSObjectBase sets = new JSObjectBase();
            sets.set("ignoreComments", true);
            sets.set("ignoreProcessingInstructions", true);
            sets.set("ignoreWhitespace", true);
            sets.set("prettyPrinting", true);
            sets.set("prettyIndent", 2);
            return sets;
        }

        public boolean ignoreComments = true;
        public boolean ignoreProcessingInstructions = true;
        public boolean ignoreWhitespace = true;
        public boolean prettyPrinting = true;
        public int prettyIndent = 2;

        public Namespace defaultNamespace = new Namespace();
        
        public Object get( Object n ) {
            String s = n.toString();
            if( s.equals( "ignoreComments" ) )
                return ignoreComments;
            if( s.equals( "ignoreProcessingInstructions " ) )
                return ignoreProcessingInstructions;
            if( s.equals( "ignoreWhitespace" ) )
                return ignoreWhitespace;
            if( s.equals( "prettyPrinting" ) )
                return prettyPrinting;
            if( s.equals( "prettyIndent" ) )
                return prettyIndent;
            return null;
        }

        public Object set( Object k, Object v ) {
            String s = k.toString();
            String val = v.toString();
            if( s.equals( "ignoreComments" ) )
                ignoreComments = Boolean.parseBoolean(val);
            if( s.equals( "ignoreProcessingInstructions" ) ) 
                ignoreProcessingInstructions = Boolean.parseBoolean(val);
            if( s.equals( "ignoreWhitespace" ) )
                ignoreWhitespace = Boolean.parseBoolean(val);
            if( s.equals( "prettyPrinting" ) )
                prettyPrinting = Boolean.parseBoolean(val);
            if( s.equals( "prettyIndent" ) )
                prettyIndent = Integer.parseInt(val);
            return v;
        }

        public Namespace getDefaultNamespace() {
            return defaultNamespace;
        }

        public Namespace setAndGetDefaultNamespace(Object o) {
            defaultNamespace = new Namespace(o);
            return defaultNamespace;
        }
    }


    static class ENode extends JSObjectBase {
        private E4X.Cons XML;

        private ENode(){
            nodeSetup( null );
        }

        private ENode( E4X.Cons c, Namespace ns ) {
            XML = c;
            defaultNamespace = ns;
            nodeSetup( null );
        }

        private ENode( Node n ) {
            this( n, null );
        }

        private ENode( XMLList n ) {
            this( null, null, n );
        }

        private ENode( Node n, ENode parent ) {
            this( n, parent, null );
        }

        private ENode( Node n, ENode parent, XMLList children ) {
            if( n != null &&
                children == null &&
                n.getNodeType() != Node.TEXT_NODE &&
                n.getNodeType() != Node.ATTRIBUTE_NODE ) 
                this.children = new XMLList();
            else if( children != null ) {
                this.children = children;
            }
            this.node = n;
            nodeSetup(parent);
        }

        // creates an empty node with a given parent and tag name
        private ENode( ENode parent, Object o ) {
            if( parent instanceof XMLList && ((XMLList)parent).get(0) != null ) {
                parent = ((XMLList)parent).get(0);
            }
            if(parent != null && parent.node != null)
                node = parent.node.getOwnerDocument().createElement(o.toString());
            this.children = new XMLList();
            this._dummy = true;
            nodeSetup(parent);
        }

        void nodeSetup(ENode parent) {
            this.parent = parent;
            if( this.parent != null ) {
                this.XML = this.parent.XML;
            }
            getNamespace();
            addNativeFunctions();
        }

        // finds and sets the qname and namespace for a node.
        void getNamespace() {
            this.inScopeNamespaces = new ArrayList<Namespace>();
            // get parent's namespaces
            if( this.parent != null ) {
                this.defaultNamespace = parent.defaultNamespace;
                this.inScopeNamespaces.addAll( this.parent.inScopeNamespaces );
            }
            // add default namespace, if it isn't boring
            if( this.defaultNamespace != null && !this.defaultNamespace.uri.equals( "" ) ) {
                this.inScopeNamespaces.add( this.defaultNamespace );
            }

            if( this.node == null ) 
                return;

            NamedNodeMap attr = this.node.getAttributes();
            Pattern xmlns = Pattern.compile("xmlns(\\:(\\w+))?");
            for( int i=0; attr != null && i< attr.getLength(); i++) {
                Matcher m = xmlns.matcher( attr.item(i).getNodeName() );
                if( m.matches() ) {
                    String nsName =  m.group(1) == null ? "" : m.group(2);
                    Namespace ns = new Namespace( nsName, attr.item(i).getNodeValue() );
                    this.addInScopeNamespace( ns );
                }
            }

            // get qualified name
            Pattern qname = Pattern.compile("(\\w+):(\\w+)");
            Matcher name = qname.matcher( this.node.getNodeName() );
            if( name.matches() ) {
                String prefix = name.group(1);
                this.name = new QName( new Namespace( prefix, this.getNamespaceURI( prefix ) ), name.group(2) );
            }
            else {
                this.name = new QName( defaultNamespace, this.node.getNodeName() );
            }
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
            // get attributes
            NamedNodeMap attr = parent.node.getAttributes();
            for( int i=0; attr != null && i< attr.getLength(); i++) {
                String nodeName = attr.item( i ).getNodeName();
                if( nodeName.equals( "xmlns" ) || nodeName.startsWith( "xmlns:") )
                    continue;
                parent.children.add( new ENode(attr.item(i), parent ) );
            }
            // get processing instructions
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
            // finally, traverse the children
            NodeList kids = parent.node.getChildNodes();
            for( int i=0; i<kids.getLength(); i++) {
                if( ( kids.item(i).getNodeType() == Node.COMMENT_NODE && parent.XML.ignoreComments ) ||
                    ( kids.item(i).getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && parent.XML.ignoreProcessingInstructions ) )
                    continue;
                ENode n = new ENode(kids.item(i), parent);
                buildENodeDom(n);
                parent.children.add(n);
            }
        }

        void init( String s ){
            try {
                // get rid of newlines and spaces if ignoreWhitespace is set (default)
                if( XML.ignoreWhitespace ) {
                    Pattern p = Pattern.compile("\\>\\s+\\<");
                    Matcher m = p.matcher(s);
                    s = m.replaceAll("><");
                }
                _document = XMLUtil.parse( s );
            }
            catch ( Exception e ){
                throw new RuntimeException( "can't parse : " + e );
            }
            node = _document.getDocumentElement();
            getNamespace();
            children = new XMLList();
            buildENodeDom(this);
        }

        Hashtable<String, ENodeFunction> nativeFuncs = new Hashtable<String, ENodeFunction>();

        public Object get( Object n ) {
            if ( n == null )
                return null;

            Pattern num = Pattern.compile("\\d+");
            Matcher m = num.matcher( n.toString() );
            if( m.matches() || n instanceof Number )
                return child( n );

            if ( n instanceof String || n instanceof JSString ){
                String s = n.toString();

                if( nativeFuncs.containsKey( s ) )
                    return nativeFuncs.get( s );

                if(s.equals("tojson")) return null;

                Object o = _nodeGet( this, s );
                //                ((JSObject)_prototype).set("__proto__", (new JSString.JSStringCons()).getPrototype());
                return (o == null && E4X.isXMLName(s)) ? new ENode( this, s ) : o;
            }

            if ( n instanceof Query ) {
		Query q = (Query)n;
                XMLList searchNode = ( this instanceof XMLList ) ? (XMLList)this : this.children;
		List<ENode> matching = new ArrayList<ENode>();
                for ( ENode theNode : searchNode ){
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
            if(this.children == null ) this.children = new XMLList();

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

            // if v is an XML list, add each element
            if( v instanceof XMLList ) {
                int index = this.children.size();
                for( ENode target : (XMLList)v ) {
                    if ( this.children.contains( target ) ) {
                        index = this.children.indexOf( target ) + 1;
                    }
                    else {
                        this.children.add( index, target );
                        index++;
                    }
                }
                return v;
            }
            // if v is already XML and it's not an XML attribute, just add v to this enode's children
            if( v instanceof ENode ) {
                if( k.toString().equals("*") ) {
                    this.children = new XMLList();
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

                int numChildren = this instanceof XMLList ? ((XMLList)this).size() : this.children.size();
                // this index is greater than the number of elements existing
                if( index >= numChildren ) {
                    // if there is a list of future siblings, get the last one
                    // if this isn't a fake node, we've gone one too far and we need to get its parent
                    ENode rep = this instanceof XMLList ? ((XMLList)this).get( ((XMLList)this).size() - 1 ) : ( n._dummy ? this : this.parent );

                    // if k/v doesn't really exist, "get" returns a dummy node, an emtpy node with nodeName = key
                    if( n._dummy ) {
                        n._dummy = false;
                    }
                    // otherwise, we need to reset n so we don't replace an existing node
                    else {
                        n = new ENode();
                        n.children = new XMLList();
                    }

                    ENode attachee = rep.parent;
                    n.node = rep.node.getOwnerDocument().createElement(rep.node.getNodeName());
                    Node content = rep.node.getOwnerDocument().createTextNode(v.toString());
                    n.children.add( new ENode( content, n ) );
                    n.parent = attachee;
                    // get the last sibling's position & insert this new one there
                    attachee.children.add( attachee.children.indexOf(rep)+1, n );
                }
                // replace an existing element
                else {
                    // FIXME!  why are we using this.node?!
                    // reset the child list
                    n.children = new XMLList();
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
                int index = this.children.size();

                if( n.node != null && n.node.getNodeType() != Node.ATTRIBUTE_NODE) {
                    index = this.children.indexOf( n );
                    this.children.remove( n );
                }
                // if there are a list of children, delete them all and replace with the new k/v
                else if ( n instanceof XMLList ) {
                    XMLList list = (XMLList)n;
                    for( int i=0; n != null && i < list.size(); i++) {
                        if( list.get(i).node.getNodeType() == Node.ATTRIBUTE_NODE ) 
                            continue;
                        // find the index of this node in the tree
                        index = this.children.indexOf( list.get(i) );
                        // remove it from the tree
                        this.children.remove( list.get(i) ) ;
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

            if( ! (n instanceof XMLList) )
                return n.parent.children.remove(n);

            for( ENode e : (XMLList)n ) {
                this.children.remove( e );
            }

            return true;
        }

        public class addNamespace extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                throw new RuntimeException("not yet implemented");
            }
        }

        private ENode appendChild(Node child, ENode parent) {
            if(parent.children == null)
                parent.children = new XMLList();

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
            XMLList nodeList = ( this instanceof XMLList ) ? (XMLList)this : this.children;
            Pattern num = Pattern.compile("\\d+(\\.\\d+)?");
            Matcher m = num.matcher(propertyName.toString());
            if( m.matches() ) {
                int i = Integer.parseInt(propertyName.toString());

                if( i < nodeList.size() )
                    return nodeList.get(i);
                else if ( nodeList.size() >= 1 ) 
                    return new ENode( this, this instanceof XMLList ? nodeList.get(0).name.localName : this.name.localName );
                else
                    return new ENode();
            }
            else {
                Object obj = this.get(propertyName);
                return ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
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
            if( parent == null || parent.node.getNodeType() == Node.ATTRIBUTE_NODE || this.node.getNodeType() == Node.ATTRIBUTE_NODE )
                return -1;

            XMLList sibs = parent.children();
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

        private XMLList children() {
            XMLList child = new XMLList();
            for( ENode n : this.children ) {
                if( n.node.getNodeType() != Node.ATTRIBUTE_NODE )
                    child.add( n );
            }
            return child;
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

        public XMLList comments() {
            XMLList comments = new XMLList();

            for( ENode child : this.children ) {
                if( child.node.getNodeType() == Node.COMMENT_NODE )
                    comments.add( child );
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
            return new XMLList(kids);
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

        private ArrayList<Namespace> getNamespaces( Object o ) {
            o = o == null ? "" : o;
            ArrayList<Namespace> list = new ArrayList<Namespace>();
            for( Namespace ns : this.inScopeNamespaces ) {
                if( o instanceof Namespace ) {
                    if( ns.equals( o ) )
                        list.add(ns);
                }
                else if( o instanceof String ) {
                    if( ns.prefix == null )
                        continue;

                    if( ns.prefix.equals(o.toString()) )
                        list.add(ns);
                    else if( ns.uri.equals( o.toString() ) )
                        list.add(ns);
                }
            }
            return list;
        }

        private String getNamespacePrefix( String uri ) {
            for( Namespace n : this.inScopeNamespaces ) {
                if( n.uri != null && n.uri.equals( uri ) ) 
                    return n.prefix;
            }
            return null;
        }

        private String getNamespaceURI( String name ) {
            for( Namespace n : this.inScopeNamespaces ) {
                if( n.prefix != null && n.prefix.equals( name ) ) 
                    return n.uri;
            }
            return null;
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
        private boolean hasSimpleContent() {
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

        public class hasSimpleContent extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode en = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return en.hasSimpleContent();
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
                return enode instanceof XMLList ? ((XMLList)enode).size() : ( enode.node != null ? 1 : enode.children.size() );
            }
        }

        private String localName() {
            return this.name.localName;
        }

        public class localName extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.localName();
            }
        }

        private QName name() {
            if( this.name != null && ( this.name.uri == null || this.name.uri.equals( "" )) && !this.defaultNamespace.uri.equals( "" ) )
                return new QName( defaultNamespace, this.name.localName );
            return this.name;
        }

        public class name extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return enode.name();
            }
        }

        private Namespace namespace() {
            return namespace( null );
        }

        private Namespace namespace( String prefix ) {
            if( prefix == null ) {
                return new Namespace( this.name.localName, this.name.uri );
            }
            ENode n = this;
            while( n != null ) {
                String uri = n.getNamespaceURI( prefix );
                if( uri != null ) 
                    return new Namespace( prefix, uri );
                n = n.parent;
            }
            return null;
        }

        public class namespace extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode enode = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                String prefix = (foo.length > 0) ? foo[0].toString() : null;
                return enode.namespace( prefix );
            }
        }


        private ArrayList<Namespace> getAncestors() {
            ArrayList<Namespace> ancestors = new ArrayList<Namespace>();
            ancestors.add( XML.defaultNamespace );
            ENode temp = this.parent;
            while( temp != null ) {
                for( Namespace ns : temp.inScopeNamespaces ) {
                    if( ! ns.containsPrefix( ancestors ) ) {
                        ancestors.add( ns );
                    }
                }
                temp = temp.parent;
            }
            return ancestors;
        }

        private JSArray namespaceDeclarations() {
            JSArray a = new JSArray();
            if( isSimpleTypeNode( this.node.getNodeType() ) )
                return a;

            ArrayList<Namespace> ancestors = this.getAncestors();

            for( Namespace ns : this.inScopeNamespaces ) {
                if( ! ns.containedIn( ancestors ) )
                    a.add( ns );
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

        public XMLList processingInstructions( String name ) {
            boolean all = name.equals( "*" );

            XMLList list = new XMLList();
            for( ENode n : this.children ) {
                if ( n.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && ( all || name.equals(n.name.localName) ) ) {
                    list.add( n );
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

        /** Set the name (uri:localName) of this.  
         * Behavior depends on node type.
         * <dl>
         * <dt>Text node</dt><dd>Fails</dd>
         * <dt>Comment node</dt>Fails</dd>
         * <dt>Processing instruction node</dt><dd>Local name can be set, but it cannot have a uri associated with its name</dd>
         * <dt>Attribute node</dt><dd>Its name will be set and the new namespace will be added to its parent</dd>
         * <dt>Element node</dt><dd>Succeeds</dd>
         * </dl>
         * @param name Either a string or QName representing the new name
         */
        private void setName( Object name ) {
            if( this.node == null ||
                this.node.getNodeType() == Node.TEXT_NODE ||
                this.node.getNodeType() == Node.COMMENT_NODE )
                return;

            QName n;
            if ( name instanceof QName && ((QName)name).uri.equals("") )
                name = ((QName)name).localName;

            n = new QName( XML.defaultNamespace, name );

            if( this.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE )
                n.uri = "";
            this.name = n;

            Namespace ns = n.uri == null ? XML.defaultNamespace : new Namespace( n.prefix, n.uri );
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
            Namespace ns2;
            if( ns instanceof Namespace )
                ns2 = (Namespace)ns;
            else
                ns2 = new Namespace( ns );

            this.name = new QName( ns2, this.name );
            if( this.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
                if (this.parent == null )
                    return;
                this.parent.addInScopeNamespace( ns2 );
            }
            if( this.node.getNodeType() == Node.ELEMENT_NODE ) {
                this.addInScopeNamespace( ns2 );
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

        private XMLList text() {
            XMLList list = new XMLList();
            if( this instanceof XMLList ) {
                for ( ENode n : (XMLList)this ) {
                    if( n.node.getNodeType() == Node.TEXT_NODE ) {
                        list.add( n );
                    }
                }
            }
            else if( this.node != null && this.node.getNodeType() == Node.TEXT_NODE ) {
                list.add( this );
            }            
            return list;
        }

        public class text extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                Object obj = s.getThis();
                ENode en = ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
                return en.text();
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

                append( singleNode, xml, 0, new ArrayList<Namespace>() );
            }
            // XMLList
            else {
                for( int i=0; i<this.children.size(); i++ ) {
                    append( this.children.get( i ), xml, 0, new ArrayList<Namespace>() );
                }
            }

            if( xml.length() > 0 && xml.charAt(xml.length() - 1) == '\n' ) {
                xml.deleteCharAt(xml.length()-1);
            }
            return xml.toString();
        }
        /*
        public String toString() {
            // if this is an empty top level element, return nothing
            if( this.node.getNodeType() == Node.ELEMENT_NODE && ( this.children == null || this.children.size() == 0 ) )
                return "";
            
            if( this.node.getNodeType() == Node.ATTRIBUTE_NODE || this.node.getNodeType() == Node.TEXT_NODE )
                return this.node.getNodeValue();
            
            if ( this.node.getNodeType() == Node.ELEMENT_NODE &&
                 this.children != null &&
                 this.childrenAreTextNodes() ) {
                StringBuilder xml = new StringBuilder();
                for( ENode n : this.children )
                    xml.append( n.node.getNodeValue() );
                return xml.toString();
            }

            return this.append( new StringBuilder(), new ArrayList<Namespace>(), 0 ).toString();
        }
        
        public StringBuilder append( StringBuilder buf, ArrayList<Namespace> ancestors, int level ) {
            if( XML.prettyPrinting )
                _level( this, buf, level );
            if( this.node.getNodeType() == Node.TEXT_NODE ) {
                if( XML.prettyPrinting ) {
                    return buf.append( escapeElementValue( this.node.getNodeValue().trim() ) );
                }
                else {
                    return buf.append( escapeElementValue( this.node.getNodeValue() ) );
                }
            }
            if( this.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
                return buf.append( escapeAttributeValue( this.node.getNodeValue() ) );
            }
            if( this.node.getNodeType() == Node.COMMENT_NODE ) {
                return buf.append( "<!--"+this.node.getNodeValue()+"-->" );
            }
            if( this.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ) {
                return buf.append( "<?"+this.node.getNodeName() + attributesToString( this , new ArrayList<Namespace>() )+"?>");
            }

            ArrayList<Namespace> nsDeclarations = new ArrayList<Namespace>();
            for( Namespace ns : this.inScopeNamespaces ) {
                if( !ns.containedIn( ancestors ) )
                    nsDeclarations.add( ns );
            }

            Namespace namespace = name.getNamespace();
            if( namespace.prefix == null ) {
                namespace.prefix = "";
                nsDeclarations.add( namespace );
            }

            buf.append( "<" );
            if( namespace.prefix != "" ) {
                buf.append( namespace.prefix + ":");
            }
            buf.append( name.localName );
            buf.append( attributesToString( this, ancestors ) );

            List<ENode> kids = printableChildren();
            if ( kids.size() == 0 ) {
                return buf.append( "/>" );
            }
            buf.append( ">" );

            boolean indentChildren = ( kids.size() > 1 ) || ( kids.size() == 1 && kids.get(0).node.getNodeType() != Node.TEXT_NODE );
            int nextIndentLevel = level;
            if( XML.prettyPrinting && indentChildren )
                nextIndentLevel = level + 1;
            else
                nextIndentLevel = 0;

            for ( ENode c : kids ) {
                if( XML.prettyPrinting && indentChildren )
                    buf.append( "\n" );

                ancestors.addAll( nsDeclarations );
                buf.append( c.append( new StringBuilder(), ancestors, nextIndentLevel ).toString() );
            }

            if( XML.prettyPrinting && indentChildren ) {
                buf.append( "\n" );
                _level( this, buf, level );
            }

            buf.append( "</" );
            if( namespace.prefix != "" ) {
                buf.append( namespace.prefix + ":");
            }
            buf.append( name.localName );
            return buf.append( ">" );
        }
        */

        public StringBuilder append( ENode n , StringBuilder buf , int level , ArrayList<Namespace> ancestors ){
            switch (n.node.getNodeType() ) {
            case Node.ATTRIBUTE_NODE:
                return _level( n, buf, level).append( n.XML.prettyPrinting ? n.node.getNodeValue().trim() : n.node.getNodeValue() );
            case Node.TEXT_NODE:
                return _level( n, buf, level).append( n.XML.prettyPrinting ? n.node.getNodeValue().trim() : n.node.getNodeValue() ).append("\n");
            case Node.COMMENT_NODE:
                return _level( n, buf, level).append( "<!--"+n.node.getNodeValue()+"-->" ).append("\n");
            case Node.PROCESSING_INSTRUCTION_NODE:
                return _level( n, buf, level).append( "<?"+n.node.getNodeName() + attributesToString( n , new ArrayList<Namespace>() )+"?>").append("\n");
            }

            _level( n, buf , level ).append( "<" );
            String prefix = n.getNamespacePrefix( n.name.uri );
            prefix = prefix != null && !prefix.equals( "" ) ? prefix + ":" : "";
            buf.append( prefix + n.name.localName ).append(attributesToString( n , ancestors ));

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
                return buf.append( n.XML.prettyPrinting ? kids.get(0).node.getNodeValue().trim() : kids.get(0).node.getNodeValue() ).append( "</" ).append( n.node.getNodeName() ).append( ">\n" );
            }

            for ( int i=0; i<kids.size(); i++ ){
                ENode c = kids.get(i);
                if( ( n.XML.ignoreComments && c.node.getNodeType() == Node.ATTRIBUTE_NODE ) ||
                    ( n.XML.ignoreComments && c.node.getNodeType() == Node.COMMENT_NODE ) ||
                    ( n.XML.ignoreProcessingInstructions && c.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ) )
                    continue;
                else {
                    // add to ancestors
                    append( c , buf , level + 1 , ancestors );
                    // delete from ancestors
                }
            }

            _level( n, buf , level ).append( "</" );
            return buf.append( prefix + n.name.localName ).append( ">\n" );
        }

        private StringBuilder _level( ENode n, StringBuilder buf , int level ){
            for ( int i=0; i<level; i++ ) {
                for( int j=0; j< n.XML.prettyIndent; j++) {
                    buf.append( " " );
                }
            }
            return buf;
        }

        private String attributesToString( ENode n , ArrayList<Namespace> ancestors ) {
            StringBuilder buf = new StringBuilder();
            for( Namespace ns : n.inScopeNamespaces ) {
                if( ancestors.contains( ns ) ) 
                    continue;

                if( ns.prefix == null || ns.prefix.equals( "" ) )
                    buf.append( " xmlns=\"" + ns.uri + "\"" );
                else 
                    buf.append( " xmlns:" + ns.prefix + "=\"" + ns.uri + "\"" );
            }
            ancestors.addAll( n.inScopeNamespaces );

            // get attrs
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
                if( c.node == null )
                    throw new RuntimeException("c.node is null: "+c.getClass());
                if( c.node.getNodeType() == Node.ATTRIBUTE_NODE ||
                    ( c.node.getNodeType() == Node.COMMENT_NODE && XML.ignoreComments ) ||
                    ( c.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && XML.ignoreProcessingInstructions ) )
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

        private void addInScopeNamespace( Namespace n ) {
            if ( this.node == null )
                return;
            short type = this.node.getNodeType();
            if( type == Node.COMMENT_NODE ||
                type == Node.PROCESSING_INSTRUCTION_NODE ||
                type == Node.TEXT_NODE ||
                type == Node.ATTRIBUTE_NODE )
                return;

            if( ( n.prefix == null || n.prefix.equals( "" ) ) && ( n.uri == null || n.uri.equals( "" ) ) )
                return;

            ArrayList<Namespace> match = this.getNamespaces( n.prefix );
            //            System.out.println("scope: "+inScopeNamespaces+" n: "+n+" match? "+match);
            if( match.size() > 0 ) {
                this.inScopeNamespaces.remove( match.get(0) );
                //                n.prefix += "-" + match.size();
                //                this.inScopeNamespaces.add( n );
            }

            this.inScopeNamespaces.add( n );
            if( n.prefix == null || n.prefix.equals( "" ) ) {
                n = new Namespace( n );
                n.createPrefix();
                addInScopeNamespace( n );
            }
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
                return getNode() == null ? "" : cnode.toString();
            }

            public Object get( Object n ) {
                return getNode().get( n );
            }

            public Object set( Object n, Object v ) {
                // there's this stupid thing where set is called for every xml node created
                if( n.equals("prototype") && v instanceof JSObjectBase)
                    return null;

                return getNode() == null ? null : cnode.set( n, v );
            }

            public Object removeField( Object f ) {
                return getNode().removeField(f);
            }

            public ENode getNode() {
                if( cnode != null) return cnode;
                cnode = (ENode)E4X._nodeGet(ENode.this, this.getClass().getSimpleName());
                return cnode;
            }

            ENode cnode;
        }

        public Collection<String> keySet( boolean includePrototype ) {
            XMLList list = ( this instanceof XMLList ) ? (XMLList)this : this.children;
            Collection<String> c = new ArrayList<String>();
            for( int i=0; i<list.size(); i++ ) {
                c.add( String.valueOf( i ) );
            }
            return c;
        }

        private Document _document;

        private XMLList children;
        private ENode parent;
        private Node node;

        private boolean _dummy;
        private ArrayList<Namespace> inScopeNamespaces = new ArrayList<Namespace>();
        private QName name;

        public boolean ignoreComments = true;
        public boolean ignoreProcessingInstructions = true;
        public boolean ignoreWhitespace = true;
        public boolean prettyPrinting = true;
        public int prettyIndent = 2;
        public Namespace defaultNamespace;
    }

    static class XMLList extends ENode implements List<ENode>, Iterable<ENode> {
        public List<ENode> children;
        public XMLList() {
            children = new LinkedList<ENode>();
        }

        public XMLList( ENode node ) {
            if( node.node  == null && node.children == null ) {
                children = new LinkedList<ENode>();
            }
            else if( node.node == null ) {
                children = node.children.subList(0, node.children.size());
            }
            else {
                children = new LinkedList<ENode>();
                children.add( node );
            }
        }

        public XMLList( List<ENode> list ) {
            children = list;
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

        public String toString() {
            StringBuilder xml = new StringBuilder();
            if( children.size() == 1 ) {
                return children.get(0).toString();
            }
            for( ENode n : children ) {
                //xml.append( n.toString() );
                append( n, xml, 0, new ArrayList<Namespace>() );
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

        public boolean add( ENode n ) { return children.add(n); }
        public void add( int index, ENode n) { children.add( index, n); }
        public boolean addAll( Collection<? extends E4X.ENode> list ) { return children.addAll( list ); }
        public boolean addAll( int index, Collection<? extends E4X.ENode> list ) { return children.addAll( index, list ); }
        public void clear() {  children.clear(); }
        public boolean contains( Object o ) { return  children.contains( o ); }
        public boolean containsAll( Collection o ) { return  children.containsAll( o ); }
        public boolean equals( Object o) { return children.equals(o); }
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

    static Object _nodeGet( ENode start , String s ){
        if( start instanceof XMLList )
            return _nodeGet( (XMLList)start, s );
        return _nodeGet( new XMLList( start ), s );
    }

    static Object _nodeGet( XMLList start , String s ){
        final boolean qualified = s.contains( "::" );
        String uri = "";
        if( qualified ) {
            uri = s.substring( 0, s.indexOf("::") );
            s = s.substring( s.indexOf( "::" ) + 2 );
        }

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

                XMLList kids = n.children;
                if ( kids == null || kids.size() == 0 )
                    continue;

                for ( int i=0; i<kids.size(); i++ ){
                    ENode c = kids.get(i);
                    if ( ! attr && c.node.getNodeType() != Node.ATTRIBUTE_NODE && 
                         ( all ||
                           ( qualified && c.name.uri.equals( uri ) && c.name.localName.equals( s ) ) ||
                           ( ! qualified && c.name.localName != null && c.name.localName.equals( s ) )
                           ) ) {
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
        return new XMLList(lst);
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

    static class QName extends JSObjectBase {
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
            this.uri = namespace == null ? null : namespace.uri;
        }

        public String toString() {
            String s = this.uri == null ? "*::" : ( this.uri.equals("") ? "" : this.uri + "::" );
            return s + this.localName;
        }

        public Namespace getNamespace( ) {
            if( this.uri == null )
                return null;

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

    static class Namespace extends JSObjectBase {

        void init( String s ) {
            this.uri = s;
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

        private boolean containedIn( ArrayList<Namespace> list ) {
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

        private boolean containsPrefix( ArrayList<Namespace> list ) {
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


        public void createPrefix() {
            String prefix = this.uri;
            while( prefix.endsWith("/") || prefix.endsWith(".xml") ) {
                if ( prefix.endsWith( "/" ) )
                    prefix.substring( 0, prefix.length() - 1 );
                if ( prefix.endsWith( ".xml" ) )
                    prefix.substring( 0, prefix.length() - 4 );
            }
            prefix = prefix.substring( prefix.lastIndexOf("/") + 1 );
            prefix = prefix.substring( prefix.lastIndexOf(".") + 1 );
            this.prefix = prefix;
        }
    }

    public static String escapeElementValue( String s ) {
        s = s.replaceAll( "<", "&lt;" );
        s = s.replaceAll( ">", "&gt;" );
        s = s.replaceAll( "&", "&amp;" );
        return s;
    }

    public static String escapeAttributeValue( String s ) {
        s = s.replaceAll( "\"", "&quot;" );
        s = s.replaceAll( ">", "&gt;" );
        s = s.replaceAll( "&", "&amp;" );

        s = s.replaceAll( "\\u000A", "&#xA;" );
        s = s.replaceAll( "\\u000D", "&#xD;" );
        s = s.replaceAll( "\u0009", "&#x9;" );
        return s;
    }

    public static XMLList addNodes(ENode a, ENode b) {
        if( a instanceof XMLList && b instanceof XMLList) {
            ((XMLList)a).addAll(b);
            return (XMLList)a;
        }
        else if ( a instanceof XMLList ) {
            ((XMLList)a).add(b);
            return (XMLList)a;
        }
        else if ( b instanceof XMLList ) {
            ((XMLList)b).add(0, a);
            return (XMLList)b;
        }
        else {
            XMLList list = new XMLList();
            list.add( a );
            list.add( b );
            return list;
        }
    }

}
