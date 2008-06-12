// HttpRequestTest.java

package ed.net.httpserver;

import org.testng.annotations.Test;

import ed.*;

public class HttpRequestTest extends TestCase {
    
    public static void testGetParams(){
        HttpRequest r = HttpRequest.getDummy( "/crazy/me?a=1&b=2&b=3" );
        assertEquals( "1" , r.getParameter( "a" ) );
        assertEquals( "1" , r.getGetParmeter( "a" ) );
        assertEquals( "1" , r.getGetParmeter( "a" , "asdsad" ) );
        assertEquals( "Z" , r.getGetParmeter( "c" , "Z" ) );
        assertNull( r.getPostParmeter( "a" ) );

        assertEquals( 2 , r.getParameters( "b" ).size() );
        assertEquals( "2" , r.getParameter( "b" ) );
        assertEquals( "2" , r.getParameters( "b" ).get(0).toString() );
        assertEquals( "3" , r.getParameters( "b" ).get(1).toString() );
        assertNull( r.getParameters( "b" ).get(2) );
    }


    public static void main( String args[] ){
        (new HttpRequestTest()).runConsole();
    }

}
