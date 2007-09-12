// ConvertTest.java

package ed.js.engine;

import ed.*;

public class ConvertTest extends TestCase {

    public void testFoo(){
        throw new RuntimeException( "yo" );
    }

    public void testBar(){
    }

    public void testGoo(){
        assertEquals( 5 , 6 );
    }

    public static void main( String args[] ){
        TestCase.run( args );
    }
}
