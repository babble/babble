// DBPort.java

package ed.db;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

import ed.util.*;

public class DBPort {
    
    static final int PORT = 27017;
    static final boolean USE_NAGLE = false;
    
    static final long CONN_RETRY_TIME_MS = 15000;
    

    DBPort( InetSocketAddress addr )
        throws IOException {
        this( addr , null );
    }
    
    DBPort( InetSocketAddress addr  , DBPortPool pool )
        throws IOException {
        _addr = addr;
        _pool = pool;

        _array[0].order( Bytes.ORDER );

        _hashCode = _addr.hashCode();

        _open();
    }

    /**
     * @param response will get wiped
     */
    DBMessage call( DBMessage msg , ByteBuffer response )
        throws IOException {
        return go( msg , response );
    }
    
    void say( DBMessage msg )
        throws IOException {
        go( msg , null );
    }

    private synchronized DBMessage go( DBMessage msg , ByteBuffer response )
        throws IOException {
        
        _reset( _array[0] );
        msg.putHeader( _array[0] );
        _array[0].flip();
        
        _array[1] = msg.getData();
        
        _sock.write( _array );
        
        if ( response == null )
            return null;

        _reset( _array[0] );
        _sock.read( _array[0] );
        _array[0].flip();
        DBMessage msgResponse = new DBMessage( _array[0] , response );
        
        _reset( response );

        if ( msgResponse._len <= DBMessage.HEADER_LENGTH )
            throw new IllegalArgumentException( "db sent invalid length : " + msgResponse._len );
        

        final int bodySize = msgResponse._len - DBMessage.HEADER_LENGTH;

        if ( bodySize > response.capacity() )
            throw new IllegalArgumentException( "db message size is too big (" + bodySize + ") max is (" + response.capacity() + ")" );

        response.limit( bodySize );
        while ( response.remaining() > 0 )
            _sock.read( response );
        
        return msgResponse;
    }

    void _reset( ByteBuffer buf ){
        buf.position( 0 );
        buf.limit( buf.capacity() );
    }
    
    void _open()
        throws IOException {
        
        long sleepTime = 100;

        final long start = System.currentTimeMillis();
        while ( true ){
            
            IOException lastError = null;

            try {
                _sock = SocketChannel.open();
                _sock.connect( _addr );
                
                _sock.socket().setTcpNoDelay( ! USE_NAGLE );
                
                return;
            }
            catch ( IOException ioe ){
                lastError = ioe;
                _error( "connect fail" );
            }
            
            if ( _pool != null && ! _pool._everWorked )
                throw lastError;

            long sleptSoFar = System.currentTimeMillis() - start;

            if ( sleptSoFar >= CONN_RETRY_TIME_MS )
                throw lastError;
            
            if ( sleepTime + sleptSoFar > CONN_RETRY_TIME_MS )
                sleepTime = CONN_RETRY_TIME_MS - sleptSoFar;

            _error( "going to sleep and retry.  total sleep time after = " + ( sleptSoFar + sleptSoFar ) + "ms  this time:" + sleepTime + "ms" );
            ThreadUtil.sleep( sleepTime );
            sleepTime *= 2;
            
        }
        
    }

    public int hashCode(){
        return _hashCode;
    }
    
    public String host(){
        return _addr.toString();
    }
    
    public String toString(){
        return "{DBPort  " + host() + "}";
    }

    void _error( String msg ){
        System.err.println( "DBPort " + host() + " " + msg );
    }

    protected void finalize(){
        if ( _sock != null ){
            try {
                _sock.close();
            }
            catch ( Exception e ){
                // don't care
            }
            
            _sock = null;            
        }

    }
    
    final int _hashCode;
    final InetSocketAddress _addr;
    final DBPortPool _pool;

    private final ByteBuffer[] _array = new ByteBuffer[]{ ByteBuffer.allocateDirect( DBMessage.HEADER_LENGTH ) , null };
    private SocketChannel _sock;
}
