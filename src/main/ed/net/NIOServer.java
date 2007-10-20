// NIOServer.java

package ed.net;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public abstract class NIOServer extends Thread {

    final static boolean D = false;

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
                _selector.select( 10 );
            }
            catch ( IOException ioe ){
                ioe.printStackTrace();
                continue;
            }
            
            for ( Iterator<SelectionKey> i = _selector.selectedKeys().iterator() ; i.hasNext() ;  ){
                SelectionKey key = i.next();
                
                SocketChannel sc = null;
                SocketHandler sh = null;
                
                try {
                    
                    if ( D ) System.out.println( key + " read : " + key.isReadable() + " write : " + key.isWritable() );
                    
                    if ( key.isAcceptable() ){
                        
                        ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                        
                        sc = ssc.accept();
                        
                        sc.configureBlocking( false );
                        
                        sh = accept( sc );
                        sh._key = sc.register( _selector , SelectionKey.OP_READ , sh );
                        
                        if ( D ) System.out.println( sc );
                        i.remove();
                        continue;
                    }
                    
                    sc = (SocketChannel)key.channel();
                    sh = (SocketHandler)key.attachment();
                    
                    if ( sh.shouldClose() ){
                        if ( D ) System.out.println( "\t want to close : " + sc );
                        Socket temp = sc.socket();
                        temp.shutdownInput();
                        temp.shutdownOutput();
                        temp.close();
                        sc.close();
                        key.cancel();
                        continue;
                    }
                    
                    if ( key.isWritable() )
                        sh.writeMoreIfWant();
                    
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
                catch ( Exception e ){
                    e.printStackTrace();
                    if ( sc != null ){
                        try {
                            sc.close();
                        }
                        catch ( Exception ee ){
                            ee.printStackTrace();
                        }
                    }
                    
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

        protected abstract void writeMoreIfWant() 
            throws IOException;
        
        // other stuff

        public void registerForWrites()
            throws IOException {
            if ( D ) System.out.println( "registerForWrites" );
            _register( SelectionKey.OP_WRITE );
        }

        public void registerForReads()
            throws IOException {
            if ( D ) System.out.println( "registerForReads" );
            _register( SelectionKey.OP_READ );
        }

        private void _register( int ops )
            throws IOException {
            SelectionKey key = _channel.register( _selector , ops , this );
            _selector.wakeup();
            if ( D ) System.out.println( key + " : " + key.isValid() );

        }
        
        public void cancel(){
            _key.cancel();
        }

        public void pause(){
            _key.interestOps( 0 );
        }
        
        protected final SocketChannel _channel;
        private SelectionKey _key = null;
        
    }

    protected final int _port;
    protected final Selector _selector;
    protected final ServerSocketChannel _ssChannel;    
}
