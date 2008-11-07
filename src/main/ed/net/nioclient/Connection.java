// Connection.java

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

package ed.net.nioclient;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import ed.io.*;
import ed.log.*;
import ed.util.*;
import ed.net.*;
import ed.net.httpserver.*;
import static ed.net.HttpExceptions.*;
import static ed.net.nioclient.NIOClient.*;

public class Connection {
        
    Connection( NIOClient client , NIOClient.ConnectionPool pool , InetSocketAddress addr ){
        _client = client;
        _pool = pool;
        _addr = addr;
        _myLostConnectionLogger = _client._loggerLostConnection.getChild( _addr.toString() );
        try {
            _sock = SocketChannel.open();
            _sock.configureBlocking( false );
            _sock.connect( _addr );
            _key = _sock.register( _client._selector , SelectionKey.OP_CONNECT , this );
                
            _client._loggerOpen.debug( "opening connection to [" + addr + "]" );
        }
        catch ( UnresolvedAddressException e ){
            _error = new UnknownHostException( addr.toString() );
            throw new CantOpen( addr , _error );
        }
        catch ( IOException ioe ){
            _error = ioe;
            throw new CantOpen( addr , ioe );
        }
    }
        
    void handleConnect(){
        _event();
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
        catch ( Exception e ){
            err = new IOException( "weird error on finish connect : " + e );
        }
            
        if ( err == null ){
            _client._loggerOpen.debug( "done opening connection to [" + _addr + "]" );
            _ready = true;
            return;
        }

        _error = err;            
        _client.serverError( _addr , ServerErrorType.CONNECT , err );
        _client._loggerOpen.error( "error opening connection to [" + _addr + "] (" + this.hashCode() + ")" , _error );            
    }
        
    boolean ready(){
        return _ready;
    }
        
    boolean ok(){
        if ( _error != null ){
            _myLostConnectionLogger.info( "error" );
            return false;
        }

        if ( _closed ){
            _myLostConnectionLogger.debug( "closed " );
            return false;
        }
            
        if ( ! _ready && System.currentTimeMillis() - _opened > CONNECT_TIMEOUT ){
            _myLostConnectionLogger.info( "connect timeout" );
            return false;
        }
            
        if ( _current != null && _current.isDone() ){
            _myLostConnectionLogger.info( "have call but its done" );
            return false;
        }
            
        return true;
    }
        
    public int doRead( boolean errorOnEOF ){
        _fromServer.position( 0 );
        _fromServer.limit( _fromServer.capacity() );
            
        int read = 0;

        try {
            read = _sock.read( _fromServer );
            if ( read > 0 )
                _event();
        }
        catch ( IOException ioe ){
            _error( ServerErrorType.SOCK_TIMEOUT , ioe );
            return -1;
        }
            
        if ( read < 0 ){
            if ( errorOnEOF ){
                _error( ServerErrorType.EOF , new UnexpectedConnectionClosed( _numCalls - 1 ) );
            }
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
            
        if ( doRead( _current != null ) < 0 )
            return;
            
        WhatToDo next = null;
        if ( _current == null ){
            _client._logger.error( " _current is null in handleRead, should never happen" );
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
            _userError( "Call.handleRead returned ERROR" );
            return;
        case CLIENT_ERROR:
            _error = new IOException( "downstream error so closing" );
            done( true );
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

        _current = null;

        if ( close )
            _close( true );
        else 
            _putBackInPool();
    }

    void handleWrite(){
        int wrote = 0;
        try {
            wrote = _sock.write( _toServer );
            if ( wrote > 0 )
                _event();
        }
        catch ( IOException ioe ){
            _error( ServerErrorType.SOCK_TIMEOUT , ioe );
            _key.interestOps( 0 );
        }
            
        if ( _toServer.position() == _toServer.limit() ){
		
            if ( _extraDataToServer != null && _extraDataToServer.hasMore() ){
                _toServer.position(0);
                _toServer.limit( _toServer.capacity() );
                _extraDataToServer.write( _toServer );
                _toServer.flip();
                handleWrite();
                return;
            }

            _extraDataToServer = null;
            _key.interestOps( _key.OP_READ );
            _client._logger.debug( 3 , "finished writing" );
            return;
        }
            
        if ( wrote < 0 ){
            _error( ServerErrorType.SOCK_TIMEOUT , new IOException( "wrote 0 bytes" ) );
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
        _numCalls++;
        _event();

        _toServer.position( 0 );
        _toServer.limit( _toServer.capacity() );
            
        _extraDataToServer = _current.fillInRequest( _toServer );
        if ( _toServer.position() == 0 ){
            _userError( "fillInRequest didn't give me any data" );
            return;
        }
            
        _toServer.flip();
            
        handleWrite();
    }
        
    public void userError( String msg ){
        _error( ServerErrorType.WEIRD , new ClientError( msg ) );
    }

    private void _userError( String msg ){
        _userError( msg , true );
    }
        
    private void _userError( String msg , boolean shouldThrow ){
        _error( ServerErrorType.WEIRD , new IOException( "User Error : " + msg ) );
        if ( shouldThrow )
            throw new RuntimeException( msg );
    }

    private void _error( ServerErrorType type , IOException e ){
        _error = e;
        if ( _current != null )
            _current.error( type , e );

        if ( _ready )
            _close( true );

    }

    public String toString(){
        return _addr.toString();
    }

    public String statusString(){
        StringBuilder buf = new StringBuilder();
        buf.append( "ready:" ).append( _ready ).append( " " );
        buf.append( "error:" ).append( _error ).append( " " );
        buf.append( "closed:" ).append( _closed ).append( " " );
        buf.append( "has call:").append( _current != null ).append( " " );
            
        if ( _current != null ){
            int ops = _key.interestOps();
            buf.append( "waiting for read:" ).append( ( ops & _key.OP_READ ) > 0 ).append( " " );
            buf.append( "waiting for write:" ).append( ( ops & _key.OP_WRITE ) > 0 ).append( " " );
        }
            
        return buf.toString();
    }
        
    void _putBackInPool(){
        _pool.done( this );
        _client._logger.debug( 2 , "putting connection back in pool" );
    }

    void _close( boolean putBackInBool ){

        if ( _closed )
            return;
            
        _closed = true;
            
        if ( putBackInBool )
            _putBackInPool();

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

    void _event(){
        _lastEvent = System.currentTimeMillis();
    }
        
    void checkForTimeOut(){
        if ( _closed )
            return;
                    
        if ( System.currentTimeMillis() - _lastEvent < CONN_TIMEOUT )
            return;

        _close( true );
    }
    
    final NIOClient _client;
    final NIOClient.ConnectionPool _pool;
    final InetSocketAddress _addr;
    final long _opened = System.currentTimeMillis();
    final Logger _myLostConnectionLogger ;

    final ByteBuffer _toServer = ByteBuffer.allocateDirect( 1024 * 32 );
    final ByteBuffer _fromServer = ByteBuffer.allocateDirect( 1024 * 32 );
    private ByteStream _extraDataToServer = null;

    private final SocketChannel _sock;
    private final SelectionKey _key;  
        
    private boolean _ready = false;
    private IOException _error = null;
    private boolean _closed = false;
        
    private Call _current = null;
    private int _numCalls;
        
    private long _lastEvent = _opened;
} // end of Connection
    
