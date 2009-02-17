// HTMLUtil.java

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
