// DBJni.java

package ed.db;

import java.nio.*;

import ed.js.*;

public class DBJni {

    static {
        System.load( ( new java.io.File( "build/libdb.so" ) ).getAbsolutePath() );
    }

    public static native String msg();
    
    public static void insert( String collection , JSObject o ){

        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        buf.order( ByteOrder.LITTLE_ENDIAN );

        ByteEncoder encoder = new ByteEncoder();
        
        buf.putInt( 0 );
        encoder._put( buf , collection );

        encoder.putObject( buf , null , o );
        buf.flip();
        
        insert( buf );
        
    }

    private static void insert( ByteBuffer buf ){
        insert( buf , buf.position() , buf.limit() );
    }

    private static native void insert( ByteBuffer buf , int position , int limit );
    
    public static void main( String args[] ){

        System.out.println( "msg:" + msg() );

        JSObject o = new JSObjectBase();
        o.set( "f1" , 5.17 );
        
        insert( "eliot.t1" , o );
    }
    
}
