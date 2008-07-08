// DBTCP.java

package ed.db;

import java.io.*;
import java.nio.*;

import ed.js.*;

public class DBTCP extends DBMessageLayer {

    DBTCP( String root , String ip, int port){
        super( root );
        _portPool = DBPortPool.get( ip, port);
	_host = ip;
    }

    public void requestStart(){
        _threadPort.get().requestStart();
    }
    
    public void requestDone(){
        _threadPort.get().requestDone();
    }

    protected void say( int op , ByteBuffer buf ){
        MyPort mp = _threadPort.get();
        DBPort port = mp.get( true );
                
        try {
            port.say( new DBMessage( op , buf ) );
            mp.done( port );
        }
        catch ( IOException ioe ){
            mp.error();
            throw new JSException( "can't say something" , ioe );
        }
    }
    
    protected int call( int op , ByteBuffer out , ByteBuffer in ){
        MyPort mp = _threadPort.get();
        DBPort port = mp.get( false );
        
        try {
            DBMessage a = new DBMessage( op , out );
            DBMessage b = port.call( a , in );
            mp.done( port );
            return b.dataLen();
        }
        catch ( IOException ioe ){
            mp.error();
            throw new JSException( "can't call something" , ioe );
        }
    }
    
    public String getConnectPoint(){
	return _host;
    }


    class MyPort {
        
        DBPort get( boolean keep ){
            if ( _port != null )
                return _port;
            
            DBPort p = _portPool.get();
            if ( keep && _inRequest )
                _port = p;
            return p;
        }
        
        void done( DBPort p ){
            if ( p != _port )
                _portPool.done( p );
        }

        void error(){
            _port = null;
            _portPool.gotError();
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
                _portPool.done( _port );
            _port = null;
            _inRequest = false;
        }

        DBPort _port;
        boolean _inRequest;
    }
    
    private final String _host;
    private final DBPortPool _portPool;

    private final ThreadLocal<MyPort> _threadPort = new ThreadLocal<MyPort>(){
        protected MyPort initialValue(){
            return new MyPort();
        }
    };
        

}
