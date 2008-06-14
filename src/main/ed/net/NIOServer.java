// NIOServer.java

package ed.net;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public abstract class NIOServer extends Thread {

    final static boolean D = Boolean.getBoolean( "DEBUG.NIO" );
    final static long SELECT_TIMEOUT = 10;
    
    public NIOServer( int port )
        throws IOException {
        super( "NIOServer  port:" + port  );
        
        _port = port;
        
        _ssChannel = ServerSocketChannel.open();
        _ssChannel.configureBlocking( false );
        _ssChannel.socket().bind( new InetSocketAddress( port ) );
        
        _initSelector();

        setDaemon( true );
    }

    public Selector getSelector(){
        return _selector;
    }

    private void _initSelector()
        throws IOException {

        Selector s = Selector.open();

        if ( _selector != null ){
            for ( SelectionKey sk : _selector.keys() ){
                
                if ( sk.channel() == _ssChannel )
                    continue;

                if ( sk.interestOps() == 0 )
                    continue;

                System.out.println( "moving : " + sk.channel() + " : " + sk.interestOps() );
                sk.channel().register( s , sk.interestOps() );
            }
            _selector.close();
            _didASelectorReset = true;
        }

        _ssChannel.register( s , SelectionKey.OP_ACCEPT );
        _selector = s;
    }
    
    public void run(){
        
        System.out.println( "Listening on port: " + _port );

        final ByteBuffer readBuf = ByteBuffer.allocateDirect(1024);
        final ByteBuffer writeBuf = ByteBuffer.allocateDirect(1024);
        
        int deadSelectorCount = 0;

        while ( true ){

            final int numKeys;
            final long selectTime;

            try {
                final long start = System.currentTimeMillis();
                numKeys = _selector.select( 10 );
                selectTime = System.currentTimeMillis() - start;
            }
            catch ( IOException ioe ){
                ed.log.Logger.getLogger( "nio" ).error( "couldn't select on port : " + _port , ioe );
                continue;
            }
            
            final Iterator<SelectionKey> i = _selector.selectedKeys().iterator();

            if ( numKeys == 0 && ! i.hasNext() ){
                if ( selectTime == 0 ){
                    deadSelectorCount++;
                    
                    if ( deadSelectorCount > 950 ){
                        System.err.println( "got 0 keys after waiting :" + selectTime + " " + deadSelectorCount + " in a row." );
                        
                        if ( deadSelectorCount > 1000 ){
                            System.err.println( "****  KILLING SELECTOR AND STARTING OVER - should be taking good channels " );
                            try {
                                _initSelector();
                            }
                            catch ( Throwable t ){
                                System.err.println( "couldn't re-init selector after issue.  dying" );
                                t.printStackTrace();
                                System.exit( -1 );
                            }
                        }
                    }
                }
                continue;
            }
            
            deadSelectorCount = 0;

            for ( ; i.hasNext() ;  ){
                SelectionKey key = i.next();
                
                SocketChannel sc = null;
                SocketHandler sh = null;
                
                try {
                    
                    if ( D ) System.out.println( key + " read : " + key.isReadable() + " write : " + key.isWritable() );
                    
                    if ( key.isAcceptable() ){
                        
                        ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                        
                        sc = ssc.accept();
                        sc.socket().setTcpNoDelay( true );
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
                    if ( sc != null ){
                        try {
                            sc.close();
                        }
                        catch ( Exception ee ){
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

        protected abstract boolean shouldClose()
            throws IOException ;

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
            
            SelectionKey key = _channel.keyFor( _selector );
            if ( key == null ){
                if ( ! _didASelectorReset )
                    throw new RuntimeException( "can't find key for this selector" );
                key = _channel.register( _selector , ops );
                key.attach( this );
                _key = key;
            }
            
            if ( key.attachment() != this )
                throw new RuntimeException( "why is the attachment not me" );
            
            if ( key != _key )
                throw new RuntimeException( "why are the keys different" );

            if ( key.interestOps() == ops )
                return;

            key.interestOps( ops );
            _selector.wakeup();
            if ( D ) System.out.println( key + " : " + key.isValid() );

        }
        
        public void cancel(){
            _key.cancel();
        }

        public void pause(){
            _key.interestOps( 0 );
        }
        
        public InetAddress getInetAddress(){
            return _channel.socket().getInetAddress();
        }

        protected final SocketChannel _channel;
        private SelectionKey _key = null;
        
    }

    protected final int _port;
    protected final ServerSocketChannel _ssChannel;    

    protected Selector _selector;

    private boolean _didASelectorReset = false;
}
