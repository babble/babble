// HttpServerTest.java

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

package ed.net.httpserver;

import java.io.*;
import java.net.*;
import java.util.*;

import org.testng.annotations.Test;

import ed.*;
import ed.io.*;
import ed.util.*;

public class HttpServerTest extends TestCase {

    HttpServerTest()
	throws IOException {
	_server = new HttpServer( _port );
	_server.addHandler( new PingHandler() );
	_server.start();
    }
    
    public void testBasic1()
	throws IOException {
	
	Socket s = open();
	s.getOutputStream().write( headers( "GET" , "" , "Connection: Close\r\n" ).toString().getBytes() );
	InputStream in = s.getInputStream();
	Response r = read( in );
	assertEquals( PingHandler.DATA , r.body );
    }

    public void testKeepAlive1()
	throws IOException {
	
	Socket s = open();
	
	OutputStream out = s.getOutputStream();
	InputStream in = s.getInputStream();
	
	out.write( headers( "GET" , "" , "Connection: Keep-Alive\r\n" ).toString().getBytes() );
	Response r = read( in );
	assertEquals( PingHandler.DATA , r.body );

	out.write( headers( "GET" , "" , "Connection: Keep-Alive\r\n" ).toString().getBytes() );
	r = read( in );
	assertEquals( PingHandler.DATA , r.body );

	out.write( headers( "GET" , "" , "Connection: Keep-Alive\r\n" ).toString().getBytes() );
	r = read( in );
	assertEquals( PingHandler.DATA , r.body );

	out.write( headers( "GET" , "" , "Connection: Keep-Alive\r\n" ).toString().getBytes() );
	r = read( in );
	assertEquals( PingHandler.DATA , r.body );

	out.write( headers( "GET" , "" , "Connection: Keep-Alive\r\n" ).toString().getBytes() );
	r = read( in );
	assertEquals( PingHandler.DATA , r.body );

	out.write( headers( "GET" , "" , "Connection: Close\r\n" ).toString().getBytes() );
	r = read( in );
	assertEquals( PingHandler.DATA , r.body );

	assert( in.read() == -1 );
    }


    public void testPipeLine1()
	throws IOException {
	
	int num = 5;

	StringBuilder buf = new StringBuilder();
	for ( int i=0; i<num; i++ )
	    buf.append( headers( "GET" , "" , "Connection: Keep-Alive\r\n" ) );

	Socket s = open();
	Response r = null;

	OutputStream out = s.getOutputStream();
	InputStream in = s.getInputStream();
	
	out.write( buf.toString().getBytes() );
	for ( int i=0; i<5; i++ ){
	    r = read( in );
	    assertEquals( PingHandler.DATA , r.body );
	}

	out.write( headers( "GET" , "" , "Connection: Close\r\n" ).toString().getBytes() );
	r = read( in );
	assertEquals( PingHandler.DATA , r.body );

	assert( in.read() == -1 );
    }
    
    Socket open()
	throws IOException {
	return new Socket( "127.0.0.1" , _port );
    }

    StringBuilder headers( String method , String params , String headers ){
	StringBuilder buf = new StringBuilder();
	buf.append( method ).append( " /~ping?" ).append( params ).append( " HTTP/1.1\r\n" );
	buf.append( "Host: localhost\r\n" );
	buf.append( headers );
	buf.append( "\r\n" );
	return buf;
    }

    Response read( InputStream in )
	throws IOException {
	
	StringBuilder buf = new StringBuilder();
	
	int lineCount = 0;
	while ( true ){
	    int thing = in.read();
	    if ( thing < 0 )
		throw new RuntimeException( "ran out of stuff" );
	    
	    char c = (char)thing;
	    if ( c == '\r' )
		continue;
	    
	    if ( c != '\n' ){
		lineCount++;
		buf.append( c );
		continue;
	    }
	    
	    if ( lineCount == 0 )
		break;
	    
	    buf.append( c );
	    lineCount = 0;
	}
	
	String firstLine = null;
	Map<String,String> m = new StringMap<String>();
	
	for ( String line : buf.toString().trim().split( "\n" ) ){
	    line = line.trim();
	    if ( line.length() == 0 )
		continue;

	    if ( m.size() == 0 ){
		firstLine = line;
		m.put( "FIRST_LINE" , line );
		continue;
	    }
		       
	    int idx = line.indexOf( ":" );
	    m.put( line.substring( 0 , idx ).trim() , 
		   line.substring( idx + 1 ).trim() );
	}
	
	byte[] data;
	if ( m.get( "Content-Length" ) != null ){
	    data = new byte[Integer.parseInt( m.get( "Content-Length" ) )];
	    in.read( data );
	}
	else {
	    data = StreamUtil.readBytesFully( in );
	}
	
	return new Response( firstLine , m , data );
    }
    
    protected void finalize(){
	_server.stopServer();
    }
    
    class Response {
	
	Response( String fl , Map<String,String> h , byte[] data ){
	    firstLine = fl;
	    headers = h;
	    body = new String( data );
	}
	
	public String toString(){
	    return firstLine + " headers:" + headers + " [" + body + "]";
	}

	final String firstLine;
	final String body;
	final Map<String,String> headers;
    }

    HttpServer _server;
    final int _port = 15123;

    public static void main( String args[] )
	throws IOException {
        (new HttpServerTest()).runConsole();
    }
}
