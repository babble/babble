// ByteTest.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.*;
import ed.js.*;

public class ByteTest extends TestCase {

    public void testNumber1(){
        ByteEncoder encoder = new ByteEncoder();
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        
        encoder.putNumber( buf , "eliot" , 5 );

        buf.flip();
        
        JSObject o = new JSObjectBase();
        ByteDecoder decoder = new ByteDecoder();
        decoder.decodeNext( buf , o );
        assertEquals( 5.0 , ((Double)o.get( "eliot" )).doubleValue() );
        
    }

    public void testNumber2(){
        ByteEncoder encoder = new ByteEncoder();
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        
        encoder.putNumber( buf , "eliot" , 5 );
        encoder.putNumber( buf , "something" , 123.123 );

        buf.flip();
        
        JSObject o = new JSObjectBase();
        ByteDecoder decoder = new ByteDecoder();
        decoder.decodeNext( buf , o );
        decoder.decodeNext( buf , o );

        assertEquals( 5.0 , ((Double)o.get( "eliot" )).doubleValue() );
        assertEquals( 123.123 , ((Double)o.get( "something" )).doubleValue() );
        
    }

    public void testString(){
        ByteEncoder encoder = new ByteEncoder();
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );

        encoder.putString( buf , "eliot" , "s1" );
        encoder.putString( buf , "something" , "hello" );

        buf.flip();
        
        JSObject o = new JSObjectBase();
        ByteDecoder decoder = new ByteDecoder();

        decoder.decodeNext( buf , o );
        decoder.decodeNext( buf , o );
        
        assertEquals( "s1" , o.get( "eliot" ).toString() );
        assertEquals( "hello" , o.get( "something" ).toString() );
    }

    public static void main( String args[] ){
        (new ByteTest()).runConsole();
    }
}
