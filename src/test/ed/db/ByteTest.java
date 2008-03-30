// ByteTest.java

package ed.db;

import ed.*;
import ed.js.*;
import org.testng.annotations.Test;

public class ByteTest extends TestCase {

    @Test(groups = {"basic"})
    public void testObject1(){
        ByteEncoder encoder = ByteEncoder.get();
        
        JSObject o = new JSObjectBase();
        o.set( "eliot" , "horowitz" );
        o.set( "num" , 517 );
        
        encoder.putObject( null , o );
        
        encoder.flip();
        
        ByteDecoder decoder = new ByteDecoder( encoder._buf );
        JSObject read = decoder.readObject();
        
        assertEquals( "horowitz" , read.get( "eliot" ).toString() );
        assertEquals( 517.0 , ((Double)read.get( "num" )).doubleValue() );
        
        assertEquals( encoder._buf.limit() , encoder._buf.position() );
    }

    @Test(groups = {"basic"})
    public void testString()
        throws Exception {
        ByteEncoder encoder = ByteEncoder.get();
        
        String eliot = java.net.URLDecoder.decode( "horowitza%C3%BCa" , "UTF-8" );

        JSObject o = new JSObjectBase();
        o.set( "eliot" , eliot );
        o.set( "num" , 517 );
        
        encoder.putObject( null , o );
        
        encoder.flip();
        
        ByteDecoder decoder = new ByteDecoder( encoder._buf );
        JSObject read = decoder.readObject();
        
        assertEquals( eliot , read.get( "eliot" ).toString() );
        assertEquals( 517.0 , ((Double)read.get( "num" )).doubleValue() );
        
        assertEquals( encoder._buf.limit() , encoder._buf.position() );
    }

    @Test(groups = {"basic"})
    public void testObject2(){
        ByteEncoder encoder = ByteEncoder.get();
        
        JSObject o = new JSObjectBase();
        o.set( "eliot" , "horowitz" );
        o.set( "num" , 517 );
        o.set( "z" , "y" );
        o.set( "asd" , null );
        
        JSObject o2 = new JSObjectBase();
        o2.set( "a" , "b" );
        o2.set( "b" , "a" );
        o.set( "next" , o2 );
        
        encoder.putObject( null , o );

        encoder.flip();
        
        ByteDecoder decoder = new ByteDecoder( encoder._buf );
        JSObject read = decoder.readObject();
        
        assertEquals( "horowitz" , read.get( "eliot" ).toString() );
        assertEquals( 517.0 , ((Double)read.get( "num" )).doubleValue() );
        assertEquals( "b" , ((JSObject)read.get( "next" ) ).get( "a" ).toString() );
        assertEquals( "a" , ((JSObject)read.get( "next" ) ).get( "b" ).toString() );
        assertEquals( "y" , read.get( "z" ).toString() );
        assertEquals( o.keySet().size() , read.keySet().size() );

        assertEquals( encoder._buf.limit() , encoder._buf.position() );
    }

    @Test(groups = {"basic"})
    public void testArray1(){
        ByteEncoder encoder = ByteEncoder.get();
        
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

        encoder.putObject( null , o );
        
        encoder.flip();
        
        ByteDecoder decoder = new ByteDecoder( encoder._buf );
        JSObject read = decoder.readObject();
        
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

        assertEquals( encoder._buf.limit() , encoder._buf.position() );
    }
    
    public static void main( String args[] ){
        (new ByteTest()).runConsole();
    }
}
