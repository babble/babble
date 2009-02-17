// XMLUtil.java

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
