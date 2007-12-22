// DBJni.java

package ed.db;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import ed.js.*;

public class DBJni extends DBApiLayer {
    
    DBJni( String root , String ip ){
	super( root );

        try {
            ip = InetAddress.getByName( ip ).getHostAddress();
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "can't get ip for:" + ip );
        }
        
        _ip = ip;
        _sock = getSockAddr( _ip );
        
    }
    
    protected void doInsert( ByteBuffer buf ){
        native_insert( _sock , buf , buf.position() , buf.limit() );
    }

    protected void doDelete( ByteBuffer buf ){
        native_delete( _sock , buf , buf.position() , buf.limit() );
    }

    protected void doUpdate( ByteBuffer buf ){
        native_update( _sock , buf , buf.position() , buf.limit() );
    }

    protected int doQuery( ByteBuffer out , ByteBuffer in ){
        return native_query( _sock , out , out.position() , out.limit() , in );
    }

    protected int doGetMore( ByteBuffer out , ByteBuffer in ){
        return native_getMore( _sock , out , out.position() , out.limit() , in );
    }

    public String toString(){
        return "DBConnection " + _ip + ":" + _root;
    }

    static long getSockAddr( String name ){
        Long addr = _ipToSockAddr.get( name );
        if ( addr != null )
            return addr;
        
        addr = createSock( name );
        _ipToSockAddr.put( name, addr );
        return addr;
    }    

    private static native long createSock( String name );

    private synchronized static native String msg( long sock );

    private synchronized static native void native_insert( long sock , ByteBuffer buf , int position , int limit );
    private synchronized static native void native_delete( long sock , ByteBuffer buf , int position , int limit );
    private synchronized static native int native_query( long sock , ByteBuffer buf , int position , int limit , ByteBuffer res );
    private synchronized static native void native_update( long sock , ByteBuffer buf , int position , int limit );

    private synchronized static native int native_getMore( long sock , ByteBuffer buf , int position , int limit , ByteBuffer res );
    
    final String _ip;
    final long _sock;

    static final Map<String,Long> _ipToSockAddr = Collections.synchronizedMap( new HashMap<String,Long>() );
    static final long _defaultIp;

    static {
        String ext = "so";
        String os = System.getenv("OSTYPE" );
        if ( "darwin".equals( os ) )
            ext = "jnilib";
        
        System.load( ( new java.io.File( "build/libdb." + ext ) ).getAbsolutePath() );

        _defaultIp = createSock( "127.0.0.1" );
    }
    
}
