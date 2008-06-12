// HttpRequestTest.java

package ed.net.httpserver;

import org.testng.annotations.Test;

import ed.*;

public class HttpRequestTest extends TestCase {
    
    @Test(groups = {"basic"})    
    public static void testGetParams(){
        HttpRequest r = HttpRequest.getDummy( "/crazy/me?a=1&b=2&b=3" );
        assertEquals( "1" , r.getParameter( "a" ) );
        assertEquals( "1" , r.getURLParmeter( "a" ) );
        assertEquals( "1" , r.getURLParmeter( "a" , "asdsad" ) );
        assertEquals( "Z" , r.getURLParmeter( "c" , "Z" ) );
        assertNull( r.getPostParmeter( "a" ) );

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
        
        assertEquals( "Y" , r.getPostParmeter( "c" ) );
        assertEquals( "Y" , r.getParameter( "c" ) );
        assertEquals( "Z" , r.getURLParmeter( "c" ) );

        assertEquals( 3 , r.getParameterNames().size() );
        assertEquals( 1 , r.getPostParameterNames().size() );
        assertEquals( 3 , r.getURLParameterNames().size() );
    }

    public static void main( String args[] ){
        (new HttpRequestTest()).runConsole();
    }

}
