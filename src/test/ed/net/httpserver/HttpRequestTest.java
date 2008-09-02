// HttpRequestTest.java

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

import org.testng.annotations.Test;

import ed.*;

public class HttpRequestTest extends TestCase {
    
    @Test(groups = {"basic"})    
    public static void testGetParams(){
        HttpRequest r = HttpRequest.getDummy( "/crazy/me?a=1&b=2&b=3" );
        assertEquals( "1" , r.getParameter( "a" ) );
        assertEquals( "1" , r.getURLParameter( "a" ) );
        assertEquals( "1" , r.getURLParameter( "a" , "asdsad" ) );
        assertEquals( "Z" , r.getURLParameter( "c" , "Z" ) );
        assertNull( r.getPostParameter( "a" ) );

        assertEquals( 2 , r.getParameters( "b" ).size() );
        assertEquals( "2" , r.getParameter( "b" ) );
        assertEquals( "2" , r.getParameters( "b" ).get(0).toString() );
        assertEquals( "3" , r.getParameters( "b" ).get(1).toString() );
        assertNull( r.getParameters( "b" ).get(2) );

        assertEquals( 2 , r.getParameterNames().size() );
    }

    @Test(groups = {"basic"})    
    public static void testPostParams(){
        HttpRequest r = HttpRequest.getDummy( "/crazy/me?a=1&b=2&b=3&c=Z" );
        
        r._addParm( "c" , "Y" , true );
        
        assertEquals( "Y" , r.getPostParameter( "c" ) );
        assertEquals( "Y" , r.getParameter( "c" ) );
        assertEquals( "Z" , r.getURLParameter( "c" ) );

        assertEquals( 3 , r.getParameterNames().size() );
        assertEquals( 1 , r.getPostParameterNames().size() );
        assertEquals( 3 , r.getURLParameterNames().size() );
    }
    
    @Test(groups = {"basic"})    
    public static void testJSInterface(){
        HttpRequest r = HttpRequest.getDummy( "/crazy/me?a=2" );        
        assertEquals( "2" , r.get( "a" ).toString() );
        r.set( "a" , "3" );
        assertEquals( "3" , r.get( "a" ).toString() );
    }    


    @Test(groups = {"basic"})    
    public static void testPaths(){
        HttpRequest r = HttpRequest.getDummy( "/crazy/me?a=2" );        
        assertEquals( "/crazy/" , r.getDirectory() );

        r = HttpRequest.getDummy( "/crazy/me/?a=2" );        
        assertEquals( "/crazy/me/" , r.getDirectory() );

        r = HttpRequest.getDummy( "/crazy?a=2" );        
        assertEquals( "/" , r.getDirectory() );

    }    

    @Test(groups = {"basic"})
    public static void testRandom(){
        // yes, this shouldn't be here.

        HttpRequest r = HttpRequest.getDummy( "/?f=data" , "Referer: http://blah.com/asda" );
        assertEquals( "http://blah.com/asda" , r.getReferer() );
        assertEquals( "/asda" , r.getRefererNoHost() );

        r = HttpRequest.getDummy( "/?f=data" , "Referer: /asda" );
        assertEquals( "/asda" , r.getReferer() );
        assertEquals( "/asda" , r.getRefererNoHost() );
    }

    @Test(groups = {"basic"})
    public static void testMonitorGetFilter(){
        // yes, this shouldn't be here.

        HttpRequest r = HttpRequest.getDummy( "/?f=data" );        
        assertEquals( "data" , HttpMonitor.ThreadMonitor.getFilter( r ) );
        
        r = HttpRequest.getDummy( "/?a=data" );        
        assertEquals( null , HttpMonitor.ThreadMonitor.getFilter( r ) );

        r = HttpRequest.getDummy( "/?a=data" , "Host: www.alleyinsider.com" );        
        assertEquals( "alleyinsider" , HttpMonitor.ThreadMonitor.getFilter( r ) );

        r = HttpRequest.getDummy( "/?a=data" , "Host: www.10gen.com" );        
        assertEquals( "www" , HttpMonitor.ThreadMonitor.getFilter( r ) );

        r = HttpRequest.getDummy( "/?a=data" , "Host: iad-sb-n7.10gen.cc" );        
        assertEquals( null , HttpMonitor.ThreadMonitor.getFilter( r ) );
        r = HttpRequest.getDummy( "/?a=data" , "Host: iad-sb-n7.10gen.cc:8080" );        
        assertEquals( null , HttpMonitor.ThreadMonitor.getFilter( r ) );
        
    }


    @Test(groups = {"basic"})
    public static void testSetGet(){

        HttpRequest r = HttpRequest.getDummy( "/?f=data" );        
	assertEquals( "data" , r.get( "f" ).toString() );
	assertEquals( "data" , r.getURLParameter( "f" ) );
	
	r.set( "f" , "a" );
	assertEquals( "data" , r.getURLParameter( "f" ) );
	assertEquals( "a" , r.get( "f" ).toString() );

	assertEquals( 1 , r.keySet().size() );
	r.set( "z" , 5 );
	assertEquals( 5 , r.get( "z" ) );
	assertEquals( 2 , r.keySet().size() );

	assertEquals( 2 , r.getAttributes().keySet().size() );
	assertEquals( 5 , r.getAttributes().get( "z" ) );
    }

    @Test(groups = {"basic"})
    public static void testObjInt(){

        HttpRequest r = HttpRequest.getDummy( "/?f=data" );        
	assertEquals( "data" , r.get( "f" ).toString() );
	assertEquals( "data" , r.getURLParameter( "f" ) );
	assertEquals( "data" , r.getURLParameters().get( "f" ) );

    }


    public static void main( String args[] ){
        (new HttpRequestTest()).runConsole();
    }

}
