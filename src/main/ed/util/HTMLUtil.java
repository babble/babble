// HTMLUtil.java

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
import java.net.*;

import org.cyberneko.html.parsers.DOMParser;

import org.w3c.dom.*;
import org.xml.sax.*;

import ed.net.httpclient.*;

public class HTMLUtil {

    public static Document parse( URL url )
	throws IOException {
	HTMLParserHandler h = new HTMLParserHandler();
	HttpClient.download( url , h );
	return h._doc;
    }
    
    static class HTMLParserHandler extends HttpResponseHandlerBase {
	
	public int read( InputStream in )
	    throws IOException {
	    try {
		_doc = parse( in );
	    }
	    catch ( SAXException e ){
		throw new IOException( "can't parse : " + e );
	    }
	    return 0;
	}	
	
	Document _doc;
    }
    
    public static Document parse( String s )
        throws SAXException , IOException {
        return parse( new StringBufferInputStream( s ) );
    }

    public static Document parse( File f )
        throws SAXException , IOException {
        return parse( new FileInputStream( f ) );
    }
    
    public static Document parse( InputStream in )
        throws SAXException , IOException {
	DOMParser p = getParser();
	p.parse( new InputSource( in ) );
	return p.getDocument();
    }

    public static DOMParser getParser(){
	return new DOMParser();
    }

}
