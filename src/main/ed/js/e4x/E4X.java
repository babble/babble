// E4X.java

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

public class E4X {

    static XMLList _nodeGet( ENode start , Object s ){
        if( start instanceof XMLList ) {
            return _nodeGet( (XMLList)start, s );
        }
        return _nodeGet( new XMLList( start ), s );
    }

    static XMLList _attrNodeGet( ENode start , Object s ){
        if( start instanceof XMLList ) {
            return _attrNodeGet( (XMLList)start, s );
        }
        return _attrNodeGet( new XMLList( start ), s );
    }

    static XMLList _attrNodeGet( XMLList start, Object obj ) {
        String s = obj instanceof QName ?
            ((QName)obj).localName.toString() :
            obj.toString();

        final boolean search = s.startsWith( ".." );
        if ( search )
            s = s.substring(2);

        if( s.startsWith( "@" ) )
            s = s.substring( 1 );

        final boolean all = s.endsWith("*");
        if( all ) {
            if( s.length() > 1) return null;
            s = "";
        }

        final boolean qualified = (obj instanceof QName);
        String uri = qualified ? ((QName)obj).uri + "" : "";

        List<ENode> traverse = new LinkedList<ENode>();
        XMLList res = new XMLList();

        for(int k=0; k< start.size(); k++) {
            traverse.add( start.get(k) );

            while ( ! traverse.isEmpty() ){
                ENode n = traverse.remove(0);

                ArrayList<ENode> nnm = n.getAttributes();
                for( ENode enode : nnm ) {
                    if( all || 
                        ( ( ( qualified && 
                              ( enode.name().uri != null && 
                                enode.name().uri.equals( uri ) ) ) || 
                            !qualified ) && 
                          enode.localName().equals( s ) ) ) {
                        res.add( enode );
                    }
                }
            
                XMLList kids = n.children();
                if ( kids == null || kids.size() == 0 )
                    continue;

                for ( int i=0; i<kids.size(); i++ ){
                    ENode c = kids.get(i);
                    if ( search )
                        traverse.add( c );
                }
            }
        }
        return res;
    }

    static XMLList _nodeGet( XMLList start , Object obj ){
        String s = obj instanceof QName ?
            ((QName)obj).localName.toString() :
            obj.toString();

        final boolean search = s.startsWith( ".." );
        if ( search )
            s = s.substring(2);

        final boolean all = s.endsWith("*");
        if( all ) {
            if( s.length() > 1) return null;
            s = "";
        }

        final boolean qualified = (obj instanceof QName);
        String uri = qualified ? ((QName)obj).uri.toString() : "";

        List<ENode> traverse = new LinkedList<ENode>();
        XMLList res = new XMLList();
    
        for(int k=0; k< start.size(); k++) {
            traverse.add( start.get(k) );

            while ( ! traverse.isEmpty() ){
                ENode n = traverse.remove(0);
            
                XMLList kids = n.children();
                if ( kids == null || kids.size() == 0 )
                    continue;

                for ( int i=0; i<kids.size(); i++ ){
                    ENode c = kids.get(i);

                    if ( c.node.getNodeType() != Node.ATTRIBUTE_NODE &&
                         ( all || 
                           ( ( c.node.getNodeType() == Node.TEXT_NODE && 
                               c.text().equals( s ) ) || 
                             c.node.getNodeType() != Node.TEXT_NODE ) &&
                           ( ( ( qualified && 
                                 c.name().uri.equals( uri ) ) || 
                               !qualified ) && 
                             ( c.localName() != null && 
                               c.localName().equals( s ) ) ) ) ) {
                        res.add( c );
                    }

                    if ( search )
                        traverse.add( c );
                }
            }
        }
        return res;
    }

    /*    static Object _handleListReturn( List<ENode> lst ){
        if ( lst.size() == 0 )
            return null;

        if ( lst.size() == 1 ){
            return lst.get(0);
        }
        return new XMLList(lst);
        }*/

    public static boolean isXMLName( String name ) {
        Pattern invalidChars = Pattern.compile("[@\\s\\{\\/\\']|(\\.\\.)|(\\:\\:)");
        Matcher m = invalidChars.matcher( name );
        if( m.find() ) {
            return false;
        }
        return true;
    }

    public static String escapeElementValue( String s ) {
        s = s.replaceAll( "&", "&amp;" );
        s = s.replaceAll( "<", "&lt;" );
        s = s.replaceAll( ">", "&gt;" );
        return s;
    }

    public static String escapeAttributeValue( String s ) {
        s = s.replaceAll( "&", "&amp;" );
        s = s.replaceAll( "\"", "&quot;" );
        s = s.replaceAll( ">", "&gt;" );

        s = s.replaceAll( "\\u000A", "&#xA;" );
        s = s.replaceAll( "\\u000D", "&#xD;" );
        s = s.replaceAll( "\u0009", "&#x9;" );
        return s;
    }

    public static XMLList addNodes(ENode a, ENode b) {
        if( ( a instanceof XMLList || a.node == null ) && b instanceof XMLList) {
            ((XMLList)a).addAll( (XMLList)b );
            return (XMLList)a;
        }
        else if ( a instanceof XMLList || a.node == null ) {
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

    /**
     * Given an ENode or ENodeFunction, this returns the ENode.
     * This is useful for handling getter output.
     */
    public static ENode getENode( Object o ) {
        if( o instanceof ENode.ENodeFunction ) {
            return ((ENode.ENodeFunction)o).getNode();
        }
        else {
            return (ENode)o;
        }
    }
    
    public static boolean isXMLName( Object o ) {
        if( o == null )
            return false;

        QName q = new QName( o );
        if( q.localName.toString().matches( "[A-Za-z_][\\w\\.\\-]*" ) )
            return true;
        return false;
    }

    public static QName toXMLName( Object o ) {
        if( o instanceof QName ) {
            return (QName)o;
        }
        else {
            String name = o.toString();
            // not quite spec
            return new QName( name.charAt(0) == '@' ? name.substring( 1 ) : name );
        }
    }

    public static XMLList toXMLList( Object o ) {
        if( o instanceof XMLList ) 
            return (XMLList)o;
        else if( o instanceof ENode ) {
            return new XMLList( (ENode)o );
        }
        else return new XMLList();
    }
}
