// DBPort.java

package ed.db;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class DBPort {

    static final int PORT = 27017;
    
    DBPort()
        throws IOException {
        this( "127.0.0.1" , PORT );
    }
    
    DBPort( String host )
        throws IOException {
        this( host , PORT );
    }
    
    DBPort( String host , int port )
        throws IOException {
        _host = host;
        _port = port;
        _array[0].order( ByteOrder.LITTLE_ENDIAN );
        _addr = new InetSocketAddress( _host , _port );

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
        response.limit( msgResponse._len - DBMessage.HEADER_LENGTH );
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
        _sock = SocketChannel.open();
        _sock.connect( _addr );

        _sock.socket().setTcpNoDelay( true );
    }
    
    public String toString(){
        return "{DBPort  host:" + _host + " port:" + _port + "}";
    }
    
    final String _host;
    final int _port;
    final InetSocketAddress _addr;
    
    private final ByteBuffer[] _array = new ByteBuffer[]{ ByteBuffer.allocateDirect( DBMessage.HEADER_LENGTH ) , null };
    private SocketChannel _sock;
}
