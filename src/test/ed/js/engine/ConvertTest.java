// ConvertTest.java

package ed.js.engine;

import ed.*;
import static ed.js.engine.Convert.*;

public class ConvertTest extends TestCase {

    public void testFoo(){
        Object a = 5;
        Object b = 6;
    }

    public void testConvertStatement(){
        assertClose( "Object a = 5;" , convertStatement( "var a = 5;" ) );
        assertClose( "return null;" , convertStatement( "return ;" ) );
        assertClose( "return null;" , convertStatement( "return;" ) );
        assertClose( "return null;" , convertStatement( "return; " ) );
    }

    public static void main( String args[] ){
        TestCase.run( args );
    }
}
