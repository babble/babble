// DBTCP.java

package ed.db;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import ed.js.*;
import ed.log.*;

public class DBTCP extends DBMessageLayer {

    static Logger _logger = Logger.getLogger( "DBTCP" );
    static Logger _createLogger = _logger.getChild( "connect" );
    
    public DBTCP( DBAddress addr ){
        super( addr._name );

        _createLogger.info( addr );
        
        _set( addr );
        _allHosts = null;
    }
    
    public DBTCP( List<DBAddress> all ){
        super( all.get(0)._name );

        final String name = all.get(0)._name;
        for ( int i=1; i<all.size(); i++ ){
            if ( ! all.get(i)._name.equals( name ) )
                throw new IllegalArgumentException( " names don't match [" + all.get(i)._name + "] != [" + name + "]" );
        }
        
        _allHosts = new ArrayList<DBAddress>( all ); // make a copy so it can't be modified
        _pickInitial();

        _createLogger.info( all  + " -> " + _curAddress );
    }

    public void requestStart(){
        _threadPort.get().requestStart();
    }
    
    public void requestDone(){
        _threadPort.get().requestDone();
    }
    
    public void requestEnsureConnection(){
        _threadPort.get().requestEnsureConnection();
    }

    protected void say( int op , ByteBuffer buf ){
        MyPort mp = _threadPort.get();
        DBPort port = mp.get( true );
                
        try {
            port.say( new DBMessage( op , buf ) );
            mp.done( port );
        }
        catch ( Exception ioe ){
            mp.error();
            _error();
            throw new JSException( "can't say something" , ioe );
        }
    }
    
    protected int call( int op , ByteBuffer out , ByteBuffer in ){
        return _call( op , out , in , 2 );
    }
    
    private int _call( int op , ByteBuffer out , ByteBuffer in , int retries ){
        MyPort mp = _threadPort.get();
        DBPort port = mp.get( false );
        
        try {
            DBMessage a = new DBMessage( op , out );
            DBMessage b = port.call( a , in );
            mp.done( port );
            
            String err = _getError( in );

            if ( err != null ){
                if ( "not master".equals( err ) ){
                    _pickCurrent();
                    if ( retries <= 0 )
                        throw new RuntimeException( "not talking to master and retries used up" );
                    in.position( 0 );
                    
                    return _call( op , out , in , retries -1 );
                }
            }

            return b.dataLen();
        }
        catch ( Exception ioe ){
            mp.error();
            if ( _error() && retries > 0 ){
                in.position( 0 );
                return _call( op , out , in , retries - 1 );
            }
            throw new JSException( "can't call something" , ioe );
        }
    }
    
    public DBAddress getAddress(){
        return _curAddress;
    }
    
    public String getConnectPoint(){
        return _curAddress.toString();
    }

    boolean _error(){
        _pickCurrent();
        return true;
    }

    String _getError( ByteBuffer buf ){
        QueryHeader header = new QueryHeader( buf , 0 );
        if ( header._num != 1 )
            return null;
        
        DBJSObject obj = new DBJSObject( buf , header.headerSize() );
        Object err = obj.get( "$err" );
        if ( err == null )
            return null;
        
        return err.toString();
    }

    class MyPort {
        
        DBPort get( boolean keep ){
            if ( _port != null )
                return _port;
            
            DBPort p = _curPortPool.get();
            if ( keep && _inRequest )
                _port = p;
            return p;
        }
        
        void done( DBPort p ){
            if ( p != _port )
                _curPortPool.done( p );
        }

        void error(){
            _port = null;
            _curPortPool.gotError();
        }

        void requestEnsureConnection(){
            if ( ! _inRequest )
                return;
            
            if ( _port != null )
                return;
            
            _port = _curPortPool.get();
        }
        
        void requestStart(){
            _inRequest = true;
            if ( _port != null ){
                _port = null;
                System.err.println( "ERROR.  somehow _port was not null at requestStart" );
            }
        }
        
        void requestDone(){
            if ( _port != null )
                _curPortPool.done( _port );
            _port = null;
            _inRequest = false;
        }

        DBPort _port;
        boolean _inRequest;
    }

    private void _pickInitial(){
        // we need to just get a server to query for ismaster
        _pickCurrent();
        
        DBCollection collection = getCollection( "$cmd" );
        Iterator<JSObject> i = collection.find( _isMaster , null , 0 , 1 );
        if ( i == null || ! i.hasNext() )
            throw new RuntimeException( "no result for ismaster query?" );
        JSObject res = i.next();
        if ( i.hasNext() )
            throw new RuntimeException( "what's going on" );
        
        int ismaster = ((Number)res.get( "ismaster" )).intValue();
        if ( 1 == ismaster )
            return;

        if ( res.get( "remote" ) == null )
            throw new RuntimeException( "remote not sent back!" );
        
        String remote = res.get( "remote" ).toString();
        synchronized ( _allHosts ){
            for ( DBAddress a : _allHosts ){
                if ( ! a.sameHost( remote ) )
                    continue;
                System.out.println( "remote [" + remote + "] -> [" + a + "]" );
                _set( a );
                return;
            }
        }
        throw new RuntimeException( "can't find remote [" + remote + "]" );
    }
    
    private void _pickCurrent(){
        if ( _allHosts == null )
            throw new RuntimeException( "got master/slave issue but not in master/slave mode on the client side" );
        
        synchronized ( _allHosts ){
            Collections.shuffle( _allHosts );
            for ( int i=0; i<_allHosts.size(); i++ ){
                DBAddress a = _allHosts.get( i );
                if ( a == _curAddress )
                    continue;

                if ( _curAddress != null )
                    _logger.info( "switching from [" + _curAddress + "] to [" + a + "]" );
                
                _set( a );
                return;
            }
        }
        
        throw new RuntimeException( "couldn't find a new host to swtich too" );
    }

    private boolean _set( DBAddress addr ){
        if ( _curAddress == addr )
            return false;
        _curAddress = addr;
        _curPortPool = DBPortPool.get( _curAddress );
        return true;
    }
    
    private DBAddress _curAddress;
    private DBPortPool _curPortPool;
    private final List<DBAddress> _allHosts;

    private final ThreadLocal<MyPort> _threadPort = new ThreadLocal<MyPort>(){
        protected MyPort initialValue(){
            return new MyPort();
        }
    };
        
    private final static JSObjectBase _isMaster = new JSObjectBase();
    static {
        _isMaster.set( "ismaster" , 1 );
        _isMaster.lock();
    }

}
