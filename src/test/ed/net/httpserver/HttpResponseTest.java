// HttpResponseTest.java

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

import javax.servlet.http.*;

import org.testng.annotations.Test;

import ed.*;
import ed.js.*;
import static ed.js.JSInternalFunctions.*;

public class HttpResponseTest extends TestCase {

    @Test
    public void testCookieParing1(){
        JSObject o = 
            JS_buildLiteralObject( new String[]{ "name" , "value" } , 
                                   new Object[]{ "eliot" , "hello" } );
        
        Cookie c = HttpResponse.objectToCookie( o );
        assertEquals( "eliot" , c.getName() );
        assertEquals( "hello" , c.getValue() );
        assertFalse( c.getSecure() );
    }

    @Test
    public void testCookieParing2(){
        JSObject o = 
            JS_buildLiteralObject( new String[]{ "name" , "value" , "domain" , "path" , "secure" } , 
                                   new Object[]{ "eliot" , "hello" , "blah.com" , "/abc" , 5 } );
        
        Cookie c = HttpResponse.objectToCookie( o );
        assertEquals( "eliot" , c.getName() );
        assertEquals( "hello" , c.getValue() );
        assertEquals( "blah.com" , c.getDomain() );
        assertEquals( "/abc" , c.getPath() );
        assert( c.getSecure() );
    }


    @Test
    public void testCookieParing3(){
        JSObject o = 
            JS_buildLiteralObject( new String[]{ "name" , "value" , "expires" } , 
                                   new Object[]{ "eliot" , "hello" , 1 } );
        
        Cookie c = HttpResponse.objectToCookie( o );
        assertEquals( "eliot" , c.getName() );
        assertEquals( "hello" , c.getValue() );
        assertFalse( c.getSecure() );
        assertEquals( 86400 , c.getMaxAge() );

        o = 
            JS_buildLiteralObject( new String[]{ "name" , "value" , "expires" } , 
                                   new Object[]{ "eliot" , "hello" , new java.util.Date( System.currentTimeMillis() + 5500 ) } );
        
        c = HttpResponse.objectToCookie( o );
        {
            long ma = c.getMaxAge();
            assertTrue( ma >= 4 && ma <= 6 );
        }

        o = 
            JS_buildLiteralObject( new String[]{ "name" , "value" , "expires" } , 
                                   new Object[]{ "eliot" , "hello" , new JSDate( System.currentTimeMillis() + 5000 ) } );
        
        c = HttpResponse.objectToCookie( o );
        assertEquals( 5 , c.getMaxAge() );

    }

    public static void main( String args[] ){
        (new HttpResponseTest()).runConsole();
    }
}
