// XMLUtil.java

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

package ed.util;

import java.io.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class XMLUtil {
    
    public static Document parse( File f )
        throws SAXException , IOException {
        return getBuilder().parse( f );
    }

    public static Document parse( InputStream in )
        throws SAXException , IOException  {
        return getBuilder().parse( in );
    }

    public static Document parse( String s )
        throws SAXException , IOException {
        return parse( new StringBufferInputStream( s ) );
    }
    
    public static DocumentBuilder getBuilder(){
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();   
        }
        catch ( ParserConfigurationException pce ){
            throw new RuntimeException( "can't create parser?" + pce );
        }
    }

    public static String toString( Node n ){
        StringBuilder buf = new StringBuilder();
        append( n , buf , 0 );
        return buf.toString();
    }

    public static StringBuilder append( Node n , StringBuilder buf , int level ){
        if ( n instanceof CharacterData )
            return _level( buf , level ).append( n.getNodeValue() ).append( "\n" );
        
        _level( buf , level ).append( "<" ).append( n.getNodeName() );
        NamedNodeMap attr = n.getAttributes();
        if ( attr != null ){
            for ( int i=0; i<attr.getLength(); i++ ){
                Node a = attr.item(i);
                buf.append( " " ).append( a.getNodeName() ).append( "=\"" ).append( a.getNodeValue() ).append( "\" " );
            }
        }
        
        NodeList children = n.getChildNodes();
        if ( children == null || children.getLength() == 0 )
            return buf.append( "/>\n" );
        buf.append( ">\n" );
        
        for ( int i=0; i<children.getLength(); i++ ){
            Node c = children.item(i);
            append( c , buf , level + 1 );
        }
        
        return _level( buf , level ).append( "</" ).append( n.getNodeName() ).append( ">\n" );
    }
    
    static StringBuilder _level( StringBuilder buf , int level ){
        for ( int i=0; i<level; i++ )
            buf.append( " " );
        return buf;
    }
}
