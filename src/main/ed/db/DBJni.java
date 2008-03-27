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

    protected void say( int op , ByteBuffer dataOut ){

        DBMessage m = new DBMessage( op , dataOut );
        Buf b = _pool.get();
        
        m.putHeader( b.out );
        b.out.put( dataOut );

        native_say( b.out );
    }

    protected int call( int op , ByteBuffer dataOut , ByteBuffer dataIn ){

        DBMessage m = new DBMessage( op , dataOut );
        Buf b = _pool.get();
        
        m.putHeader( b.out );
        b.out.put( dataOut );
                                
        int len = native_call( b.out , b.in );

        DBMessage inMsg = new DBMessage( b.in , b.in );
        dataIn.put( b.in );
        dataIn.flip();

        return dataIn.limit();
    }

    static native void native_say( ByteBuffer out );
    static native int native_call( ByteBuffer out , ByteBuffer in );

    static class Buf {
        
        Buf(){
            reset();
        }
        
        void reset(){
            reset( in );
            reset( out );
        }

        void reset( ByteBuffer buf ){
            buf.position( 0 );
            buf.limit( buf.capacity() );
            buf.order( ByteOrder.LITTLE_ENDIAN );
        }

        ByteBuffer in = ByteBuffer.allocateDirect( 1024 * 1024 );
        ByteBuffer out = ByteBuffer.allocateDirect( 1024 * 1024 );
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
