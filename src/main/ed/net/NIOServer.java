// NIOServer.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.net;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import ed.log.*;
import ed.net.httpserver.*;

public abstract class NIOServer extends Thread {

    public final static boolean D = Boolean.getBoolean( "DEBUG.NIO" );
    final static long SELECT_TIMEOUT = 10;
    final static long CLIENT_TIMEOUT = 1000 * 60 * 2;
    final static int DEAD_CYCLES = 10000;
    final static int DEAD_CYCLES_WARN = DEAD_CYCLES - 50;
    
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

    public void stopServer(){
        try {
            _ssChannel.socket().close();
        }
        catch ( IOException ioe ){
            // don't care
        }
        _closed = true;
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

                //System.out.println( "moving : " + sk.channel() + " : " + sk.interestOps() );
                //sk.channel().register( s , sk.interestOps() );
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
        int emptyCycles = 0;

        while ( true ){
            
            final int numKeys;
            final long selectTime;
            
            try {
                _cleanOldConnections();
            }
            catch ( Exception e ){
                _logger.error( "error _cleanOldConnections" , e );
            }


            try {
                final long start = System.currentTimeMillis();
                numKeys = _selector.select( 10 );
                selectTime = System.currentTimeMillis() - start;
            }
            catch ( IOException ioe ){
                _logger.error( "couldn't select on port : " + _port , ioe );
                continue;
            }
            
            final Iterator<SelectionKey> i = _selector.selectedKeys().iterator();
            
            if ( numKeys == 0 && ! i.hasNext() ){
                emptyCycles++;
                if ( selectTime == 0 ){
                    deadSelectorCount++;
                    
                    if ( deadSelectorCount > DEAD_CYCLES_WARN ){
                        System.err.println( "got 0 keys after waiting " + selectTime + "ms " + deadSelectorCount + " in a row. total selectors: " + NIOUtil.toString( _selector.keys() ) );
                        
                        if ( deadSelectorCount > DEAD_CYCLES ){
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
                

                // the idea here is we want to give people a bit of time to finish up running requests
                if ( _closed && emptyCycles > 20 )
                    break;
                
                continue;
            }
            
            deadSelectorCount = 0;
            emptyCycles = 0;

            for ( ; i.hasNext() ;  ){
                SelectionKey key = i.next();
                i.remove();

                if ( D ) System.out.println( NIOUtil.toString( key ) );
                
                SocketChannel sc = null;
                SocketHandler sh = null;
                
                try {
                    
                    if ( key.isAcceptable() ){
                        ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                        
                        sc = ssc.accept();
                        sc.socket().setTcpNoDelay( true );
                        sc.configureBlocking( false );
                        
                        sh = accept( sc );
                        sh._key = sc.register( _selector , SelectionKey.OP_READ , sh );
                        
                        if ( D ) System.out.println( "connected : " + sc );

                        continue;
                    }
                    
                    sc = (SocketChannel)key.channel();
                    sh = (SocketHandler)key.attachment();

                    sh._action( "selected" );
                    sh._lastAction = System.currentTimeMillis();
                    

                    if ( sh._bad || sh.shouldClose() ){
                        if ( D ) System.out.println( "\t want to close : " + sc + " bad:" + sh._bad );
                        sh.close();
                        continue;
                    }
                    
                    if ( key.isWritable() ){
                        sh.writeMoreIfWant();
                    }
                    
                    if ( key.isReadable() ){
                        
                        readBuf.clear();
                        int read = sc.read( readBuf );
                        if ( D ) System.out.println( "\t" + read  );
                        if ( read == -1 ){
                            sh.close();
                            continue;
                        }
                        
                        if ( read > 0 ){
                            readBuf.flip();
                            
                            if ( sh.gotData( readBuf ) ){
                                key.cancel();
                            }
                        }
                        
                    }
                }
                catch ( OutOfMemoryError oom ){
                    try {
                        System.err.println( "got at OutOfMemoryError.  exiting" );
                        oom.printStackTrace();
                    }
                    catch ( Throwable t ){
                        // this means we're so screwed we can't print a stack trace
                    } 
                    Runtime.getRuntime().halt( -2 );
                }
                catch ( IOException e ){
                    
                    if ( D ) _selectLoopLogger.error( sc.toString() , e );
                    
                    if ( sc != null ){
                        try {
                            sc.close();
                        }
                        catch ( Exception ee ){
                        }
                    }
                    
                }
                catch ( Exception e ){
                    
                    _selectLoopLogger.error( sc.toString() , e );
                    
                    if ( sc != null ){
                        try {
                            sc.close();
                        }
                        catch ( Exception ee ){
                        }
                    }
                    
                }
                
            } // main selector loop
            
        }
        
        try {
            _selector.close();
        }
        catch ( IOException ioe ){
            // don't care
        }
        
    }

    private void _cleanOldConnections(){
        final long now = System.currentTimeMillis();
        
        int total = 0;

        for ( SelectionKey key : _selector.keys() ){
            total++;
            
            if ( ! key.isValid() )
                continue;
            
            Object attachment = key.attachment();
            if ( attachment == null )
                continue;
            
            SocketHandler handler = (SocketHandler)attachment;
            if ( ! handler.shouldTimeout( now ) )
                continue;
            
            if ( D ) System.out.println( "timing out connection" );

            handler._bad = true;
            try {
                handler.close();
            }
            catch ( Exception e ){}
        }
        
        _numSelectors = total;
    }
    
    protected List<SocketHandler> getCurrentHandlers(){

        Set<SelectionKey> keys = _selector.keys();
        List<SocketHandler> handlers = new ArrayList<SocketHandler>( keys.size() );
        
        for ( SelectionKey key : _selector.keys() ){
            
            if ( ! key.isValid() )
                continue;
            
            Object attachment = key.attachment();
            if ( attachment == null )
                continue;
            
            SocketHandler handler = (SocketHandler)attachment;
            handlers.add( handler );
        }
        
        return handlers;
    }
    
    protected int getNumSelectors(){
        return _numSelectors;
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
        
        /**
         * @return true if the selector thread should stop paying attention to this
         */
        protected abstract boolean writeMoreIfWant() 
            throws IOException;
        
        protected abstract String debugString();

        // other stuff
        
        public void registerForWrites()
            throws IOException {
            if ( D ) System.out.println( _channel + " registerForWrites" );
            _register( SelectionKey.OP_WRITE , "register-writes" );
        }

        public void registerForReads()
            throws IOException {
            if ( D ) System.out.println( _channel + " registerForReads" );
            _register( SelectionKey.OP_READ , "register-reads" );
        }

        private void _register( int ops , String name )
            throws IOException {
    
            _action( name );

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
        }
        
        public void cancel(){
            _key.cancel();
        }
        
        public void pause()
            throws IOException {
            if ( D ) System.out.println( _channel + " pausing selector" );
            _register( 0 , "pause" );
        }

        public void pause( String why )
            throws IOException {
            if ( D ) System.out.println( _channel + " pausing selector b/c : " + why );
            _register( 0 , why );
        }
        
        public InetAddress getInetAddress(){
            return _channel.socket().getInetAddress();
        }
        
        public int getRemotePort(){
            return _channel.socket().getPort();
        }

        public SocketAddress getRemote(){
            return _channel.socket().getRemoteSocketAddress();
        }
        
        public void bad(){
            _bad = true;
        }
        
        public void close(){
            if ( _closed )
                return;
            
            if ( D ) System.out.println( "closing " + _channel );
            _closed = true;
            

            try {
                _channel.close();
            }
            catch ( IOException ioe ){
                System.err.println( "error closing channel : " + _channel );
            }
            
            _key.attach( null );
            _key.cancel();
        }

        protected boolean shouldTimeout( long now ){
            return now - _lastAction > CLIENT_TIMEOUT;
        }

        public long lastActionTime(){
            return _lastAction;
        }

        public String lastAction(){
            return _lastActionWhat;
        }

        protected void _action( String what ){
            _lastAction = System.currentTimeMillis();
            _lastActionWhat = what;
        }
        
        public String toString(){
            return "SocketHandler: " + _channel;
        }

        public long age( long now ){
            return now - _created;
        }
        
        public long timeSinceLastAction( long now ){
            return now - _lastAction;
        }
        
        protected final SocketChannel _channel;
        private SelectionKey _key = null;
        protected boolean _bad = false;
        protected final long _created = System.currentTimeMillis();
        
        private long _lastAction = _created;
        private String _lastActionWhat = "created";

        private boolean _closed = false;
    }


    class SelectorMonitor extends HttpMonitor {
        SelectorMonitor(){
            super( "selectors" );
        }
        
        public void handle( MonitorRequest mr ){
            mr.addHeader( "NIO Selectors" );
            
            final long now = System.currentTimeMillis();
            
            mr.startData( "selectors" , "age" , "last action" , "time since last action" , "debug" );
            for ( SocketHandler sh : getCurrentHandlers() ){
                mr.addData( sh.getRemote().toString() ,
                            ((double)sh.age( now ))/1000 , sh.lastAction() , 
                            ((double)sh.timeSinceLastAction( now )) / 1000 ,
                            sh.debugString() );
            }
            mr.endData();
        }
        
    }

    protected HttpMonitor getSelectorMonitor(){
        return new SelectorMonitor();
    }
    
    protected final int _port;
    protected final ServerSocketChannel _ssChannel;    

    protected Selector _selector;
    
    private boolean _didASelectorReset = false;
    private boolean _closed = false;
    private int _numSelectors = 0;

    Logger _logger = Logger.getLogger( "nioserver" );
    Logger _selectLoopLogger = _logger.getChild( "runloop" );
}
