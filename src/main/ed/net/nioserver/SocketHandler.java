// SocketHandler.java

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

public abstract class SocketHandler implements WritableByteChannel {

    public final static boolean D = Boolean.getBoolean( "DEBUG.NIO" );

    protected SocketHandler( NIOServer server , SocketChannel sc ){
        _server = server;
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

    // --- selector management ----
        
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

        SelectionKey key = _channel.keyFor( _server._selector );
        if ( key == null ){
            if ( ! _server.didASelectorReset() )
                throw new RuntimeException( "can't find key for this selector" );
            key = _channel.register( _server._selector , ops );
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
        _server._selector.wakeup();
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

    public boolean isOpen(){
        return _channel.isOpen();
    }

    protected void _action( String what ){
        _lastAction = System.currentTimeMillis();
        _lastActionWhat = what;
    }

    protected void _selected( int ops ){
        _lastReadyOps = ops;
        _action( "selected" );
    }
        
    // ------ IO api -----

    public int write( ByteBuffer buf )
        throws IOException {
        return _channel.write( buf );
    }

    public long transerFile( FileChannel fc , long position , long count )
        throws IOException {
        return fc.transferTo( position , count , _channel );
    }

    // ------ intropsection ------
        
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
        
    protected boolean shouldTimeout( long now ){
        return now - _lastAction > NIOServer.CLIENT_TIMEOUT;
    }

    public long lastActionTime(){
        return _lastAction;
    }

    public String lastAction(){
        return _lastActionWhat;
    }

    public int lastReadyOps(){
        return _lastReadyOps;
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
        
    final SocketChannel _channel;
    final NIOServer _server;
    SelectionKey _key = null;
    
    protected boolean _bad = false;
    protected final long _created = System.currentTimeMillis();
        
    private long _lastAction = _created;
    private String _lastActionWhat = "created";
    private int _lastReadyOps = 0;
        
    private boolean _closed = false;
}


