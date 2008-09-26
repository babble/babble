// NIOClient.java

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
import java.util.concurrent.*;

import ed.log.*;
import ed.util.*;

public class NIOClient extends Thread {
    
    protected enum WhatToDo { CONTINUE , PAUSE , DONE_AND_CLOSE , DONE_AND_CONTINUE , ERROR };

    public NIOClient( String name , int connectionsPerHost , int verboseLevel ){
        super( "NIOClient: " + name );
        _name = name;
        _verbose = verboseLevel;
        _connectionsPerHost = connectionsPerHost;
        
        _logger = Logger.getLogger( "nioclient-" + name );
        _loggerOpen = _logger.getChild( "open" );
        _loggerDrop = _logger.getChild( "drop" );

        _logger.setLevel( _verbose > 0 ? Level.DEBUG : Level.INFO );

        try {
            _selector = Selector.open();
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "can't open selector" , ioe );
        }
        
    }
    
    public void run(){
        while ( true ){
            try {
                _run();
            }
            catch ( Exception e ){
                _logger.error( "error in run loop" , e );
            }
        }
    }
    
    public boolean add( Call c ){
        return _newRequests.offer( c );
    }
    
    private void _run(){
        _doNewRequests();
        _doOldStuff();
    }
    
    private void _doOldStuff(){
        int numKeys = 0;
        try {
            numKeys = _selector.select( 10 );                
        }
        catch ( IOException ioe ){
            _logger.error( "can't select" , ioe );
        }
        
        if ( numKeys <= 0 )
            return;
        
        final Iterator<SelectionKey> i = _selector.selectedKeys().iterator();
        while ( i.hasNext() ){
            SelectionKey key = i.next();
            i.remove();
            
            if ( ! key.isValid() )
                continue;
            
            Connection c = (Connection)key.attachment();
            
            if ( c == null ){
                _logger.error( "attachment was null " );
                continue;
            }                

            if ( key.isConnectable() )
                c.handleConnect();
            
            if ( key.isReadable() )
                c.handleRead();
            
            if ( key.isWritable() )
                c.handleWrite();
        }
        
    }
    
    private void _doNewRequests(){
        List<Call> pushBach = new LinkedList<Call>();
        
        for ( int i=0; i<10; i++ ){ // don't want to just handle new requests
            
            Call c = _newRequests.poll();
            if ( c == null )
                break;
            
            try {
                final InetSocketAddress addr = c.where();
                final ConnectionPool pool = getConnectionPool( addr );
                
                Connection conn = pool.get( 0 );
                if ( conn == null ){
                    pushBach.add( c );
                    continue;
                }
                
                if ( ! conn.ready() ){
                    pushBach.add( c );
                    pool.done( conn );
                    continue;
                }

                conn.start( c );

            }
            catch ( CantOpen co ){
                _logger.error( "couldn't open" , co );
                c.error( co );
            }
                
        }

        for ( Call c : pushBach ){
            if ( ! _newRequests.offer( c ) ){
                _loggerDrop.error( "couldn't push something back on to queue." );
            }
        }
    }

    ConnectionPool getConnectionPool( InetSocketAddress addr ){
        ConnectionPool p = _connectionPools.get( addr );
        if ( p != null )
            return p;

        p = new ConnectionPool( addr );
        _connectionPools.put( addr , p );
        
        return p;
    }

    protected class Connection {
        
        Connection( ConnectionPool pool , InetSocketAddress addr ){
            _pool = pool;
            _addr = addr;
            try {
                _sock = SocketChannel.open();
                _sock.configureBlocking( false );
                _key = _sock.register( _selector , SelectionKey.OP_CONNECT , this );
                _sock.connect( _addr );
                
                _loggerOpen.debug( "opening connection to [" + addr + "]" );
            }
            catch ( IOException ioe ){
                _error = ioe;
                throw new CantOpen( addr , ioe );
            }
        }
        
        void handleConnect(){
            IOException err = null;
            try {
                if ( ! _sock.finishConnect() ){
                    err = new IOException( "finishConnect faild silently" );
                    err.fillInStackTrace();
                }
            }
            catch ( IOException ioe ){
                err = ioe;
            }
            
            if ( err == null ){
                _loggerOpen.debug( "done opening connection to [" + _addr + "]" );
                _ready = true;
                return;
            }
            
            _error = err;
        }

        boolean ready(){
            return _ready;
        }
        
        boolean ok(){
            if ( _error != null )
                return false;

            if ( System.currentTimeMillis() - _opened > 1000 * 60 )
                return false;
            
            if ( _closed )
                return false;

            return true;
        }
        
        public int doRead(){
            _fromServer.position( 0 );
            _fromServer.limit( _fromServer.capacity() );
            
            int read = 0;

            try {
                read = _sock.read( _fromServer );
            }
            catch ( IOException ioe ){
                _error( ioe );
                _current.error( ioe );
                return -1;
            }
            
            if ( read < 0 ){
                _error( new IOException( "socket dead" ) );
                done( true );
                return -1;
            }

            if ( read != _fromServer.position() )
                throw new RuntimeException( "i'm confused  says i read [" + read + "] but at position [" + _fromServer.position() + "]" );
            
            _fromServer.flip();

            return read;
        }

        void handleRead(){
            // read data from wire
            // pass data to Call
            // response could be
            //   - continue
            //   - pause - turn off selector
            //   - done - add yourself back to the pool
            //   - error, close connection
            
            if ( doRead() < 0 )
                return;
            
            WhatToDo next = null;
            if ( _current == null ){
                _logger.error( " _current is null in handleRead, should never happen" );
                next = WhatToDo.DONE_AND_CLOSE;
            }
            else {
                next = _current.handleRead( _fromServer , this );
            }
            
            switch ( next ){
            case CONTINUE: 
                _key.interestOps( _key.OP_READ );
                return;
            case PAUSE: 
                _key.interestOps( 0 );
                return;
            case ERROR:
                _userError( "unknown" );
                return;
            case DONE_AND_CLOSE:
                done( true );
                return;
            case DONE_AND_CONTINUE:
                done( false );
                return;
            }
        }
        
        public void done( boolean close ){

            if ( close )
                close();

            if ( _error == null ){
                _logger.debug( "putting connection back in pool" );
                _pool.done( this );
            }
            _current = null;
        }

        void handleWrite(){
            int wrote = 0;
            try {
                wrote = _sock.write( _toServer );
            }
            catch ( IOException ioe ){
                _error( ioe );
                _key.interestOps( 0 );
            }
            
            if ( _toServer.position() == _toServer.limit() ){
                _key.interestOps( _key.OP_READ );
                _logger.debug( "finished writing" );
                return;
            }
            
            if ( wrote < 0 ){
                _error( new IOException( "wrote 0 bytes" ) );
                return;
            }

            // need to write more
            _key.interestOps( _key.OP_WRITE );
        }

        void start( Call c ){
            if ( c == null ){
                _userError( "shouldn't call start with a null Call" );
                return;
            }
            
            if ( _current != null ){
                _userError( "trying to start a Call but already have one" );
                return;
            }
            
            _current = c;
            
            _toServer.position( 0 );
            _toServer.limit( _toServer.capacity() );
            
            _current.fillInRequest( _toServer );
            if ( _toServer.position() == 0 ){
                _userError( "trying to start a Call but already have one" );
                return;
            }
            
            _toServer.flip();

            handleWrite();
        }

        private void _userError( String msg ){
            _error( new IOException( "User Error : " + msg ) );
            throw new RuntimeException( msg );
        }

        private void _error( IOException e ){
            _error = e;
            if ( _current != null )
                _current.error( e );
            if ( _ready ){
                close();
            }
        }

        public String toString(){
            return _addr.toString();
        }
        
        void close(){
            if ( _closed )
                return;
            
            _closed = true;
            try {
                _key.interestOps( 0 );
                _key.attach( null );
                _key.cancel();
                _sock.close();
            }
            catch ( IOException ioe ){
                // don't care
            }
        }

        final ConnectionPool _pool;
        final InetSocketAddress _addr;
        final long _opened = System.currentTimeMillis();

        final ByteBuffer _toServer = ByteBuffer.allocateDirect( 1024 * 32 );
        final ByteBuffer _fromServer = ByteBuffer.allocateDirect( 1024 * 32 );

        final SocketChannel _sock;
        final SelectionKey _key;  
        
        private boolean _ready = false;
        private IOException _error = null;
        private boolean _closed = false;
        
        private Call _current = null;

        
    }
    
    class ConnectionPool extends SimplePool<Connection> {
        ConnectionPool( InetSocketAddress addr ){
            super( "ConnectionPool : " + addr , _connectionsPerHost , _connectionsPerHost );
            _addr = addr;
        }

        protected Connection createNew(){
            return new Connection( this , _addr );
        }
        
        public boolean ok( Connection c ){
            return c.ok();
        }
        
        final InetSocketAddress _addr;
    }
    
    class ConnectionError extends RuntimeException {
        ConnectionError( String msg , InetSocketAddress addr , IOException cause ){
            super( "[" + addr + "] : " + msg + " " + cause , cause );
            _addr = addr;
        }

        final InetSocketAddress _addr;
    }
    
    class CantOpen extends ConnectionError {
        CantOpen( InetSocketAddress addr , IOException ioe ){
            super( "can't open" , addr , ioe );
        }
    }
    
    public abstract class Call {
        
        protected abstract InetSocketAddress where(); 
        protected abstract void error( Exception e );

        protected abstract void fillInRequest( ByteBuffer buf );
        protected abstract WhatToDo handleRead( ByteBuffer buf , Connection conn );

    }

    final protected String _name;
    final protected int _verbose;
    final protected int _connectionsPerHost;

    final Logger _logger;
    final Logger _loggerOpen;
    final Logger _loggerDrop;

    private Selector _selector;
    private final BlockingQueue<Call> _newRequests = new ArrayBlockingQueue<Call>( 1000 );
    private final Map<InetSocketAddress,ConnectionPool> _connectionPools = new HashMap<InetSocketAddress,ConnectionPool>();
    
    
}
