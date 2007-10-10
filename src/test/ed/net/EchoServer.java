// EchoServer.java

package ed.net;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class EchoServer extends NIOServer {

    EchoServer( int port )
        throws IOException {
        super( port );
        start();
    }
    
    protected EchoSocketHandler accept( SocketChannel sc ){
        return new EchoSocketHandler( sc );
    }
    
    class EchoSocketHandler extends NIOServer.SocketHandler {
        EchoSocketHandler( SocketChannel sc ){
            super( sc );
        }
        
        boolean shouldClose(){
            return false;
        }
        
        boolean gotData( ByteBuffer inBuf )
            throws IOException {
            
            byte bb[] = new byte[inBuf.remaining()];
            inBuf.get( bb );
            
            _writeBuf.clear();
            _writeBuf.put( ( new String( bb ) ).getBytes() );
            _writeBuf.flip();
            _channel.write( _writeBuf );
            
            return false;
        }
        
    }    
    
    final ByteBuffer _writeBuf = ByteBuffer.allocateDirect( 2048 );

    public static void main( String args[] )
        throws Exception {
        EchoServer es = new EchoServer( 9999 );
        es.join();
    }
}
