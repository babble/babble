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
        buf.order( ByteOrder.LITTLE_ENDIAN );
        
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
        buf.order( ByteOrder.LITTLE_ENDIAN );
        
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
        buf.order( ByteOrder.LITTLE_ENDIAN );

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

    public void testObjectId(){
        ByteEncoder encoder = new ByteEncoder();
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        buf.order( ByteOrder.LITTLE_ENDIAN );

        ObjectId id = ObjectId.get();

        encoder.putString( buf , "eliot" , "s1" );
        encoder.putObjectId( buf , "_id" , id );
        encoder.putString( buf , "something" , "hello" );


        buf.flip();
        
        JSObject o = new JSObjectBase();
        ByteDecoder decoder = new ByteDecoder();

        decoder.decodeNext( buf , o );
        decoder.decodeNext( buf , o );
        decoder.decodeNext( buf , o );
        
        assertEquals( "s1" , o.get( "eliot" ).toString() );
        assertEquals( "hello" , o.get( "something" ).toString() );
        assertEquals( id , o.get( "_id" ) );
    }

    public void testObject1(){
        ByteEncoder encoder = new ByteEncoder();
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        buf.order( ByteOrder.LITTLE_ENDIAN );
        
        JSObject o = new JSObjectBase();
        o.set( "eliot" , "horowitz" );
        o.set( "num" , 517 );
        
        encoder.putObject( buf , null , o );
        
        buf.flip();
        
        ByteDecoder decoder = new ByteDecoder();
        JSObject read = decoder.readObject( buf );
        
        assertEquals( "horowitz" , read.get( "eliot" ).toString() );
        assertEquals( 517.0 , ((Double)read.get( "num" )).doubleValue() );
        
        assertEquals( buf.limit() , buf.position() );
    }

    public void testObject2(){
        ByteEncoder encoder = new ByteEncoder();
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        buf.order( ByteOrder.LITTLE_ENDIAN );
        
        JSObject o = new JSObjectBase();
        o.set( "eliot" , "horowitz" );
        o.set( "num" , 517 );
        o.set( "z" , "y" );
        o.set( "asd" , null );
        
        JSObject o2 = new JSObjectBase();
        o2.set( "a" , "b" );
        o2.set( "b" , "a" );
        o.set( "next" , o2 );
        
        encoder.putObject( buf , null , o );
        
        buf.flip();
        
        ByteDecoder decoder = new ByteDecoder();
        JSObject read = decoder.readObject( buf );
        
        assertEquals( "horowitz" , read.get( "eliot" ).toString() );
        assertEquals( 517.0 , ((Double)read.get( "num" )).doubleValue() );
        assertEquals( "b" , ((JSObject)read.get( "next" ) ).get( "a" ).toString() );
        assertEquals( "a" , ((JSObject)read.get( "next" ) ).get( "b" ).toString() );
        assertEquals( "y" , read.get( "z" ).toString() );
        assertEquals( o.keySet().size() , read.keySet().size() );

        assertEquals( buf.limit() , buf.position() );
    }

    public void testArray1(){
        ByteEncoder encoder = new ByteEncoder();
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        buf.order( ByteOrder.LITTLE_ENDIAN );
        
        JSObject o = new JSObjectBase();
        o.set( "eliot" , "horowitz" );
        o.set( "num" , 517 );
        o.set( "z" , "y" );
        o.set( "asd" , null );
        o.set( "myt" , true );
        o.set( "myf" , false );
        
        JSArray a = new JSArray();
        a.set( "" , "A" );
        a.set( "" , "B" );
        a.set( "" , "C" );
        o.set( "a" , a );

        o.set( "d" , new JSDate() );
        o.set( "r" , new JSRegex( "\\d+" , "i" ) );

        encoder.putObject( buf , null , o );
        
        buf.flip();
        
        ByteDecoder decoder = new ByteDecoder();
        JSObject read = decoder.readObject( buf );
        
        assertEquals( "horowitz" , read.get( "eliot" ).toString() );
        assertEquals( 517.0 , ((Double)read.get( "num" )).doubleValue() );
        assertEquals( "y" , read.get( "z" ).toString() );
        assertEquals( o.keySet().size() , read.keySet().size() );
        assertEquals( 3 , a.size() );
        assertEquals( a.size() , ((JSArray)read.get( "a" ) ).size() );
        assertEquals( "A" , ((JSArray)read.get( "a" ) ).get( 0 ).toString() );
        assertEquals( "B" , ((JSArray)read.get( "a" ) ).get( 1 ).toString() );
        assertEquals( "C" , ((JSArray)read.get( "a" ) ).get( 2 ).toString() );
        assertEquals( ((JSDate)o.get("d")).getTime() , ((JSDate)read.get("d")).getTime() );
        assertEquals( true , (Boolean)o.get("myt") );
        assertEquals( false , (Boolean)o.get("myf") );
        assertEquals( o.get( "r" ).toString() , read.get("r").toString() );

        assertEquals( buf.limit() , buf.position() );
    }
    
    public static void main( String args[] ){
        (new ByteTest()).runConsole();
    }
}
