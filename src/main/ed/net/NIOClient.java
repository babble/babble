// NIOClient.java

package ed.net;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import ed.log.*;

public class NIOClient extends Thread {
    
    public NIOClient( String name , boolean verbose ){
        super( "NIOClient: " + name );
        _name = name;
        _verbose = verbose;
        
        _logger = Logger.getLogger( "nioclient-" + name );
        _loggerOpen = _logger.getChild( "open" );
        
        _logger.setLevel( verbose ? Level.DEBUG : Level.INFO );

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
            Call c = (Call)key.attachment();
            c._key = key;
            
            if ( key.isConnectable() ){
                i.remove();
                c.handleConnect();
            }
            
            if ( key.isReadable() ){
                i.remove();
                c.handleRead();
            }
            
        }
        
    }
    
    private void _doNewRequests(){
        for ( int i=0; i<10; i++ ){ // don't want to just handle new requests
            
            Call c = _newRequests.poll();
            if ( c == null )
                break;
            
            try {
                final InetSocketAddress addr = c.where();
                
                final SocketChannel s = SocketChannel.open();
                s.configureBlocking( false );
                s.register( _selector , SelectionKey.OP_CONNECT , c );
                s.connect( c.where() );
                
                c._sock = s;

                _loggerOpen.debug( addr );
            }
            catch ( IOException ioe ){
                _logger.error( "can't open at all" , ioe );
                c.errorOpen( ioe );
            }
                
        }
    }


    public abstract class Call {
        protected abstract InetSocketAddress where(); 
        protected abstract void errorOpen( IOException ioe );

        protected abstract void handleRead();
        protected abstract void handleConnect();

        private SocketChannel _sock;
        private SelectionKey _key;
    }

    final protected String _name;
    final protected boolean _verbose;

    final Logger _logger;
    final Logger _loggerOpen;

    private Selector _selector;
    private final BlockingQueue<Call> _newRequests = new ArrayBlockingQueue<Call>( 1000 );

    
    
    
}
