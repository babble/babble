// ReplayTest.java

package ed.net.httpserver;

import org.testng.annotations.Test;

import ed.*;

public class ReplayTest extends TestCase {

    @Test(groups = {"basic"})
    public void testHeaders(){
        Replay r = new Replay( null , 0 , "www.shopwiki.com" );
        
        assertEquals( "GET /\nConnection: close\nHost: www.shopwiki.com\n\n" ,
                      r.fixHeaders( "GET /\nConnection: asd\nHost: asdasd\n\n" ) );

        assertEquals( "GET /\nConnection: close\nHost: www.shopwiki.com\nA: B\n\n" ,
                      r.fixHeaders( "GET /\nConnection: asd\nHost: asdasd\nA: B\n\n" ) );

        assertEquals( "GET /\nConnection: close\nHost: www.shopwiki.com\n\n" ,
                      r.fixHeaders( "GET /\nConnection: asd\n\n" ) );
    }

    public static void main( String args[] ){
        (new ReplayTest()).runConsole();
    }
}
