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
                        return getENode( s ).insertChildAfter(foo[0], getENode( s ).toXML( foo[1] ));
                    }
                });

            _prototype.set( "insertChildBefore", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        foo = getTwoArgs( foo );
                        return getENode( s ).insertChildBefore(foo[0], getENode( s ).toXML( foo[1] ));
                    }
                });
            _prototype.set( "length", new ENodeFunction() {
                    public Object call(Scope s, Object foo[]) {
                        ENode enode = getENode( s );
                        return enode instanceof XMLList ? ((XMLList)enode).size() : ( enode.node != null ? 1 : enode.children.size() );
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
                        return getENode( s ).insertChild( (Object)null, (ENode)getOneArg( foo ), 0 );
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
            if( s.equals( "prototype" ) ) {
                return this.getPrototype();
            }
            return null;
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
            return v;
        }

        public Namespace getDefaultNamespace() {
            return defaultNamespace;
        }

        public Namespace setAndGetDefaultNamespace(Object o) {
            defaultNamespace = new Namespace( "", o );
            return defaultNamespace;
        }
    }

    private Cons XML;

    public ENode(){
        super( _getCons() );
        XML = (Cons)ENode._cons;
        nodeSetup( null );
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
        nodeSetup(parent);
    }

    // creates a copy of an existing ENode
    private ENode( ENode n ) {
        super( _getCons() );
        this.XML = n.XML;
        this.name = n.name;

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
    private ENode( ENode parent, Object o ) {
        super( _getCons() );
        if( parent instanceof XMLList && 
            ((XMLList)parent).size() > 0 && 
            ((XMLList)parent).get(0) != null ) {
            parent = ((XMLList)parent).get(0);
        }
        if(parent != null && parent.node != null) {
            node = parent.node.getOwnerDocument().createElement( o.toString() );
        }
        this.children = new XMLList();
        this._dummy = true;
        nodeSetup(parent);
    }

    /** Sets this node's parent, points it to the XML constructor, gets
     * the namespace, attributes, and initializes functions for it.
     */
    void nodeSetup( ENode parent ) {
        this.parent = parent;
        this.XML = this.parent == null ? (Cons)this._getCons() : this.parent.XML;

        if( this.node != null && 
            this.node.getNodeType() == Node.ATTRIBUTE_NODE ) {
            ((Attr)this.node).setValue( E4X.escapeAttributeValue( ((Attr)this.node).getValue() ) );
        }
        getNamespace();
        addAttributes();
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
            if( temp.indexOf( ':' ) > 0) {
                String prefix = temp.substring( 0, temp.indexOf( ':' ) );
                String localName = temp.substring( temp.indexOf( ':' ) + 1 );
                this.name = new QName( new Namespace( prefix, this.getNamespaceURI( prefix ) ), localName );
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
            }
            this.name = new QName( new Namespace( prefix, this.getNamespaceURI( prefix ) ), name.group( 3 ) );
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
        if( XML.ignoreWhitespace ) 
            s = s.trim();

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
            temp = XMLUtil.parse( "<parent>" + s + "</parent>" ).getDocumentElement();
        }
        catch ( Exception e ) {
            throw new RuntimeException( "can't parse : " + e );
        }

        NodeList kids = temp.getChildNodes();
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
                node = temp.getOwnerDocument().createTextNode( "" );
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

        Pattern num = Pattern.compile("\\d+");
        Matcher m = num.matcher( n.toString() );
        if( m.matches() || n instanceof Number )
            return child( n );

        if ( n instanceof String || n instanceof JSString ){
            String s = n.toString();
            if( s.equals( "tojson" ) ) 
                return null;

            // the order of these is important so that, for example, JSString.length doesn't
            // get called in preference to ENode.length.
            Object objFromProto = ENode._cons.getPrototype().get( s );
            if( objFromProto != null && objFromProto instanceof ENodeFunction ) {
                ((ENodeFunction)objFromProto).setup( E4X._nodeGet( this, s ) , this , s );
                return (ENodeFunction)objFromProto;
            }

            // if this is a simple node, we could be trying to get a string function
            if( this.hasSimpleContent() ) {
                Object o = (new JSString( this.toString()) ).get( n );
                if( o != null ) {
                    return o;
                }
            }

            Object o = E4X._nodeGet( this, s );
            return ( o == null && E4X.isXMLName(s) ) ? new ENode( this, s ) : o;
        }

        if ( n instanceof Query ) {
            Query q = ( Query )n;
            List<ENode> matching = new ArrayList<ENode>();
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
            return E4X._handleListReturn( matching );
        }

        throw new RuntimeException( "can't handle : " + n.getClass() );
    }

    /** @setter
     */
    public Object set( Object k, Object v ) {
        if( v == null ) 
            v = "null";
        if(this.children == null ) 
            this.children = new XMLList();

        // attach any dummy ancestors to the tree
        if( this.isDummy() ) {
            ENode topParent = this;
            this._dummy = false;
            while( topParent.parent.isDummy() ) {
                topParent.parent.children.add( topParent );
                topParent = topParent.parent;
                topParent._dummy = false;
            }
            topParent.parent.children.add(topParent);
        }

        // set an attribute
        if( k.toString().startsWith("@") )
            return setAttribute(k.toString(), v.toString());

        // if v is an XML list, add each element
        if( v instanceof XMLList && !k.equals( "*" ) ) {
            int index = this.children.size();
            for( ENode target : (XMLList)v ) {
                if ( ((List)this.children).contains( target ) ) {
                    index = this.children.indexOf( target ) + 1;
                }
                else {
                    this.children.add( index, target );
                    index++;
                }
            }
            return v;
        }

        Pattern num = Pattern.compile("-?\\d+");
        Matcher m = num.matcher(k.toString());

        // find out if this k/v pair exists
        ENode n;
        Object obj = get( k );
        if( obj instanceof ENode ) {
            n = ( ENode )obj;
        }
        else {
            n = (( ENodeFunction )obj).cnode;
            if( n == null ) {
                n = new ENode( this.XML, this.defaultNamespace );
            }
        }

        // if v is already XML and it's not an XML attribute, just add v to this enode's children
        if( v instanceof ENode ) {
            // in the unusual situation where we have x.set("*", x), we have
            // to copy x before resetting its children
            ENode vcopy = ((ENode)v).copy();
            if( k.toString().equals("*") ) {
                // replace children
                if( v instanceof XMLList ) {
                    for( int i=0; i < this.children.size(); ) {
                        if( this.children.get( i ).node.getNodeType() != Node.ATTRIBUTE_NODE ) {
                            this.children.remove( i );
                        }
                        else {
                            i++;
                        }
                    }
                    this.children.addAll( (XMLList)vcopy );
                    return this;
                }
                this.children = new XMLList();
            }

            // if k is a number, go back one
            if( m.matches() ) {
                if ( this.parent == null ) {
                    this.setAtIndex( Integer.parseInt(k.toString()), vcopy );
                }
                else {
                    int index = this.parent.children.indexOf( this );
                    this.parent.children.remove( this );
                    this.parent.children.add( index, vcopy );
                }
            }
            // non-* strings
            else {
                if( n instanceof XMLList ) {
                    for( ENode r : (XMLList)n ) {
                        this.children.remove( r );
                    }
                }
                this.children.add( vcopy );
            }
            return v;
        }

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
                ENode rep = this instanceof XMLList ? ((XMLList)this).get( ((XMLList)this).size() - 1 ) : this;

                // if k/v doesn't really exist, "get" returns a dummy node, an emtpy node with nodeName = key
                if( n.isDummy() ) {
                    n._dummy = false;
                }
                // otherwise, we need to reset n so we don't replace an existing node
                else if( !n.equals( this ) ) {
                    n = new ENode( this.XML, this.defaultNamespace );
                    n.children = new XMLList();
                }

                ENode attachee = rep.parent;
                // if we have, say, xml.foo[0] = "bar" we have:
                //     - this: empty dummy node (<foo/>), attached to tree above
                //     - n: the node about to be attached (<foo>bar</foo>)
                // too many dummies!
                if( numChildren == 0 ) {
                    attachee.children.remove( this );
                }
                n.node = rep.node.getOwnerDocument().createElement( rep.localName() );
                Node content = rep.node.getOwnerDocument().createTextNode(v.toString());
                n.children.add( new ENode( content, n , null) );
                n.parent = attachee;
                // get the last sibling's position & insert this new one there
                int insertIndex = attachee.children.indexOf( rep ) + 1;
                if( insertIndex == 0 )
                    attachee.children.add( n );
                else
                    attachee.children.add( insertIndex, n );
            }
            // replace an existing element
            else {
                // reset the child list
                n.children = new XMLList();
                NodeList kids = n.node.getChildNodes();
                for( int i=0; kids != null && i<kids.getLength(); i++) {
                    n.node.removeChild(kids.item(i));
                }
                Node content = n.node.getOwnerDocument().createTextNode(v.toString());
                n.appendChild( new ENode( content , n , null ) );
            }
        }
        // k must be a string
        else {
            int index = getInsertionIndex( n );
            
            n = new ENode(this.node.getOwnerDocument().createElement(k.toString()), this , null);
            Node content = this.node.getOwnerDocument().createTextNode(v.toString());
            n.children.add( new ENode( content, n , null ) );
            if( !((List)this.children).contains( n ) ) {
                if( index >= 0 ) {
                    this.children.add( index, n );
                }
                else {
                    this.children.add( n );
                }
            }
        }
        return v;
    }

    private int getInsertionIndex( ENode n ) {
        if( this.children == null )
            this.children = new XMLList();

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
        return index;
    }

    private Object setAtIndex( int index , Object v ) {
        if( index < 0 || index > this.children.size() ) {
            this.children.add( (ENode)v );
        }
        else {
            this.children.remove( index );
            this.children.add( index, (ENode)v );
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
            this.children.add( new ENode(newNode, this , null) );
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
        Object obj = get(o);
        ENode n = obj instanceof ENodeFunction ? ((ENodeFunction)obj).getNode() : (ENode)obj;
        if( n == null ) {
            return false;
        }

        if( ! (n instanceof XMLList) ) {
            return n.parent.children.remove(n);
        }

        for( ENode e : (XMLList)n ) {
            this.children.remove( e );
        }
        if( obj instanceof ENodeFunction ) {
            ((ENodeFunction)obj).unset();
        }

        return true;
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
    public ENode appendChild(ENode child) {
        if( this.children == null )
            this.children = new XMLList();

        child.parent = this;
        this.children.add( child );
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
    public ENode child( Object propertyName ) {
        boolean xmllist = this instanceof XMLList;
        XMLList nodeList = xmllist ? (XMLList)this : this.children;
        Pattern num = Pattern.compile("\\d+(\\.\\d+)?");
        Matcher m = num.matcher(propertyName.toString());
        if( m.matches() ) {
            int i = Integer.parseInt( propertyName.toString() );

            // if we are looking for the 0th element and this isn't an xmllist, we are looking for this
            if( i == 0 && this.node != null ) {
                return this;
            }
            else if( i < nodeList.size() ) 
                return nodeList.get(i);
            else if ( !xmllist || ( xmllist && nodeList.size() >= 1 ) )
                return new ENode( this, this instanceof XMLList ? nodeList.get(0).name.localName : this.name.localName );
            else
                return new ENode();
        }
        else {
            Object obj = this.get(propertyName);
            return ( obj instanceof ENode ) ? (ENode)obj : ((ENodeFunction)obj).cnode;
        }
    }

    /**
     * Returns a number representing the position of this element within its parent.
     */
    public int childIndex() {
        if( parent == null || 
            parent.node.getNodeType() == Node.ATTRIBUTE_NODE || 
            this.node.getNodeType() == Node.ATTRIBUTE_NODE )
            return -1;

        XMLList sibs = parent.children();
        for( int i=0; i<sibs.size(); i++ ) {
            if(sibs.get(i).equals(this))
                return i;
        }
        return -1;
    }

    /**
     * Returns this node's children.
     */
    public XMLList children() {
        if( this.children == null ) {
            children = new XMLList();
            return children;
        }
        XMLList child = new XMLList();
        for( ENode n : this.children ) {
            if( n.node.getNodeType() != Node.ATTRIBUTE_NODE )
                child.add( n );
        }
        return child;
    }

    /**
     * Returns a list of comments, assuming XML.ignoreComments
     * was set to false when the list was created.
     */
    public XMLList comments() {
        return this.children.comments();
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
                return n.prefix;
        }
        return null;
    }

    private String getNamespaceURI( String prefix ) {
        ENode temp = this;
        while( temp != null ) {
            for( Namespace n : temp.inScopeNamespaces ) {
                if( n.prefix != null && n.prefix.equals( prefix ) ) 
                    return n.uri;
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
        if( this.node != null ) {
            short type = this.node.getNodeType();
            if( type == Node.PROCESSING_INSTRUCTION_NODE ||
                type == Node.COMMENT_NODE )
                return false;
        }
        XMLList list = this instanceof XMLList ? (XMLList)this : this.children;
        if( list == null )
            return false;

        for( ENode n : list ) {
            if( n.node.getNodeType() == Node.ELEMENT_NODE )
                return false;
        }
        return true;
    }

    public ArrayList<Namespace> inScopeNamespaces() {
        ArrayList<Namespace> isn = new ArrayList<Namespace>();
        ENode temp = this;
        while( temp != null ) {
            for( Namespace ns : temp.inScopeNamespaces ) {
                if( ! ns.containsPrefix( isn ) )
                    isn.add( ns );
            }
            temp = temp.parent;
        }
        return isn;
    }


    public ENode insertChildAfter(Object child1, ENode child2) {
        return insertChild(child1, child2, 1);
    }

    public ENode insertChildBefore(Object child1, ENode child2) {
        if( child1 == null ) { 
            this.children.add( this.children.size(), child2 );
            return this;
        }
        return insertChild(child1, child2, 0);
    }

    private ENode insertChild( Object child1, ENode child2, int j ) {
        if( this.isSimpleTypeNode() ) 
            return null;

        if( child1 == null ) {
            this.children.add( 0, child2 );
            return this;
        }
        else if ( child1 instanceof ENode ) {
            for( int i=0; i < this.children.size(); i++) {
                if( this.children.get(i).equals( child1 ) ) {
                    this.children.add(i+j, child2);
                    return this;
                }
            }
        }
        return null;
    }

    public String localName() {
        // comments and text nodes don't have local names
        if( this.name == null ) 
            return null;
        return this.name.localName;
    }

    public QName name() {
        return this.name;
    }

    public Namespace namespace() {
        return namespace( null );
    }

    public Namespace namespace( String prefix ) {
        if( prefix == null ) {
            return this.name.getNamespace( this.inScopeNamespaces );
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

    /** Inserts the given child into this object prior to the existing XML properties.
     */

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
            ENode n = this.child(prop.toString());
            return !n.isDummy();
        }
        return false;
    }

    public boolean propertyIsEnumerable() {
        return propertyIsEnumerable( null );
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
        this.set("*", toXML( value ) );
        return this;
    }

    public void setLocalName( Object name ) {
        if( this.node == null ||
            this.node.getNodeType() == Node.TEXT_NODE ||
            this.node.getNodeType() == Node.COMMENT_NODE )
            return;
        this.name.localName = ( name instanceof QName ) ? ((QName)name).localName : name.toString();
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

    public void setNamespace( Object ns) {
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

    public XMLList text() {
        XMLList list = new XMLList();
        if( this.node.getNodeType() == Node.ELEMENT_NODE ) {
            for( ENode child : this.children ) {
                if( child.node.getNodeType() == Node.TEXT_NODE || 
                    child.node.getNodeType() == Node.CDATA_SECTION_NODE ) {
                    list.add( child );
                }
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

        boolean defaultDefined = false;
        ArrayList<Namespace> namespaces = this.namespaceDeclarations();
        if( this.defaultNamespace != null && !ancestors.contains( this.defaultNamespace ) ) {
            namespaces.add( 0, this.defaultNamespace );
            ancestors.add( this.defaultNamespace );
        }

        ArrayList<Namespace> xmlns = new ArrayList<Namespace>();
        Namespace lastXmlns = new Namespace();
        for( int i = 0; i < namespaces.size(); i++ ) {
            Namespace ns = namespaces.get(i);
            if( ( ns.prefix == null || ns.prefix.equals( "" ) ) && ns.uri.equals( "" ) ) 
                continue;

            // if the prefix is null, generate a prefix and display
            // the prefix-less namespace
            if( ns.prefix == null ) {
                xmlns.remove( lastXmlns );
                xmlns.add( ns );
                lastXmlns = new Namespace( "", ns.uri );
                xmlns.add( lastXmlns );
            }
            else if( ns.prefix.equals( "" ) && !defaultDefined ) {
                xmlns.remove( lastXmlns );
                lastXmlns = ns;
                xmlns.add( ns );
            }
            else if( !ns.prefix.equals( "" ) ) {
                xmlns.add( ns );
            }
        }

        for( int i=0; i < xmlns.size(); i++ ) {
            Namespace ns = xmlns.get(i);
            if( ns.prefix == null ) {
                String genPrefix = ns.getPrefix();
                int matchCount = 0;
                for( int j=0; j < i; j++ ) {
                    if( xmlns.get(j).prefix.startsWith( genPrefix ) ) {
                        matchCount++;                            
                    }
                }
                if( matchCount > 0 ) {
                    genPrefix += "-" + matchCount;
                }
                ns.prefix = genPrefix;
                buf.append( " xmlns:" + genPrefix + "=\"" + ns.uri + "\"" );
            }
            else if( ns.prefix.equals( "" ) ) {
                buf.append( " xmlns=\"" + ns.uri + "\"" );
            }
            else { //if( !ns.prefix.equals( "" ) ) {
                buf.append( " xmlns:" + ns.prefix + "=\"" + ns.uri + "\"" );
            }
        }

        // get attrs
        ArrayList<ENode> attr = this.getAttributes();
        String[] attrArr = new String[attr.size()];
        for( int i = 0; i< attr.size(); i++ ) {
            String prefix = "";
            if( !attr.get(i).name.prefix.equals( "" ) )
                prefix = attr.get(i).name.prefix + ":";
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
        if ( this.node == null || this.isSimpleTypeNode() )
            return;

        ArrayList<Namespace> match = this.getNamespaces( n.prefix );
        for( Namespace ns : this.inScopeNamespaces ) {
            if( ( ns.prefix == null && n.prefix == null ) ||
                ( ns.prefix != null && ns.prefix.equals( n.prefix ) ) ) {
                // no duplicates allowed... bug in spidermonkey!
                if( ns.uri.equals( n.uri ) ) {
                    return;
                }
                // if two prefixes are the same, set the old one to null
                else if( ns.prefix != null && !ns.prefix.equals( "" ) ) {
                    ns.prefix = null;
                    break;
                }
            }
        }

        this.inScopeNamespaces.add( n );
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
        if( input == null )
            return null;

        if( input instanceof Boolean ||
            input instanceof Number ||
            input instanceof JSString )
            return toXML(input.toString() );
        else if( input instanceof String )
            return new ENode(this.node.getOwnerDocument().createTextNode((String)input), this , null);
        else if( input instanceof Node )
            return new ENode((Node)input, this, null);
        else if( input instanceof ENode )
            return (ENode)input;
        else
            return null;
    }

    public Set<String> keySet( boolean includePrototype ) {
        XMLList list = ( this instanceof XMLList ) ? (XMLList)this : this.children;
        Set<String> c = new OrderedSet<String>();
        for( int i=0; list != null && i < list.size(); i++ ) {
            c.add( String.valueOf( i ) );
        }
        return c;
    }

    public Collection<ENode> valueSet() {
        XMLList list = ( this instanceof XMLList ) ? (XMLList)this : this.children;
        Collection<ENode> c = new ArrayList<ENode>();
        for( ENode n : list ) {
            c.add( n );
        }
        return c;
    }

    public boolean isDummy() {
        return _dummy;
    }

    private static ENode textNodeFactory( String s ) {
        ENode n = new ENode();
        n.init( s );
        return n;
    }

    private static ENode getENode( Scope s ) {
        Object obj = s.getThis();
        if ( obj instanceof ENode ) 
            return (ENode)obj;
        else if ( obj instanceof ENodeFunction ) 
            return ((ENodeFunction)obj).cnode;

        // should only do this for XML.prototype
        return textNodeFactory( "" );
    }

    private static Object getOneArg( Object foo[] ) {
        if( foo.length == 0 ) 
            throw new RuntimeException( "This method requires one argument." );
        return foo[0];
    }

    private static Object[] getTwoArgs( Object foo[] ) {
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
            Object removed = cnode.removeField( f );
            Object obj = cnode.get( f );

            if( cnode instanceof XMLList ) {
                ((XMLList)cnode).remove( obj );
            }
            else if( cnode.equals( obj ) ) {
                cnode = null;
            }

            return removed;
        }

        public void unset() {
            cnode = null;
        }

        public void setup( Object something, ENode thiz, String tagname ) {
            if( something != null ) {
                cnode = (ENode)something;
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


    private XMLList children;
    private ENode parent;
    public Node node;

    private boolean _dummy;
    private ArrayList<Namespace> inScopeNamespaces = new ArrayList<Namespace>();
    private QName name;

    public Namespace defaultNamespace;

    private boolean _new = false;
}
