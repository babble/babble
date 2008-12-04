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

package ed.net.nioserver;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import ed.log.*;
import ed.net.*;
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

                    sh._selected( key.readyOps() );

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
                    
                    if ( sh != null ){
                        try {
                            sh.close();
                        }
                        catch ( Exception ee ){
                        }
                    }

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
    
    protected boolean didASelectorReset(){
        return _didASelectorReset;
    }

    protected abstract SocketHandler accept( SocketChannel sc );
    
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
