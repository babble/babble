// SocketHandler.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
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

    public boolean wasClosed(){
        return _closed;
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
        
        if ( buf.remaining() == 0 )
            return 0;
        
        final int w = _channel.write( buf );
        _wroteData( w );
        return w;
    }
    
    public long transerFile( FileChannel fc , long position , long count )
        throws IOException {
        
        if ( count == 0 )
            return 0;

        final long w = fc.transferTo( position , count , _channel );
        _wroteData( w );
        return w;
    }

    private void _wroteData( long amount ){
        _bytesWritten += amount;
        
        if ( amount == 0 )
            _emptyWritesInARow++;
        else
            _emptyWritesInARow = 0;
        
    }

    public int emptyWritesInARow(){
        return _emptyWritesInARow;
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

    public long bytesWritten(){
        return _bytesWritten;
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

    private long _bytesWritten = 0;
    private int _emptyWritesInARow = 0;
}


