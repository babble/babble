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
            addNativeFunctions();
        }

        private ENode( Node n ) {
            children = new LinkedList<ENode>();
            node = n;
            addNativeFunctions();
        }

        private ENode( Node n, ENode parent ) {
            this.children = new LinkedList<ENode>();
            this.node = n;
            this.parent = parent;
            addNativeFunctions();
        }

        private ENode( ENode parent, Object o ) {
            if(parent != null && parent.node != null)
                node = parent.node.getOwnerDocument().createElement(o.toString());
            this.children = new LinkedList<ENode>();
            this.parent = parent;
            this._dummy = true;
            addNativeFunctions();
        }

        private ENode( List<ENode> n ) {
            children = n;
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

        Hashtable<String, ENodeFunction> nativeFuncs = new Hashtable<String, ENodeFunction>();
        //not being used...
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

                if( nativeFuncs.containsKey(s) )
                    return nativeFuncs.get( s );

                if(s.equals("tojson")) return null;

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
                if( n.children.size() < index ) { // n == null ) {
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

        private boolean appendChild(Node child, ENode parent) {
            if(parent.children == null)
                parent.children = new LinkedList<ENode>();

            ENode echild = new ENode(child, parent);
            buildENodeDom(echild);
            return parent.children.add(echild);
        }

        private boolean appendChild(ENode child) {
            return appendChild(child.node, this);
        }

        public class appendChild extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                ENode parent = (ENode)s.getThis();

                if(foo.length < 1)
                    return parent;
                ENode child = (ENode)foo[0];

                return parent.appendChild(child);
            }
        }

        public class attribute extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                if(foo.length == 0)
                    return null;
                return ((ENode)s.getThis()).get("@"+foo[0]);
            }
        }
        public class attributes extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                return ((ENode)s.getThis()).get("@*");
            }
        }


        private ENode child(Object propertyName) {
            Pattern num = Pattern.compile("-?\\d+(\\.\\d+)?");
            Matcher m = num.matcher(propertyName.toString());
            if( m.matches() ) {
                int i = Integer.parseInt(propertyName.toString());
                if(i >= 0 ) {
                    if( i < this.children.size() )
                        return (ENode)this.children.get(i);
                    else {
                        ENode nud = (ENode)this.get(propertyName.toString());
                        System.out.println("here: "+propertyName+" "+nud);
                        return nud;
                    }
                }
            }
            else {
                return (ENode)this.get(propertyName);
            }
            return null;
        }

        public class child extends ENodeFunction {
            public Object call( Scope s,  Object foo[]) {
                if( foo.length == 0 )
                    return null;
                return ((ENode)s.getThis()).child( foo[0].toString() );
            }
        }

        public class childIndex extends ENodeFunction {
            public Object call (Scope s, Object foo[] ) {
                Node parent = ((ENode)s.getThis()).parent.node;
                if( parent == null || parent.getNodeType() == Node.ATTRIBUTE_NODE ) return -1;

                NodeList sibs = parent.getChildNodes();
                for( int i=0; i<sibs.getLength(); i++ ) {
                    if(sibs.item(i).isEqualNode(((ENode)s.getThis()).node)) return i;
                }

                return -1;
            }
        }

        public class children extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                return (ENode)((ENode)s.getThis()).get( "*" );
            }
        }

        public class comments extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                ENode t = (ENode)s.getThis();
                NodeList kids = t.node.getChildNodes();
                List<ENode> comments = new LinkedList<ENode>();

                for( int i=0; i<kids.getLength(); i++ ) {
                    if(kids.item(i).getNodeType() == Node.COMMENT_NODE)
                        comments.add(new ENode(kids.item(i)));
                }

                return new ENode(comments);
            }
        }

        // this is to spec, but doesn't seem right... it's "equals", not "contains"
        public class contains extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                ENode o = (ENode)foo[0];
                if( !(o instanceof ENode) )
                    return false;
                return ((ENode)s.getThis()).node.isEqualNode(o.node);
            }
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
                ENode n = (ENode)s.getThis();
                String name;
                if( foo.length == 0)
                    name = "*";
                else
                    name = foo[0].toString();

                return n.descendants(name);
            }
        }

        public class elements extends ENodeFunction {
            public Object call( Scope s, Object foo[] ) {
                ENode en = (ENode)s.getThis();
                String name;
                if(foo.length == 0)
                    name = "*";
                else
                    name = foo[0].toString();

                if( en.children == null || en.children.size() == 0)
                    return null;

                if(name == null || name == "") {
                    name = "*";
                }

                ENode list = new ENode();
                for( ENode n : en.children ) {
                    if( n.node != null && n.node.getNodeType() == Node.ELEMENT_NODE && (name.equals( "*" ) || n.node.getNodeName().equals(name)) )
                    list.children.add( n );
                }

                return list;
            }
        }

        public class hasOwnProperty extends ENodeFunction {
            public Object call(Scope s, Object foo[] ) {
                ENode en = (ENode)s.getThis();
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
                ENode en = (ENode)s.getThis();
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
                ENode en = (ENode)s.getThis();
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

        public class inScopeNamespaces extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                throw new RuntimeException("inScopeNamespaces not yet implemented");
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
                if(foo.length == 0 )
                    return ((ENode)s.getThis()).insertChildAfter((Object)null, (ENode)null);
                if(foo.length == 1 )
                    return ((ENode)s.getThis()).insertChildAfter((Object)foo[0], (ENode)null);
                return ((ENode)s.getThis()).insertChildAfter((Object)foo[0], (ENode)foo[1]);
            }
        }

        public class insertChildBefore extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                if(foo.length == 0 )
                    return ((ENode)s.getThis()).insertChildBefore((Object)null, (ENode)null);
                if(foo.length == 1 )
                    return ((ENode)s.getThis()).insertChildBefore((Object)foo[0], (ENode)null);
                return ((ENode)s.getThis()).insertChildBefore((Object)foo[0], (ENode)foo[1]);
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
                ENode n = (ENode)s.getThis();
                if ( n.node != null )
                    return 1;
                return children.size();
            }
        }

        public class localName extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode n = (ENode)s.getThis();
                if(n.node == null) return "";
                return n.node.getLocalName();
            }
        }

        public class name extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode n = (ENode)s.getThis();
                if(n.node == null) return "";
                return n.node.getNodeName();
            }
        }

        public class namespace extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode n = (ENode)s.getThis();
                if(foo.length == 0) return null;

                String prefix = foo[0].toString();
                if( prefix == null )
                    return n.node.getNamespaceURI();

                return n.node.lookupNamespaceURI( prefix );
            }
        }

        public class namespaceDeclarations extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode en = (ENode)s.getThis();
                JSArray a = new JSArray();
                if( isSimpleTypeNode( en.node.getNodeType() ) )
                    return a;

                Node y = en.node.getParentNode();
                throw new RuntimeException("namespaceDeclarations not yet implemented");
            }
        }

        public class nodeKind extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode n = (ENode)s.getThis();
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
                return ((ENode)s.getThis()).normalize();
            }
        }

        public class parent extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                return ((ENode)s.getThis()).parent;
            }
        }

        public class processingInstructions extends ENodeFunction {
            public Object call(Scope s, Object foo[] ) {
                ENode en = (ENode)s.getThis();

                String name;
                if(foo.length == 0 )
                    name = "*";
                else
                    name = foo[0].toString();
                boolean all = name.equals( "*" );

                ENode list = new ENode();
                for( ENode n : en.children ) {
                    if ( n.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && ( all || name.equals(n.node.getLocalName()) ) ) {
                        list.children.add( n );
                    }
                }
                return list;
            }
        }

        /** Inserts the given child into this object prior to the existing XML properties.
         */
        public class prependChild extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                return _insertChild( (Object)null, (ENode)foo[0], 0 );
            }
        }

        /**
         * So, the spec says that this should only return toString(prop) == "0".  However, the Rhino implementation returns true
         * whenever prop is a valid index, so I'm going with that.
         */
        public class propertyIsEnumerable extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                String prop = foo[0].toString();
                ENode n = (ENode)((ENode)s.getThis()).get(prop);
                if(n == null) return false;

                Pattern num = Pattern.compile("\\d+");
                Matcher m = num.matcher(prop);
                if( m.matches() )
                    return true;

                return false;
            }
        }

        public class removeNamespace extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                throw new RuntimeException("not yet implemented");
            }
        }

        public class replace extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                String name = foo[0].toString();
                Object value = foo[1];
                ENode en = (ENode)s.getThis();
                ENode exists = (ENode)en.get(name);
                if( exists == null )
                    return this;

                return en.set(name, value);
            }
        }

        /** not right */
        public class setChildren extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode en = (ENode)s.getThis();
                Object value = foo[0];
                if ( en.node != null ) {
                    return en.set( en.node.getNodeName(), value );
                }
                for( ENode n : en.children ) {
                    en.set( n.node.getNodeName(), value );
                }
                return this;
            }
        }

        /** FIXME: implement QName
         */
        public class setLocalName extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode n = (ENode)s.getThis();
                String name = foo[0].toString();
                if( n.node == null ||
                    n.node.getNodeType() == Node.TEXT_NODE ||
                    n.node.getNodeType() == Node.COMMENT_NODE )
                    return null;
                return null;
            }
        }

        /** FIXME: implement QName
         */
        public class setName extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode n = (ENode)s.getThis();
                String name = foo[0].toString();
                if( n.node == null ||
                    n.node.getNodeType() == Node.TEXT_NODE ||
                    n.node.getNodeType() == Node.COMMENT_NODE )
                    return null;
                return null;
            }
        }

        public class setNamespace extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode n = (ENode)s.getThis();
                String namespace = foo[0].toString();
                throw new RuntimeException("not yet implemented");
            }
        }

        public class text extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode en = (ENode)s.getThis();
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
            if( this.node != null ) {
                if( this.node.getNodeType() == Node.ATTRIBUTE_NODE || this.node.getNodeType() == Node.TEXT_NODE )
                    return this.node.getNodeValue();

                if ( this.node.getNodeType() == Node.ELEMENT_NODE &&
                     this.children != null &&
                     this.children.size() == 1 &&
                     this.children.get(0).node.getNodeType() == Node.TEXT_NODE ){
                    return this.children.get(0).node.getNodeValue();
                }

                append( this, xml, 0);
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

        private StringBuilder _level( StringBuilder buf , int level ){
            for ( int i=0; i<level; i++ )
                buf.append( "  " );
            return buf;
        }

        public class toString extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                return s.getThis().toString();
            }
        }

        /** too painful to do right now */
        public class toXMLString extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                ENode en = (ENode)s.getThis();
                throw new RuntimeException("not yet implemented");
            }
        }

        public class valueOf extends ENodeFunction {
            public Object call(Scope s, Object foo[]) {
                return s.getThis();
            }
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
                if( cnode == null ) {
                    // there's this stupid thing where set is called for every xml node created
                    if( ENode.this.toString() == null && n.equals("prototype") ) {
                        return null;
                    }
                    cnode = (ENode)E4X._nodeGet(ENode.this, this.getClass().getSimpleName());
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

