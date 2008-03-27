// DBJni.java

package ed.db;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import ed.js.*;
import ed.util.*;

public class DBJni extends DBMessageLayer {
    
    DBJni( String root ){
	super( root );
    }

    protected void say( int op , ByteBuffer buf ){

        DBMessage m = new DBMessage( op , buf );
        Buf b = _pool.get();
        
        m.putHeader( b.outHeader );

        native_say( op , b.outHeader , b.out , b.out.limit() );
    }

    protected int call( int op , ByteBuffer out , ByteBuffer in ){

        DBMessage m = new DBMessage( op , out );
        Buf b = _pool.get();
        
        m.putHeader( b.outHeader );
                                
        int len = native_call( op , b.outHeader , b.out , b.out.limit() , b.inHeader , in );
        in.limit( len );

        return len;
    }

    static native void native_say( int op , ByteBuffer outHeader , ByteBuffer out , int outLength );
    static native int native_call( int op , ByteBuffer outHeader , ByteBuffer out , int outLength , ByteBuffer inHeader , ByteBuffer in );

    static class Buf {
        
        Buf(){
            reset();
        }
        
        void reset(){
            reset( in );
            reset( outHeader );
            reset( out );
            reset( inHeader );
        }

        void reset( ByteBuffer buf ){
            buf.position( 0 );
            buf.limit( buf.capacity() );
            buf.order( ByteOrder.LITTLE_ENDIAN );
        }

        ByteBuffer in = ByteBuffer.allocateDirect( 1024 * 1024 );
        ByteBuffer out = ByteBuffer.allocateDirect( 1024 * 1024 );

        ByteBuffer outHeader = ByteBuffer.allocateDirect( DBMessage.HEADER_LENGTH );
        ByteBuffer inHeader = ByteBuffer.allocateDirect( DBMessage.HEADER_LENGTH );
    }

    static class BufPool extends SimplePool<Buf> {
        BufPool(){
            super( "DBJni-BufPool" , 20 , 50 );
        }
        
        public boolean ok( Buf b ){
            b.reset();
            return true;
        }

        protected Buf createNew(){
            return new Buf();
        }
    }
    
    static final BufPool _pool = new BufPool();
}
