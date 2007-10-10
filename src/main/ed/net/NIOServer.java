// NIOServer.java

package ed.net;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public abstract class NIOServer extends Thread {

    final static boolean D = true;

    public NIOServer( int port )
        throws IOException {
        super( "NIOServer  port:" + port  );
        
        _port = port;
        
        _ssChannel = ServerSocketChannel.open();
        _ssChannel.configureBlocking( false );
        _ssChannel.socket().bind( new InetSocketAddress( port ) );
        
        _selector = Selector.open();
        _ssChannel.register( _selector , SelectionKey.OP_ACCEPT );
            
        setDaemon( true );
    }
    
    public Selector getSelector(){
        return _selector;
    }
    
    public void run(){

        final ByteBuffer readBuf = ByteBuffer.allocateDirect(1024);
        final ByteBuffer writeBuf = ByteBuffer.allocateDirect(1024);

        while ( true ){

            try {
                _selector.select();
            }
            catch ( IOException ioe ){
                ioe.printStackTrace();
                continue;
            }
            
            for ( Iterator<SelectionKey> i = _selector.selectedKeys().iterator() ; i.hasNext() ;  ){
                try {
                    SelectionKey key = i.next();
                    
                    if ( D ) System.out.println( key + " read : " + key.isReadable() + " write : " + key.isWritable() );
                    
                    if ( key.isAcceptable() ){
                        
                        ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                        
                        SocketChannel sc = ssc.accept();
                        
                        sc.configureBlocking( false );
                        
                        SocketHandler sh = accept( sc );
                        sc.register( _selector , SelectionKey.OP_READ , sh );
                        
                        if ( D ) System.out.println( sc );
                        i.remove();
                        continue;
                    }
                    
                    SocketChannel sc = (SocketChannel)key.channel();
                    SocketHandler sh = (SocketHandler)key.attachment();
                    
                    if ( sh.shouldClose() ){
                        if ( D ) System.out.println( "want to close" );
                        sc.close();
                        continue;
                    }
                    
                    if ( key.isReadable() ){
                        i.remove();
                        
                        if ( D ) System.out.println( "\t" + sc );
                        
                        readBuf.clear();
                        int read = sc.read( readBuf );
                        if ( D ) System.out.println( "\t\t" + read  );
                        if ( read == -1 ){
                            sc.close();
                            continue;
                        }
                        
                        if ( read == 0 )
                            continue;
                        
                        readBuf.flip();
                        
                        if ( sh.gotData( readBuf ) ){
                            key.cancel();
                            continue;
                        }
                        
                    }
                }
                catch ( IOException ioe ){
                    ioe.printStackTrace();
                }
                
            }
        }
    }

    protected abstract SocketHandler accept( SocketChannel sc );
    
    protected abstract class SocketHandler {

        protected SocketHandler( SocketChannel sc ){
            _channel = sc;
        }

        /**
         * @return true if the selector thread should stop paying attention to this
         */
        protected abstract boolean gotData( ByteBuffer inBuf )
            throws IOException ;

        protected abstract boolean shouldClose();

        protected final SocketChannel _channel;
    }

    protected final int _port;
    protected final Selector _selector;
    protected final ServerSocketChannel _ssChannel;    
}
