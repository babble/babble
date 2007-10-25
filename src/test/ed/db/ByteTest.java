// ByteTest.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.*;
import ed.js.*;

public class ByteTest extends TestCase {

    public void testNumber(){
        ByteEncoder encoder = new ByteEncoder();
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        
        assertEquals( 1 + 5 + 8 , encoder.put( buf , "eliot" , 5 ) );
        assertEquals( 1 + 5 + 8 , buf.position() );
    }

    public void testString(){
        ByteEncoder encoder = new ByteEncoder();
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );

        assertEquals( 1 + 5  + 6 , encoder.put( buf , "eliot" , "asdasd" ) );
        assertEquals( 1 + 5 + 6 , buf.position() );
    }

    public static void main( String args[] ){
        (new ByteTest()).runConsole();
    }
}
