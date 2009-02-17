// ENode.java

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

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.util.*;

public class ENode extends JSObjectBase {

    public static JSFunction _cons = new Cons();

    public static class Cons extends JSFunctionCalls0 {

        public JSObject newOne(){
            ENode t = new ENode( this, defaultNamespace );
            t._new = true;
            return t;
        }

        public Object call( Scope scope , Object [] args){
            Object blah = scope.getThis();

            ENode e;
            if ( blah != null && blah instanceof ENode && ((ENode)blah)._new ) {
                e = (ENode)blah;
                e._new = false;
            }
            else if( args.length > 0 && args[0] != null && args[0] instanceof ENode ) {
                e = (ENode)args[0];
                return e;
            }
            else {
                e = new ENode( this, defaultNamespace );
            }

            if( args.length > 0 && args[0] != null ) {
                if( args[0] instanceof ENode && ((ENode)args[0])._new ) {
                    e = (ENode)args[0];
                    e._new = false;
                }
                else {
                    e.init( args[0].toString() );
                }
            }
            else if( args.length == 0 ) {
                e.init( "" );
            }

            return e;
        }

        protected void init() {
            
            _prototype.set( "addNamespace" , new ENodeFunction() {
                    public Object call( Scope s, Object foo[] ) {
                        return getENode( s ).addNamespace( getOneArg( foo ) );
                    }
                });
            _prototype.set( "appendChild", new ENodeFunction() {
                    public Object call( Scope s, Object foo[] ) {
                        ENode parent = getENode( s );
                        ENode child = parent.toXML( getOneArg( foo ) );
                        return child == null ? parent : parent.appendChild(child);
                    }
                });
            _prototype.set( "attribute", new ENodeFunction() {
                    public Object call( Scope s, Object foo[] ) {
                        Object arg = getOneArg( foo );
                        if( arg == null )
                            throw new JSException( "\"attribute\" cannot take null as an argument." );
                        return getENode( s ).attribute( arg.toString() );
                    }
                });
            _prototype.set( "attributes", new ENodeFunction() {
                    public Object call( Scope s, Object foo[] ) {
                        return getENode( s ).attributes();
                    }
                });
            _prototype.set( "child", new ENodeFunction() {
                    public Object call( Scope s,  Object foo[]) {
                        return getENode( s ).child( getOneArg( foo ).toString() );
                    }
                });
            _prototype.set( "childIndex", new ENodeFunction() {
                    public Object call (Scope s, Object foo[] ) {
                        return getENode( s ).childIndex();
                    }
                });

            _prototype.set( "children", new ENodeFunction() {
                    public Object call( Scope s, Object foo[] ) {
                        return getENode( s ).children();
                    }
                });

            _prototype.set( "comments", new ENodeFunction() {
                    public Object call( Scope s, Object foo[] ) {
                        return getENode( s ).comments();
                    }
                });

            _prototype.set( "contains", new ENodeFunction() {
                    public Object call( Scope s, Object foo[] ) {
                        return getENode( s ).contains( getOneArg( foo ) );
                    }
                });
            _prototype.set( "copy", new ENodeFunction() {
                    public Object call( Scope s, Object foo[] ) {
                        return getENode( s ).copy();
                    }
                });
            _prototype.set( "descendants", new ENodeFunction() {
                    public Object call( Scope s, Object foo[] ) {
                        String name = ( foo.length == 0) ? "*" : foo[0].toString();
                        return getENode( s ).descendants( name );
                    }
                });
            _prototype.set( "elements", new ENodeFunction() {
                    public Object call( Scope s, Object foo[] ) {
                        String name = (foo.length == 0) ? "*" : foo[0].toString();
                        return getENode( s ).elements( name );
                    }
                });
            _prototype.set( "hasOwnProperty", new ENodeFunction() {
                    public Object call(Scope s, Object foo[] ) {
                        return getENode( s ).hasOwnProperty( getOneArg( foo ).toString() );
                    }
                });

            _prototype.set( "hasComplexContent", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s ).hasComplexContent();
                    }
                });
            _prototype.set( "hasSimpleContent", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s ).hasSimpleContent();
                    }
                });
            _prototype.set( "inScopeNamespaces", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return new JSArray( getENode( s ).inScopeNamespaces().toArray() );
                    }
                });
            _prototype.set( "insertChildAfter", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        foo = getTwoArgs( foo );
                        return getENode( s ).insertChildAfter( foo[0], foo[1] );
                    }
                });

            _prototype.set( "insertChildBefore", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        foo = getTwoArgs( foo );
                        return getENode( s ).insertChildBefore( foo[0], foo[1] );
                    }
                });
            _prototype.set( "length", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        ENode enode = getENode( s );
                        return enode.isDummy() ? 0 : ( enode instanceof XMLList ) ? ((XMLList)enode).length() : 1;
                    }
                });
            _prototype.set( "localName", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s ).localName();
                    }
                });
            _prototype.set( "name", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s ).name();
                    }
                });
            _prototype.set( "namespace", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        String prefix = (foo.length > 0) ? foo[0].toString() : null;
                        return getENode( s ).namespace( prefix );
                    }
                });
            _prototype.set( "namespaceDeclarations", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        ArrayList<Namespace> a = getENode( s ).namespaceDeclarations();
                        JSArray decs = new JSArray();
                        for( Namespace ns : a ) {
                            decs.add( ns );
                        }
                        return decs;
                    }
                });
            _prototype.set( "nodeKind", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return new JSString( getENode( s ).nodeKind() );
                    }
                });
            _prototype.set( "normalize", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s ).normalize();
                    }
                });
            _prototype.set( "parent", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s ).parent();
                    }
                });
            _prototype.set( "prependChild", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s ).prependChild( getOneArg( foo ) );
                    }
                });
            _prototype.set( "processingInstructions", new ENodeFunction() {
                    public Object call(Scope s, Object foo[] ) {
                        String name = (foo.length == 0 ) ? "*" : foo[0].toString();
                        return getENode( s ).processingInstructions( name );
                    }
                });
            _prototype.set( "propertyIsEnumerable", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        if( foo.length == 0 )
                            return getENode( s ).propertyIsEnumerable();
                        return getENode( s ).propertyIsEnumerable( getOneArg( foo ) );
                    }
                });
            _prototype.set( "removeNamespace", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s ).removeNamespace( getOneArg( foo ) );
                    }
                });
            _prototype.set( "replace", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        foo = getTwoArgs( foo );
                        return getENode( s ).replace( foo[0].toString() , foo[1] );
                    }
                });
            _prototype.set( "setChildren", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s ).setChildren( getOneArg( foo ) );
                    }
                });
            _prototype.set( "setLocalName", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        getENode( s ).setLocalName( getOneArg( foo ) );
                        return null;
                    }
                });
            _prototype.set( "setName", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        getENode( s ).setName( getOneArg( foo ).toString());
                        return null;
                    }
                });
            _prototype.set( "setNamespace", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        getENode( s ).setNamespace( getOneArg( foo ) );
                        return null;
                    }
                });
            _prototype.set( "text", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s ).text();
                    }
                });
            _prototype.set( "toString", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return new JSString( getENode( s ).toString() );
                    }
                });
            _prototype.set( "toXMLString", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return new JSString( getENode( s ).toXMLString() );
                    }
                });
            _prototype.set( "valueOf", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        return getENode( s );
                    }
                });
            _prototype.dontEnumExisting();
        }

        public JSObject settings() {
            JSObjectBase sets = new JSObjectBase();
            sets.set( ignoreCommentsStr, ignoreComments);
            sets.set( ignoreProcessingInstructionsStr, ignoreProcessingInstructions);
            sets.set( ignoreWhitespaceStr, ignoreWhitespace);
            sets.set( prettyPrintingStr, prettyPrinting);
            sets.set( prettyIndentStr, prettyIndent);
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

            Object setting = settings.get( ignoreCommentsStr );
            if(setting != null && setting instanceof Boolean)
                ignoreComments = ((Boolean)setting).booleanValue();
            setting = settings.get( ignoreProcessingInstructionsStr );
            if(setting != null && setting instanceof Boolean)
                ignoreProcessingInstructions = ((Boolean)setting).booleanValue();
            setting = settings.get(ignoreWhitespaceStr );
            if(setting != null && setting instanceof Boolean)
                ignoreWhitespace = ((Boolean)setting).booleanValue();
            setting = settings.get( prettyPrintingStr );
            if(setting != null && setting instanceof Boolean)
                prettyPrinting = ((Boolean)setting).booleanValue();
            setting = settings.get( prettyIndentStr );
            if(setting != null && setting instanceof Integer)
                prettyIndent = ((Integer)setting).intValue();
        }

        public JSObject defaultSettings() {
            JSObjectBase sets = new JSObjectBase();
            sets.set( ignoreCommentsStr, true);
            sets.set( ignoreProcessingInstructionsStr, true);
            sets.set( ignoreWhitespaceStr, true);
            sets.set( prettyPrintingStr, true);
            sets.set( prettyIndentStr, 2 );
            return sets;
        }

        private final String ignoreCommentsStr = "ignoreComments";
        private final String ignoreProcessingInstructionsStr = "ignoreProcessingInstructions";
        private final String ignoreWhitespaceStr = "ignoreWhitespace";
        private final String prettyPrintingStr = "prettyPrinting";
        private final String prettyIndentStr = "prettyIndent";

        public boolean ignoreComments = true;
        public boolean ignoreProcessingInstructions = true;
        public boolean ignoreWhitespace = true;
        public boolean prettyPrinting = true;
        public long prettyIndent = 2;

        public Namespace defaultNamespace = new Namespace();
        
        public Object get( Object n ) {
            String s = n.toString();
            
            if ( s.equals( "HTML" ) ){
                if ( ! containsKey( "HTML" ) )
                    super.set( "HTML" , new HtmlCons() );
                return super.get( "HTML" );
            }

            if( s.equals( ignoreCommentsStr ) )
                return ignoreComments;
            if( s.equals( ignoreProcessingInstructionsStr ) )
                return ignoreProcessingInstructions;
            if( s.equals( ignoreWhitespaceStr ) )
                return ignoreWhitespace;
            if( s.equals( prettyPrintingStr ) )
                return prettyPrinting;
            if( s.equals( prettyIndentStr ) )
                return prettyIndent;
            if( s.equals( "prototype" ) ) 
                return this.getPrototype();
            
            return super.get( n );
        }
        
        public Object set( Object k, Object v ) {
            String s = k.toString();
            String val = v.toString();
            if( s.equals( ignoreCommentsStr ) )
                ignoreComments = Boolean.parseBoolean(val);
            if( s.equals( ignoreProcessingInstructionsStr ) ) 
                ignoreProcessingInstructions = Boolean.parseBoolean(val);
            if( s.equals( ignoreWhitespaceStr ) )
                ignoreWhitespace = Boolean.parseBoolean(val);
            if( s.equals( prettyPrintingStr ) )
                prettyPrinting = Boolean.parseBoolean(val);
            if( s.equals( prettyIndentStr ) )
                prettyIndent = Long.parseLong(val);
            return super.set( k , v );
        }
        
        public Namespace getDefaultNamespace() {
            return defaultNamespace;
        }

        public Namespace setAndGetDefaultNamespace(Object o) {
            defaultNamespace = new Namespace( o );
            return defaultNamespace;
        }
    }

    public static class HtmlCons extends Cons {
        public JSObject newOne(){
            ENode t = new ENode( this, defaultNamespace );
            t._new = true;
            t._html = true;
            return t;
        }
    }

    private Cons XML;

    public ENode(){
        super( _getCons() );
        XML = (Cons)ENode._cons;
        nodeSetup( null );
    }

    // for XMLList, to set constructor
    public ENode( JSFunction f ) {
        super( f );
    }

    private ENode( Cons c, Namespace ns ) {
        super( _getCons() );
        XML = c;
        defaultNamespace = ns;
        nodeSetup( null );
    }

    private ENode( Node n, ENode parent, XMLList children ) {
        super( _getCons() );
        if( n != null &&
            children == null &&
            n.getNodeType() != Node.TEXT_NODE &&
            n.getNodeType() != Node.ATTRIBUTE_NODE ) 
            this.children = new XMLList();
        else if( children != null ) {
            this.children = children;
        }
        this.node = n;
        _html = parent._html;
        nodeSetup(parent);
    }

    // creates a copy of an existing ENode
    private ENode( ENode n ) {
        super( _getCons() );
        this.XML = n.XML;
        this.name = n.name;
        _html = n._html;

        if( n.node != null ) {
            this.node = n.node.cloneNode( false );
        }

        this.inScopeNamespaces = (ArrayList<Namespace>)n.inScopeNamespaces.clone();
        if( n.children != null ) {
            this.children = new XMLList();
            for( ENode child : n.children ) {
                ENode temp = child.copy();
                this.children.add( temp );
            }
        }
    }

    /** Creates an empty node with a given parent and tag name.
     * This is to create "fake" nodes that are not attached to a parent.
     * @example 
     * xml = &lt;x/&gt;
     * xml.foo.bar;        // legal, so a "fake" node must be created 
     *                     // for foo so that bar doesn't throw an exception
     * xml.foo.bar = "hi"; // now the set method attaches the "fake" nodes to the parent
     */
    protected ENode( ENode parent, Object o ) {
        super( _getCons() );
        if( parent instanceof XMLList && 
            ((XMLList)parent).size() > 0 && 
            ((XMLList)parent).get(0) != null ) {
            parent = ((XMLList)parent).get(0);
        }
        if(parent != null && parent.node != null) {
            if( o instanceof QName ) {
                QName q = (QName)o;
                node = document.createElementNS( q.uri.toString(), q.localName.toString() );
            }
            else {
                node = document.createElement( o.toString() );
            }
        }
        this.children = new XMLList();
        this._dummy = true;
        nodeSetup(parent);
    }

    public static ENode attributeNodeFactory( ENode parent, Object nodeName ) {
        ENode enode = new ENode();
        enode._dummy = true;
        if( parent instanceof XMLList ) {
            parent = (ENode)parent.get( 0 );
        }
        enode.parent = parent;
        if( nodeName instanceof QName ) {
            QName q = (QName)nodeName;
            q.localName = new JSString( q.localName.toString().substring( q.localName.toString().indexOf( "@" ) + 1 ) );
            if( q.uri == null ) {
                enode.node = document.createAttribute( q.localName.toString() );
            }
            else {
                enode.node = document.createAttributeNS( q.uri.toString() , q.localName.toString() );
            }
            enode.addInScopeNamespace( new Namespace( q ) );
            enode.setName( q );
            enode.setLocalName( q.localName );
        }
        else {
            String s = nodeName.toString();
            s = s.substring( s.indexOf( "@" ) + 1 );
            enode.node = document.createAttribute( s );
            enode.setLocalName( s );
        }
        return enode;
    }

    /** Sets this node's parent, points it to the XML constructor, gets
     * the namespace, attributes, and initializes functions for it.
     */
    void nodeSetup( ENode parent ) {
        this.parent = parent;
        this.XML = this.parent == null ? 
            (Cons)this._getCons() : 
            this.parent.XML;

        getNamespace();
        if( this.node != null && 
            this.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
            // parse the name if it is qualified and find the uri
            String nodeName = this.node.getNodeName();
            if( nodeName.indexOf( ":" ) > 0 ) {
                String uri = getNamespaceURI( nodeName.substring( 0, nodeName.indexOf( ":" ) ) );
                this.setName( new QName( new Namespace( nodeName.substring( 0, nodeName.indexOf( ":" ) ), uri ), 
                                         nodeName.substring( nodeName.indexOf( ":" ) + 1 ) ) );
                this.setLocalName( nodeName.substring( nodeName.indexOf( ":" ) + 1 ) );
            }
            // make sure that there are no duplicate attributes
            ((Attr)this.node).setValue( E4X.escapeAttributeValue( ((Attr)this.node).getValue() ) );
            for( ENode n : parent.children ) {
                if( n.node.getNodeType() == Node.ATTRIBUTE_NODE &&
                    n.name().toString().equals( this.name().toString() ) ) {
                    if ( _html ){
                        // allowed in HTML
                    }
                    else {
                        throw new JSException( "TypeError: duplicate XML attribute "+this.name() );
                    }
                }
            }
        }
        else {
            addAttributes();
        }
    }

    /** Get attributes */
    void addAttributes() {
        if( this.node == null || isSimpleTypeNode() )
            return;

        if( this.children == null ) {
            this.children = new XMLList();
        }

        NamedNodeMap attr = this.node.getAttributes();
        for( int i=0; attr != null && i< attr.getLength(); i++) {
            String nodeName = attr.item( i ).getNodeName();
            if( nodeName.equals( "xmlns" ) || nodeName.startsWith( "xmlns:") )
                continue;
            this.children.add( new ENode(attr.item(i), this , null ) );
        }
    }

    public static JSFunction _getCons() {
        return Scope.getThreadLocalFunction( "XML" , _cons );
    }

    /** finds and sets the qname and namespace for a node.
     */
    void getNamespace() {
        this.inScopeNamespaces = new ArrayList<Namespace>();

        if( this.node == null ) {
            return;
        }
        else if( this.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ) {
            this.name = new QName( new Namespace(), this.node.getNodeName() );
            return;
        }
        else if( this.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
            String temp = this.node.getNodeName();
            String ns = this.node.getNamespaceURI();
            if( ns != null ) {
                this.setName( ns );
                this.setLocalName( temp );
            }
            else {
                this.name = new QName( XML.defaultNamespace, this.node.getNodeName() );
            }
            return;
        }

        boolean seenDefaultNamespace = false;
        NamedNodeMap attr = this.node.getAttributes();
        Pattern xmlns = Pattern.compile("xmlns(\\:(\\w+))?");
        for( int i=0; attr != null && i< attr.getLength(); i++) {
            Matcher m = xmlns.matcher( attr.item(i).getNodeName() );
            if( m.matches() ) {
                String nsName =  m.group(1) == null ? "" : m.group(2);
                if( m.group(1) == null ) {
                    seenDefaultNamespace = true;
                }
                Namespace ns = new Namespace( nsName, attr.item(i).getNodeValue() );
                this.addInScopeNamespace( ns );
            }
        }
        if( !seenDefaultNamespace ) {
            this.addInScopeNamespace( XML.defaultNamespace );
            this.defaultNamespace = XML.defaultNamespace;
        }

        // get qualified name
        Pattern qname = Pattern.compile("((\\w+):)?(\\w+)");
        Matcher name = qname.matcher( this.node.getNodeName() );
        if( name.matches() ) {
            String prefix = "";
            if( name.group(1) != null ) {
                prefix = name.group(2);
                this.name = new QName( new Namespace( prefix, this.getNamespaceURI( prefix ) ), name.group( 3 ) );
            }
            else {
                this.name = new QName( XML.defaultNamespace, name.group( 3 ) );
            }
        }
    }

    /** Transforms the Java DOM into the E4X DOM.
     */
    void buildENodeDom(ENode parent) {
        NodeList kids = parent.node.getChildNodes();
        for( int i=0; i<kids.getLength(); i++) {
            if( ( kids.item(i).getNodeType() == Node.COMMENT_NODE && 
                  parent.XML.ignoreComments ) ||
                ( kids.item(i).getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && 
                  parent.XML.ignoreProcessingInstructions ) )
                continue;
            if ( kids.item(i).getNodeType() == Node.TEXT_NODE && 
                 parent.XML.ignoreWhitespace )
                if ( kids.item(i).getNodeValue().trim().equals( "" ) )
                    continue;
                else 
                    kids.item(i).setNodeValue( kids.item(i).getNodeValue().trim() );

            ENode n = new ENode( kids.item(i), parent , null );
            buildENodeDom(n);
            parent.children.add(n);
        }
    }

    /** Turns a string into a DOM.
     */
    void init( String s ){
        Node temp;
        try {
            /* Some XML has the stupid form
               <><foo>bar</foo></>
             */
            Pattern emptyTags = Pattern.compile( "<>(.*)</>", Pattern.DOTALL );
            Matcher m = emptyTags.matcher( s );
            if( m.matches() ) {
                s = m.group( 1 );
                if( s == null ) 
                    s = "";
            }

            if ( _html )
                temp = HTMLUtil.parse( s );
            else
                temp = XMLUtil.parse( "<parent>" + s + "</parent>" ).getDocumentElement();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            throw new RuntimeException( "can't parse : " + e );
        }

        NodeList kids = temp.getChildNodes();
        for( int i=0; i<kids.getLength(); ) {
            Node k = kids.item(i);
            if( ( k.getNodeType() == Node.TEXT_NODE && 
                  k.getTextContent().matches( "\\s*" ) &&
                  XML.ignoreWhitespace ) ||
                ( k.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE &&
                  XML.ignoreProcessingInstructions ) ||
                ( k.getNodeType() == Node.COMMENT_NODE &&
                  XML.ignoreComments ) ) {
                temp.removeChild( k );
                continue;
            }
            i++;
        }

        if( kids.getLength() > 1 ) {
            children = new XMLList();
            for( int i=0; i < kids.getLength(); i++ ) {
                ENode kid = new ENode( kids.item( i ) , null , null );
                buildENodeDom( kid );
                children.add( kid );
            }
        }
        else {
            if( kids.getLength() == 1 ) {
                node = kids.item( 0 );
            }
            else {
                node = document.createTextNode( "" );
            }
            nodeSetup( null );
            buildENodeDom( this );
        }
    }

    /** @getter
     */
    public Object get( Object n ) {
        if ( n == null )
            return null;

        if( Pattern.matches( "\\d+", n.toString() ) || n instanceof Number ) {
            return new XMLList( E4X.toXMLList( this ).get( Integer.parseInt( n.toString() ) ) );
        }

        if ( n instanceof String || n instanceof JSString ){
            String s = n.toString();
            if( s.equals( "tojson" ) ) 
                return null;

            // the order of these is important so that, for example, JSString.length doesn't
            // get called in preference to ENode.length.
            Object objFromProto = ENode._cons.getPrototype().get( s );
            if( objFromProto != null && objFromProto instanceof ENodeFunction ) {
                ((ENodeFunction)objFromProto).setup( (XMLList)E4X._nodeGet( this, s ) , this , s );
                return new XMLList( ((ENodeFunction)objFromProto).getNode() );
            }
            // if this is a simple node, we could be trying to get a string function
            if( this.hasSimpleContent() ) {
                Object o = (new JSString( this.toString()) ).get( n );
                if( o != null ) {
                    return o;
                }
            }

            XMLList o;
            // an attribute search could start with ..
            if( s.indexOf( "@" ) >= 0 ) {
                o = E4X._attrNodeGet( this, s );
                return ( o.size() == 0 && !s.equals( "@*" ) ) ? 
                    new XMLList( attributeNodeFactory( this, s ) ) : o;
            }
            else {
                o = E4X._nodeGet( this, s );
                return ( o.size() == 0 && E4X.isXMLName(s) ) ? new XMLList( new ENode( this, s ) ) : o;
            }
        }

        if ( n instanceof QName ) {
            Object o;
            if( ((QName)n).localName.toString().indexOf( "@" ) >= 0 ) {
                o = E4X._attrNodeGet( this, n );
                return ( o == null ) ? new XMLList( attributeNodeFactory( this, n ) ) : o;
            }
            else {
                o = E4X._nodeGet( this, n );
                return ( o == null && 
                         E4X.isXMLName( ((QName)n).localName.toString() ) ) ? 
                    new XMLList( new ENode( this, n ) ) : o;
            }
        }

        if ( n instanceof Query ) {
            Query q = ( Query )n;
            XMLList matching = new XMLList();
            if( this instanceof XMLList ) {
                for ( ENode theNode : (XMLList)this ){
                    if ( q.match( theNode ) ) {
                        matching.add( theNode );
                    }
                }
            }
            else if( q.match( this ) ) {
                matching.add( this );
            }
            return matching;
        }

        throw new RuntimeException( "can't handle : " + n.getClass() );
    }

    public ENode getOne() {
        if( this instanceof XMLList ) 
            return ((XMLList)this).get( 0 );
        else 
            return this.children.get( 0 );
    }

    private boolean deleteByIndex( int i ) {
        if( this.children.size() > i ) {
            this.children.remove( i );
            return true;
        }
        return false;
    }

    private void insert( int i, Object v ) {
        if( ( v instanceof ENode && v == this ) ||
            this.isSimpleTypeNode() )
            return;

        int n = 1;
        if( v instanceof XMLList ) {
            n = ((ENode)v).length();
            if( n == 0 )
                return;

            for( int j=0; j < n ; j++ ) {
                ENode elem = ((XMLList)((ENode)v).get( j )).getOne();
                elem.parent = this;
                this.children.add( i + j, elem );
            }
        }
        else {
            this.replace( i , v );
        }
    }

    private void replace( int i, Object v ) {
        if( this.isSimpleTypeNode() )
            return;

        if( this instanceof XMLList && i >= this.length() )
            i = this.length();
        else if( i >= this.children().length() ) 
            i = this.children().length();
        
        if( v instanceof ENode && 
            !( v instanceof XMLList ) &&
            ( ((ENode)v).getNodeType() == Node.ELEMENT_NODE ||
              ((ENode)v).getNodeType() == Node.COMMENT_NODE ||
              ((ENode)v).getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ||
              ((ENode)v).getNodeType() == Node.TEXT_NODE ) ) {
            if( ((ENode)v).getNodeType() == Node.ELEMENT_NODE && v == this ) {
                return;
            }
            ((ENode)v).parent = this;
            // if a child exists in this position, replace it
            if( i < this.children().size() ) {
                ((ENode)this).children( false ).set( i, (ENode)v ); 
            }
            // otherwise, add it
            else {
                ((ENode)this).children( false ).add( i, (ENode)v ); 
            }
        }
        else if( v instanceof XMLList ) {
            this.deleteByIndex( i );
            this.insert( i, (ENode)v );
        }
        else {
            ENode tnode = new ENode( document.createTextNode( v.toString() ), this, null );
            this.deleteByIndex( i );
            this.children( false ).add( i, tnode );
        }
    }

    /** @setter
     */
    public Object set( Object p, Object v ) {
        Object vcopy = v instanceof ENode ? ((ENode)v).copy() : v + "";

        // this should be in xmllist, but due to conflicts 
        // between List and JSObject interfaces, it's just easier here
        if( this instanceof XMLList ) {
            XMLList x = (XMLList)this;
            x.get( 0 ).deDummy();

            // 2
            String localName = p.toString();
            if( localName.matches( "(@?\\*)|"+JSNumber.POSSIBLE_NUM ) ) {
                boolean isAttribute = localName.charAt( 0 ) == '@';
                boolean isAll = localName.length() <= 2 && localName.charAt( localName.length() - 1 ) == '*';
                if( isAttribute )
                    localName = localName.substring( 1 );
                if( isAll ) 
                    localName = "0";

                int i = Integer.parseInt( localName );

                // 2.a
                ENode r = x.get( i );

                // 2.c
                if( i >= x.length() ) {
                    // 2.c.i
                    if( r instanceof XMLList ) {
                        if( r.length() != 1 )
                            return v;
                        r = ((XMLList)r).get( 0 );
                    }

                    // 2.c.ii
                    if( !r.isDummy() && r.getNodeType() != Node.ELEMENT_NODE )
                        return v;
                        
                    // 2.c.iii
                    ENode y = null;

                    // 2.c.iv
                    if( isAttribute ) {
                        XMLList attributeExists = (XMLList)r.get( p );
                        if( attributeExists.length() > 0 )
                            return v;
                        else
                            y = attributeNodeFactory( r, p );
                    }
                    // 2.c.v
                    else if( isAll ) {
                        y = new ENode( document.createTextNode( v.toString() ), x.parent(), null );
                        /*    if( x.parent() != null ) {
                            x.parent().children.removeAll();
                            }*/
                    }
                    // 2.c.vi
                    else {
                        if( p instanceof QName ) {
                            y = new ENode( document.createElementNS( r.name().uri.toString(), r.name().localName.toString() ), x.parent(), null );
                        }
                        else if( r.localName() != null ) {
                            y = new ENode( document.createElement( r.localName() ), x.parent(), null );
                        }
                        // foo[1000] = "bar" => pick up "foo"
                        else {
                            y = new ENode( document.createElement( this.getOne().name().localName.toString() ), x.parent(), null );
                        }
                    }

                    // 2.c.vii
                    i = x.length();

                    // 2.c.viii
                    if( y.getNodeType() != Node.ATTRIBUTE_NODE ) {
                        int j = 0;
                        // 2.c.viii.1
                        // find the child index of a node
                        if( x.parent() != null ) {
                            int attrSize = x.parent().attributes().size();
                            j = x.get( i-1 ).childIndex();
                            x.parent().children.add( j+1+attrSize, y );
                        }

                        // 2.c.viii.2
                        if( vcopy instanceof XMLList ) {
                            y.setName( p );
                        }
                        // 2.c.viii.3
                        else if( vcopy instanceof ENode ) {
                            y.setName( ((ENode)vcopy).name );
                        }
                    }

                    // 2.c.ix
                    x.add( y );
                }

                // 2.d
                if( vcopy instanceof String ||
                    vcopy instanceof JSString || 
                    ( vcopy instanceof ENode && 
                      ( ((ENode)vcopy).getNodeType() == Node.TEXT_NODE || 
                        ((ENode)vcopy).getNodeType() == Node.ATTRIBUTE_NODE ) ) ) {
                    vcopy = vcopy.toString();
                }
                
                int q = x.get( i ).childIndex();

                // 2.e
                if( x.get( i ).getNodeType() == Node.ATTRIBUTE_NODE ) {
                    x.get( i ).parent.setAttribute( p, v );
                }
                // 2.f
                else if( vcopy instanceof XMLList ) {
                    if( parent != null ) {
                        parent.replace( q, (XMLList)vcopy );
                        for( ENode child : (XMLList)vcopy ) {
                            child.parent = parent;
                        }
                    }
                    if( ((XMLList)vcopy).length() == 0 )
                        parent.children.remove( q );
                    else
                        parent.children.addAll( q, (XMLList)vcopy );
                }
                // 2.g
                else if( vcopy instanceof ENode || 
                         r.getNodeType() == Node.TEXT_NODE ||
                         r.getNodeType() == Node.COMMENT_NODE ||
                         r.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ) {
                    if( x.get( i ).parent != null ) {
                        x.get( i ).parent.replace( q, (ENode)vcopy );
                    }
                    if( vcopy instanceof String ||
                        vcopy instanceof JSString ) {
                        x.set( i, new ENode( document.createTextNode( vcopy.toString() ), x, null ) );
                    }
                    else {
                        x.set( i, (ENode)vcopy );
                    }
                }
                // 2.h
                else {
                    x.get( i ).set( "*", vcopy );
                }
            }
            // 3
            else if( x.length() <= 1 ) {
                return this.getOne().set( p, vcopy );
            }
            return vcopy;
        }

        if( this.isSimpleTypeNode() ) 
            return v;

        String uri = p instanceof QName ? 
            ((QName)p).uri.toString() : null;
        String localName = p instanceof QName ? 
            ((QName)p).localName.toString() : p.toString();


        this.deDummy();

        // set an attribute
        if( localName.startsWith("@") )
            return setAttribute(p, v.toString());
        int i = -1;
        // primitiveAssign == true if v is a string being a assigned to an ENode
        boolean primitiveAssign = !(v instanceof ENode) && !localName.equals( "*" );
        for( int k = this.children( false ).length() - 1; k >= 0; k-- ) {
            ENode foo = this.children( false ).get( k );
            if( localName.equals( "*" ) || 
                ( foo.node.getNodeType() == Node.ELEMENT_NODE && 
                  localName.equals( foo.localName() ) ) &&
                ( uri == null || 
                  ( foo.node.getNodeType() == Node.ELEMENT_NODE && 
                    uri.equals( foo.name().uri ) ) ) ) {
                if( i != -1 ) {
                    this.deleteByIndex( i );
                }
                i = k;
            }
        }

        if( i == -1 ) {
            i = this.children().length();
            if( primitiveAssign ) {
                QName name;
                if( uri == null )
                    name = new QName( XML.getDefaultNamespace(), p );
                else
                    name = new QName( p );

                ENode y = new ENode( document.createElementNS( name.uri.toString(), name.localName.toString() ), this, null );
                Namespace ns = name.getNamespace();
                this.replace( i, y );
                y.addInScopeNamespace( ns ); 
            }
        }

        if( primitiveAssign == true ) {
            ENode n = this.children( false ).get( i );
            n.children = new XMLList();
            if( !vcopy.equals( "" ) ) {
                n.replace( 0, vcopy );
            }
        }
        else {
            this.replace( i, vcopy );
        }
        return v;
    }

    private Object setAttribute( Object k, Object v ) {
        QName name = E4X.toXMLName( k );

        XMLList obj = (XMLList)this.get( k );
        ENode n = ((XMLList)obj).getOne();
        String s;
        if( v instanceof XMLList ) {
            StringBuilder buf = new StringBuilder( ((XMLList)v).get(0).toString() );
            for( ENode str : (XMLList)v ) {
                buf.append( " " + str );
            }
            s = buf.toString();
        }
        else {
            s = v.toString();
        }

        ENode a = null;
        for( int i=0; i<this.children.size(); i++ ) {
            ENode child = this.children.get( i );
            if( child.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
                if( n.localName().equals( child.localName() ) &&
                    ( n.name().uri == null || n.name().uri.equals( child.name().uri ) ) ) {
                    if( a == null ) {
                        a = child;
                    }
                    else {
                        this.children.remove( child );
                        i--;
                    }
                }
            }
        }

        if( a == null ) {
            if( name.uri == null ) {
                Namespace nons = new Namespace();
                name = new QName( nons, name );
            }
            else {
                name = new QName( name );
            }
            a = attributeNodeFactory( this, name );
            this.children.add( a );
            this.addInScopeNamespace( name.getNamespace() );
            a.setDummy( false );
        }

        ((Attr)a.node).setValue( E4X.escapeAttributeValue( s ) );
        return v;
    }

    /**
     * Called for delete xml.prop
     */
    public Object removeField(Object o) {
        if( this instanceof XMLList ) {
            if( o.toString().matches( JSNumber.POSSIBLE_NUM ) ) {
                int i = Integer.parseInt( o.toString() );
                if( i >= ((XMLList)this).length() )
                    return true;

                ENode parent = ((XMLList)this.get( i )).parent();
                if( parent != null )
                    parent.children(false).remove( i );
                ((XMLList)this).remove( i );
                return true;
            }
            for( ENode q : (XMLList)this ) {
                if( q.getNodeType() == Node.ELEMENT_NODE ) {
                    q.removeField( o );
                }
            }
            return true;
        }

        boolean attribute = false;
        if( ( o instanceof QName && 
              ((QName)o).localName.toString().charAt(0) == '@' ) || 
            o.toString().charAt(0) == '@' )
            attribute = true;
        QName name = E4X.toXMLName( o );

        XMLList list;
        Object obj = get( o );
        if( obj instanceof ENodeFunction ) {
            list = new XMLList( ((ENodeFunction)obj).cnode );
        }
        else {
            list = (XMLList)obj;
        }

        for( int q = 0; q < list.length(); q++) {
            ENode elemq = list.get( q );
            if( attribute && elemq.getNodeType() == Node.ATTRIBUTE_NODE &&
                ( name.localName.toString().equals( "*" ) ||
                  elemq.localName().equals( name.localName.toString() ) ) &&
                ( name.uri == null || 
                  elemq.name().uri.toString().equals( name.uri.toString() ) ) ) {
                this.children.remove( elemq );
                elemq = null;
            }
            else if( ( name.localName.toString().equals( "*" ) ||
                ( elemq.getNodeType() == Node.ELEMENT_NODE && 
                  elemq.localName().equals( name.localName.toString() ) ) ) &&
                ( name.uri == null || 
                  ( elemq.getNodeType() == Node.ELEMENT_NODE &&
                    elemq.name().uri.toString().equals( name.uri.toString() ) ) ) ) {
                this.children.remove( elemq );
                elemq = null;
            }
        } 
        return true;
    }

    public int getNodeType() {
        if( this.node == null )
            return -1;
        return this.node.getNodeType();
    }

    /** 
     * Adds a namespace declaration to the scope namespace.
     * If this scope namespace already contains a namespace <pre>x</pre> with the
     * same prefix, the prefix of <pre>x</pre> is set to <pre>null</pre>.
     */
    public ENode addNamespace( Object ns ) {
        this.addInScopeNamespace( new Namespace( ns ) );
        return this;
    }

    /**
     * Appends a given child to this element's properties.
     */
    public ENode appendChild( Object childObj ) {
        if( this instanceof XMLList )
            return this.getOne().appendChild( childObj );

        if( this.children == null )
            this.children = new XMLList();

        ENode child = this.toXML( childObj );
        child.parent = this;
        this.children( false ).add( child );
        return this;
    }

    /**
     * Returns a list of zero or one attributes whose name matches 
     * the given property name.
     */
    public XMLList attribute( String prop ) {
        return new XMLList( (ENode)this.get("@"+prop) );
    }

    /**
     * Returns this node's attributes.
     */
    public XMLList attributes() {
        return new XMLList( (ENode)this.get( "@*" ) );
    }

    /**
     * Returns children matching a given name or index.
     */
    public XMLList child( Object propertyName ) {
        boolean xmllist = this instanceof XMLList;
        XMLList nodeList = xmllist ? (XMLList)this : this.children;
        if( propertyName.toString().matches( JSNumber.POSSIBLE_NUM ) ) {
            if( !xmllist ) {
                return (XMLList)((XMLList)this.get( "*" )).get( propertyName );
            }
            int i = Integer.parseInt( propertyName.toString() );

            return new XMLList( nodeList.get(i) );
        }
        Object obj = this.get(propertyName);
        return new XMLList( ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode );
    }

    /**
     * Returns a number representing the position of this element within its parent.
     */
    public int childIndex() {
        if( parent() == null || 
            parent().getNodeType() == Node.ATTRIBUTE_NODE || 
            this.getNodeType() == Node.ATTRIBUTE_NODE )
            return -1;

        if( this instanceof XMLList ) {
            return parent().printableChildren().indexOf( this.getOne() );
        }
        return parent.printableChildren().indexOf( this );
    }

    /**
     * Returns this node's children.
     */
    public XMLList children() {
        return children( true );
    }

    private XMLList children( boolean copy ) {
        if( this.children == null ) {
            children = new XMLList();
            return children;
        }
        if( copy ) {
            XMLList child = new XMLList();
            for( ENode n : this.children ) {
                if( n.node.getNodeType() != Node.ATTRIBUTE_NODE )
                    child.add( n );
            }
            return child;
        }
        else {
            return children;
        }
    }

    /**
     * Returns a list of comments, assuming XML.ignoreComments
     * was set to false when the list was created.
     */
    public XMLList comments() {
        XMLList comments = new XMLList();

        for( ENode child : this.children( false ) ) {
            if( child.node.getNodeType() == Node.COMMENT_NODE )
                comments.add( child );
        }
        return comments;
    }

    /** FIXME
     * Compares this with another XML object.
     */
    public boolean contains( Object o ) {
        if( this.equals( o ) ) 
            return true;

        if( !(o instanceof ENode ) ) {
            if( ( o instanceof java.lang.String || 
                  o instanceof ed.js.JSString ) &&
                o.toString().equals( this.toString() ) )
                return true;
            return false;
        }

        ENode n = (ENode)o;

        if( this instanceof XMLList && n instanceof XMLList ) {
            XMLList x = (XMLList)this;
            XMLList x2 = (XMLList)n;
            if( x.size() != x2.size() )
                return false;
            for( int i=0; i < x.size(); i++ ) {
                if( !x.get(i).contains( x2.get(i) ) ) {
                    return false;
                }
            }
            return true;
        }
        else if( !(this instanceof XMLList) && !(n instanceof XMLList) ) {
            if( !this.name.equals( n.name ) ||
                !this.node.isEqualNode( n.node ) ) 
                //                    !this.inScopeNamespaces.equals( n.inScopeNamespaces ) )
                return false;

            if( ( this.children == null && n.children != null ) ||
                ( this.children != null && n.children == null ) )
                return false;

            if( this.children != null ) {
                if( this.children.size() != n.children.size() )
                    return false;
                for( int i=0; i<this.children.size(); i++ ) {
                    if( !this.children.get( i ).contains( n.children.get( i ) ) ) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Creates a deep copy of this node.
     */
    public ENode copy() {
        if( this instanceof XMLList ) {
            return new XMLList( this );
        }
        return new ENode( this );
    }

    /** 
     * Returns all descendants with a given name, or all decendants
     * if a name is not provided.
     */
    public ENode descendants( String name ) {
        return (ENode)this.get( ".." + name );
    }

    public ENode descendants() {
        return this.descendants( "*" );
    }


    public XMLList elements( String name ) {
        if( this.children == null || this.children.size() == 0)
            return null;
            
        if(name == null || name == "") {
            name = "*";
        }

        XMLList list = new XMLList();
        for( ENode n : this.children ) {
            if( n.node != null && n.node.getNodeType() == Node.ELEMENT_NODE && (name.equals( "*" ) || n.localName().equals(name)) )
                list.add( n );
        }
        return list;
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
                return n.prefix.toString();
        }
        return null;
    }

    private String getNamespaceURI( String prefix ) {
        ENode temp = this;
        while( temp != null ) {
            for( Namespace n : temp.inScopeNamespaces ) {
                if( n.prefix != null && n.prefix.equals( prefix ) ) 
                    return n.uri.toString();
            }
            temp = temp.parent;
        }
        return null;
    }

    public boolean hasOwnProperty( String prop ) {
        Object foo = get( prop );
        if( foo instanceof ENodeFunction || !((ENode)foo).isDummy() ) 
            return true;
        return false;
    }

    private boolean isSimpleTypeNode( ) {
        if( this.node == null )
            return true;
        short type = this.node.getNodeType();
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
    public boolean hasComplexContent() {
        if( !(this instanceof XMLList) && this.isSimpleTypeNode() )
            return false;

        XMLList list = this instanceof XMLList ? (XMLList)this : this.children;
        if( list == null ) 
            return false;

        for( ENode n : list ) {
            if( n.node.getNodeType() == Node.ELEMENT_NODE )
                return true;
        }
        return false;
    }

    /**
     * Returns if this node contains simple content.  An XML node is considered to have 
     * simple content if it represents a text or attribute node or an XML element with no child elements.
     */
    public boolean hasSimpleContent() {
        if( this instanceof XMLList ) {
            XMLList x = (XMLList)this;
            switch( x.length() ) {
            case 0 :
                return true;
            case 1 :
                return x.get( 0 ).hasSimpleContent();
            default :
                for( ENode elem : x ) {
                    if( elem.getNodeType() == Node.ELEMENT_NODE )
                        return false;
                }
            }
            return true;
        }
        if( this.node != null ) {
            int type = this.getNodeType();
            if( type == Node.PROCESSING_INSTRUCTION_NODE ||
                type == Node.COMMENT_NODE )
                return false;
        }
        XMLList list = this.children;
        if( list == null )
            return false;

        for( ENode n : list ) {
            if( n.getNodeType() == Node.ELEMENT_NODE )
                return false;
        }
        return true;
    }

    public ArrayList<Namespace> inScopeNamespaces() {
        ArrayList<Namespace> isn = new ArrayList<Namespace>();
        ENode temp = this;
        while( temp != null ) {
            for( Namespace ns : temp.inScopeNamespaces ) {
                if( ! isn.contains( ns ) )
                    isn.add( ns );
            }
            temp = temp.parent();
        }
        return isn;
    }


    public ENode insertChildAfter(Object child1, Object child2) {
        if( this.isSimpleTypeNode() ) 
            return null;
        else if( child1 instanceof XMLList ) 
            return this.insertChildAfter( ((XMLList)child1).get( 0 ), child2 );

        int index = 0;
        if( child1 != null ) {
            index = this.toXML( child1 ).childIndex() + 1;
            if( index == 0 ) 
                return this;
        }
        this.children.add( index, this.toXML( child2 ) );
        return this;
    }

    public ENode insertChildBefore(Object child1, Object child2) {
        if( this.isSimpleTypeNode() ) 
            return null;
        else if( child1 instanceof XMLList ) 
            return this.insertChildBefore( ((XMLList)child1).get( 0 ), child2 );

        int index = this.children.length();

        if( child1 != null )
            index = this.toXML( child1 ).childIndex();
        if( index == -1 ) 
            return this;
        this.children.add( index, this.toXML( child2 ) );
        return this;
    }

    public int length() {
        return 1;
    }

    public String localName() {
        if( this instanceof XMLList ) 
            return this.getOne().localName();
        // comments and text nodes don't have local names
        if( this.name == null ) 
            return null;
        return this.name.localName.toString();
    }

    public QName name() {
        if( this instanceof XMLList )
            return this.getOne().name();
        return this.name;
    }

    public Namespace namespace() {
        return namespace( null );
    }

    public Namespace namespace( String prefix ) {
        if( this instanceof XMLList ) 
            return this.getOne().namespace( prefix );

        if( prefix == null ) {
            Namespace ns = this.name.getNamespace( this.inScopeNamespaces );
            if( ns == null ) {
                return this.defaultNamespace;
            }
            return ns;
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

    private ArrayList<Namespace> getAncestors() {
        ArrayList<Namespace> ancestors = new ArrayList<Namespace>();

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

    public ArrayList<Namespace> namespaceDeclarations() {
        if( this instanceof XMLList ) {
            return this.getOne().namespaceDeclarations();
        }

        ArrayList<Namespace> a = new ArrayList<Namespace>();
        if( this instanceof XMLList || this.isSimpleTypeNode( ) )
            return a;

        ArrayList<Namespace> ancestors = this.getAncestors();
        if( this.defaultNamespace != null ) {
            ancestors.add( this.defaultNamespace );
        }

        for( Namespace ns : this.inScopeNamespaces ) {
            if( ! ns.containedIn( ancestors ) )
                a.add( ns );
        }
        return a;
    }

    public String nodeKind() {
        if( this instanceof XMLList ) {
            return this.getOne().nodeKind();
        }
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

    public ENode parent() {
        return this.parent;
    }

    /** Inserts the given child into this object prior to the existing XML properties.
     */
    public ENode prependChild( Object o ) {
        ENode x = this.toXML( o );
        if( this instanceof XMLList ) {
            this.getOne().children.add( 0, x );
        }
        else
            this.children.add( 0, x );
        return this;
    }

    public XMLList processingInstructions( String name ) {
        boolean all = name == null || name.equals( "*" );

        XMLList list = new XMLList();
        for( ENode n : this.children ) {
            if ( n.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && ( all || name.equals(n.name.localName) ) ) {
                list.add( n );
            }
        }
        return list;
    }

    /**
     * So, the spec says that this should only return toString(prop) == "0".  However, the Rhino implementation returns true
     * whenever prop is a valid index, so I'm going with that.
     */
    public boolean propertyIsEnumerable( Object prop ) {
        if( prop == null ) {
            return false;
        }

        Pattern num = Pattern.compile("\\d+");
        Matcher m = num.matcher(prop.toString());
        if( m.matches() ) {
            int pnum = Integer.parseInt( prop.toString() );
            if( this instanceof XMLList ) {
                return ((XMLList)this).size() > pnum;
            }
            return this.printableChildren().size() > pnum;

        }
        return false;
    }

    public boolean propertyIsEnumerable() {
        return false;
    }

    public ENode removeNamespace(Object namespace) {
        if( this instanceof XMLList || this.isSimpleTypeNode() ) 
            return this;

        Namespace ns = new Namespace( namespace );

        if( ns.prefix == null || ns.prefix.equals( "" ) ) {
            for( int i=0; i < this.inScopeNamespaces.size(); i++ ) {
                if( this.inScopeNamespaces.get(i).uri.equals( ns.uri ) ) {
                    this.inScopeNamespaces.remove( i );
                    break;
                }
            }
        }
        else {
            for( int i=0; i < this.inScopeNamespaces.size(); i++ ) {
                if( this.inScopeNamespaces.get( i ).uri.equals( ns.uri ) &&
                    this.inScopeNamespaces.get( i ).prefix.equals( ns.prefix ) ) {
                    this.inScopeNamespaces.remove( i );
                    break;
                }
            }
        }
        for( ENode enode : this.children ) {
            enode.removeNamespace( namespace );
        }
        return this;
    }

    public ENode replace( String prop, Object value ) {
        set( prop, value );
        return this;
    }

    public Object setChildren( Object value ) {
        if( this instanceof XMLList ) 
            return this.getOne().setChildren( value );

        this.set("*", toXML( value ) );
        return this;
    }

    public void setLocalName( Object name ) {
        if( this.node == null ||
            this.node.getNodeType() == Node.TEXT_NODE ||
            this.node.getNodeType() == Node.COMMENT_NODE )
            return;
        if( this.name == null )
            this.name = new QName();
        this.name.localName = ( name instanceof QName ) ? ((QName)name).localName : new JSString( name.toString() );
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
    public void setName( Object name ) {
        if( this.node == null ||
            this.node.getNodeType() == Node.TEXT_NODE ||
            this.node.getNodeType() == Node.COMMENT_NODE )
            return;

        QName n;
        if ( name instanceof QName && 
             ((QName)name).uri != null && 
             ((QName)name).uri.equals("") )
            name = ((QName)name).localName;

        if( name instanceof QName )
            n = new QName( name );
        else 
            n = new QName( XML.defaultNamespace, name );

        if( this.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE )
            n.uri = new JSString( "" );
        this.name = n;

        Namespace ns;
        if( n.uri == null )
            ns = XML.defaultNamespace ;
        else if( n.prefix == null )  
            ns = new Namespace( n.uri.toString() );
        else
            ns = new Namespace( n.prefix.toString(), n.uri.toString() );

        if( this.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
            if( this.parent == null )
                return;
            this.parent.addInScopeNamespace( ns );
        }
        if( this.node.getNodeType() == Node.ELEMENT_NODE ){
            this.addInScopeNamespace( ns );
        }
    }

    public void setNamespace( Object ns) {
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
            this.parent.addInScopeNamespace( ns2 );
        }
        if( this.node.getNodeType() == Node.ELEMENT_NODE ) {
            this.addInScopeNamespace( ns2 );
        }
    }

    public XMLList text() {
        XMLList list = new XMLList();
        if( this.node.getNodeType() == Node.ELEMENT_NODE ) {
            for( ENode child : this.children ) {
                if( child.node.getNodeType() == Node.TEXT_NODE )
                    list.add( child );
                if( child.node.getNodeType() == Node.CDATA_SECTION_NODE )
                    list.add( new ENode( document.createTextNode(((CDATASection)child.node).getWholeText() ), this, null ) );
            }
        }
        return list;
    }

    public String toString() {
        StringBuilder xml = new StringBuilder();
        if( this.node != null || ( this.children != null && this.children.size() == 1 ) ) {
            ENode singleNode = ( this.node != null ) ? this : this.children.get(0);
            List<ENode> kids = singleNode.printableChildren();

            // if this is an empty top level element, return nothing
            if( singleNode.node.getNodeType() == Node.ELEMENT_NODE && ( kids == null || kids.size() == 0 ) )
                return "";

            if( singleNode.node.getNodeType() == Node.ATTRIBUTE_NODE || 
                singleNode.node.getNodeType() == Node.TEXT_NODE )
                return singleNode.node.getNodeValue();

            if ( singleNode.node.getNodeType() == Node.ELEMENT_NODE &&
                 singleNode.children != null &&
                 singleNode.childrenAreTextNodes() ) {
                for( ENode n : kids )
                    if( XML.ignoreWhitespace )
                        xml.append( n.node.getNodeValue().trim() );
                    else 
                        xml.append( n.node.getNodeValue() );
                return xml.toString();
            }

            singleNode.append( xml, 0, new ArrayList<Namespace>() );
        }

        if( xml.length() > 0 && xml.charAt(xml.length() - 1) == '\n' ) {
            xml.deleteCharAt(xml.length()-1);
        }
        return xml.toString();
    }

    public StringBuilder append( StringBuilder buf , int level , ArrayList<Namespace> ancestors ){
        if( this.node == null )
            return buf;

        if( XML.prettyPrinting )
            _level( buf, level );

        switch ( this.node.getNodeType() ) {
        case Node.TEXT_NODE :
            if( XML.prettyPrinting ) {
                return buf.append( E4X.escapeElementValue( this.node.getNodeValue().trim() ) );
            }
            else {
                return buf.append( E4X.escapeElementValue( this.node.getNodeValue() ) );
            }
        case Node.ATTRIBUTE_NODE :
            return buf.append( E4X.escapeAttributeValue( this.node.getNodeValue() ) );
        case Node.COMMENT_NODE :
            return buf.append( "<!--"+this.node.getNodeValue()+"-->" );
        case Node.PROCESSING_INSTRUCTION_NODE :
            return buf.append( "<?" + this.localName() + " " + ((ProcessingInstruction)this.node).getData() + "?>");
        case Node.CDATA_SECTION_NODE :
            return buf.append( "<![CDATA[" + ((CDATASection)this.node).getWholeText() + "]]>" );
        }

        buf.append( "<" );
        String prefix = "";
        if( this.name.prefix != null && !this.name.prefix.equals( "" ) ) {
            prefix = this.name.prefix + ":";
        }
        buf.append( prefix + this.name.localName ).append( this.attributesToString( ancestors ));

        List<ENode> kids = this.printableChildren();
        if ( kids == null || kids.size() == 0 ) {
            return buf.append( "/>" );
        }
        buf.append(">");

        boolean indentChildren = ( kids.size() > 1 ) || ( kids.size() == 1 && kids.get(0).node.getNodeType() != Node.TEXT_NODE );
        int nextIndentLevel = level;
        if( XML.prettyPrinting && indentChildren )
            nextIndentLevel = level + 1;
        else
            nextIndentLevel = 0;
        for ( ENode c : kids ) {
            if( c.node.getNodeType() == Node.ATTRIBUTE_NODE ||
                ( XML.ignoreComments && c.node.getNodeType() == Node.COMMENT_NODE ) ||
                ( XML.ignoreProcessingInstructions && c.node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ) )
                continue;

            if( XML.prettyPrinting && indentChildren ) 
                buf.append( "\n" );

            c.append( buf , nextIndentLevel , ancestors );
            // delete from ancestors
            ancestors.remove( c.defaultNamespace );
        }
        if( XML.prettyPrinting && indentChildren ) {
            buf.append( "\n" );
            _level( buf, level );
        }
        buf.append( "</" );
        return buf.append( prefix + this.name.localName ).append( ">" );
    }

    private StringBuilder _level( StringBuilder buf , int level ){
        for ( int i=0; i<level; i++ ) {
            for( long j=0; j< XML.prettyIndent; j++) {
                buf.append( " " );
            }
        }
        return buf;
    }

    private String attributesToString( ArrayList<Namespace> ancestors ) {
        StringBuilder buf = new StringBuilder();

        ArrayList<Namespace> nsDeclarations = new ArrayList<Namespace>();
        if( this.name != null ) {
            Namespace thisNS = this.name.getNamespace();
            if( thisNS != null && thisNS.prefix == null && !ancestors.contains( thisNS ) ) {
                nsDeclarations.add( thisNS );
            }
        }

        for( Namespace ns : this.inScopeNamespaces ) {
            if( !ancestors.contains( ns ) ) {
                ancestors.add( ns );
                Namespace ns2 = new Namespace( ns );
                if( ns2.prefix == null ){
                    ns2.prefix = new JSString( ns2.getPrefix() );
                }
                nsDeclarations.add( ns2 );
            }
        }

        Hashtable<String,Integer> matches = new Hashtable<String,Integer>();
        for( Namespace ns : nsDeclarations ) {
            if( ( ns.prefix == null || ns.prefix.equals( "" ) ) && ( ns.uri == null || ns.uri.equals( "" ) ) )
                continue;

            if( ns.prefix == null ) {
                buf.append( " xmlns=\"" + ns.uri + "\"" );
            }
            else {
                // hack!
                if( inScopeNamespaces.size() == 1 ) 
                    continue;

                String genPrefix = ns.prefix.toString();
                if( ns.prefix.equals( "" ) )
                    genPrefix = ns.getPrefix();

                Integer num = matches.get( genPrefix );
                if( num != null ) {
                    matches.put( genPrefix, num + 1 );
                    genPrefix += "-" + num;
                }
                else {
                    matches.put( genPrefix, 1 );
                }

                buf.append( " xmlns:" + genPrefix + "=\"" + ns.uri + "\"" );
            }
        }    

        // get attrs
        ArrayList<ENode> attr = this.getAttributes();
        String[] attrArr = new String[attr.size()];
        for( int i = 0; i< attr.size(); i++ ) {
            String prefix = "";
            if( attr.get(i).name.uri != null ) {
                int prefixIdx = inScopeNamespaces.indexOf( attr.get(i).name().getNamespace() );
                if( prefixIdx >= 0 ) {
                    Namespace ns = inScopeNamespaces.get( prefixIdx );
                    if( ns.prefix == null )
                        prefix = ns.getPrefix() + ":";
                    else if( !ns.prefix.equals( "" ) ) 
                        prefix = ns.prefix.toString() + ":";
                }
            }
            attrArr[i] = " " + prefix + attr.get(i).localName() + "=\"" + attr.get(i).node.getNodeValue() + "\"";
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
        List<ENode> kids = this.printableChildren();
        for( ENode n : kids ) {
            if( n.node.getNodeType() != Node.TEXT_NODE &&
                n.node.getNodeType() != Node.CDATA_SECTION_NODE )
                return false;
        }
        return true;
    }

    public String toXMLString() {
        if( this.isDummy() )
            return "";

        return this.append( new StringBuilder(), 0, new ArrayList<Namespace>() ).toString();
    }

    private void addInScopeNamespace( Namespace n ) {
        if ( n == null || 
             this.node == null || 
             this.isSimpleTypeNode() )
            return;

        if( ! this.inScopeNamespaces.contains( n ) ) {
            this.inScopeNamespaces.add( n );
        }

    }

    public ArrayList getAttributes() {
        ArrayList<ENode> list = new ArrayList<ENode>();

        if(node == null && ( children == null || children.size() == 0)) 
            return list;

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
        ENode n = this instanceof XMLList ? this.getOne() : this ;

        if( input == null )
            return null;

        if( input instanceof Boolean ||
            input instanceof Number ||
            input instanceof JSString )
            return toXML(input.toString() );
        else if( input instanceof String ) {
            return new ENode(document.createTextNode((String)input), n , null);
        }
        else if( input instanceof Node )
            return new ENode((Node)input, n, null);
        else if( input instanceof ENode )
            return (ENode)input;
        else
            return null;
    }

    public Set<String> keySet( boolean includePrototype ) {
        XMLList list = ( this instanceof XMLList ) ? (XMLList)this : this.children;
        Set<String> c = new OrderedSet<String>();
        for( int i=0; list != null && i < list.size(); i++ ) {
            if( list.get( i ).isDummy() )
                continue;
            c.add( String.valueOf( i ) );
        }
        return c;
    }

    public Collection<ENode> valueSet() {
        XMLList list = ( this instanceof XMLList ) ? (XMLList)this : this.children;
        Collection<ENode> c = new ArrayList<ENode>();
        for( ENode n : list ) {
            if( n.isDummy() )
                continue;
            c.add( n );
        }
        return c;
    }

    public boolean isDummy() {
        return _dummy;
    }

    public void setDummy( boolean b ) {
        _dummy = b;
    }

    // attach any dummy ancestors to the tree
    private void deDummy() {
        if( this.isDummy() ) {
            ENode topParent = this;
            this.setDummy( false );
            while( topParent.parent.isDummy() ) {
                topParent.parent.children.add( topParent );
                topParent = topParent.parent;
                topParent.setDummy( false );
            }
            topParent.parent.children.add(topParent);
        }
    }

    private static ENode textNodeFactory( String s ) {
        ENode n = new ENode();
        n.init( s );
        return n;
    }

    protected static ENode getENode( Scope s ) {
        Object obj = s.getThis();
        if ( obj instanceof ENode ) 
            return (ENode)obj;
        else if ( obj instanceof ENodeFunction ) 
            return ((ENodeFunction)obj).cnode;

        // should only do this for XML.prototype
        return textNodeFactory( "" );
    }

    protected static Object getOneArg( Object foo[] ) {
        if( foo.length == 0 ) 
            throw new RuntimeException( "This method requires one argument." );
        return foo[0];
    }

    protected static Object[] getTwoArgs( Object foo[] ) {
        Object[] o = new Object[2];
        if( foo.length < 2 ) 
            throw new RuntimeException( "This method requires two arguments." );

        o[0] = foo[0];
        o[1] = foo[1];
        return o;
    }


    public static abstract class ENodeFunction extends JSFunctionCalls0 {
        public String toString() {
            return cnode == null ? "" : cnode.toString();
        }

        public Object get( Object n ) {
            return cnode.get( n );
        }

        public Object set( Object n, Object v ) {
            // there's this stupid thing where set is called for every xml node created
            if( n.equals("prototype") && v instanceof JSObjectBase)
                return null;

            return cnode == null ? null : cnode.set( n, v );
        }

        public Object removeField( Object f ) {
            return cnode.removeField( f );
        }

        public void unset() {
            cnode = null;
        }

        public void setup( XMLList something, ENode thiz, String tagname ) {
            if( something.size() > 1 ) {
                cnode = something;
                for( ENode n : something ) {
                    n.parent = thiz;
                }
            }
            else if( something.size() == 1 ) {
                cnode = something.getOne();
                cnode.parent = thiz;
            }
            else {
                cnode = new ENode( thiz, tagname );
            }
        } 

        public ENode getNode() {
            return cnode;
        }

        ENode cnode;
    }

    public static Namespace getDefaultNamespace() {
        return ((Cons)_cons).getDefaultNamespace();
    }

    private static Document document;
    {
        try {
            document = XMLUtil.parse( "<foo/>" );
        }
        catch( Exception e ) {
            document = null;
        }
    }

    private XMLList children;
    private ENode parent;
    public Node node;

    private boolean _dummy;
    private ArrayList<Namespace> inScopeNamespaces = new ArrayList<Namespace>();
    private QName name;

    public Namespace defaultNamespace;

    private boolean _new = false;
    private boolean _html = false;
}
