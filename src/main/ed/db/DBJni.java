// DBJni.java

package ed.db;

import java.nio.*;

import ed.js.*;

public class DBJni {

    static {
        System.load( ( new java.io.File( "build/libdb.so" ) ).getAbsolutePath() );
    }

    public static native String msg();

    // ----- INSERT    

    public static void insert( String collection , JSObject o ){
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        buf.order( ByteOrder.LITTLE_ENDIAN );
        
        ByteEncoder encoder = new ByteEncoder();
        
        buf.putInt( 0 ); // reserved

        encoder._put( buf , collection );

        encoder.putObject( buf , null , o );
        buf.flip();
        
        insert( buf );
    }

    private static void insert( ByteBuffer buf ){
        insert( buf , buf.position() , buf.limit() );
    }

    private static native void insert( ByteBuffer buf , int position , int limit );

    // ----- QUERY

    static class Result {

        Result( ByteBuffer buf ){
            _reserved = buf.getInt();
            _cursor = buf.getLong();
            _startingFrom = buf.getInt();
            _num = buf.getInt();

            ByteDecoder decoder = new ByteDecoder();

            int num = 0;
            
            while( buf.position() < buf.limit() && num < _num ){
                JSObject o = decoder.readObject( buf );
                num++;

                System.out.println( "-- : " + o.keySet().size() );
                for ( String s : o.keySet() )
                    System.out.println( "\t " + s + " : " + o.get( s ) );
            }
        }

        public String toString(){
            return "reserved:" + _reserved + " _cursor:" + _cursor + " _startingFrom:" + _startingFrom + " _num:" + _num ;
        }
        
        

        int _reserved;
        long _cursor;
        int _startingFrom;
        int _num;
    }

    public static void query( String collection , JSObject o ){
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        buf.order( ByteOrder.LITTLE_ENDIAN );
        
        ByteEncoder encoder = new ByteEncoder();
        
        buf.putInt( 0 ); // reserved

        encoder._put( buf , collection );

        buf.putInt( 0 ); // num to return

        encoder.putObject( buf , null , o );
        buf.flip();
        
        ByteBuffer res = ByteBuffer.allocateDirect( 1024 * 1024 );
        res.order( ByteOrder.LITTLE_ENDIAN );
        
        int len = query( buf , res );
        res.position( len );
        res.flip();
        
        System.out.println( res );
        
        Result r = new Result( res );
        System.out.println( r );


        System.out.println( res );
    }

    private static int query( ByteBuffer buf , ByteBuffer res ){
        return query( buf , buf.position() , buf.limit() , res );
    }

    private static native int query( ByteBuffer buf , int position , int limit , ByteBuffer res );

    // ----- TESTING
    
    public static void main( String args[] ){
        
        //System.out.println( "msg:" + msg() );

        JSObject o = new JSObjectBase();
        o.set( "jumpy" , "yes" );
        o.set( "name"  , "ab" );
        insert( "eliot.t1" , o );
     
        JSObject q = new JSObjectBase();
        q.set( "name" , "ab" );
        query( "eliot.t1" , q );
    }
    
}
