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

    protected void say( int op , ByteBuffer buf ){
        DBPort port = _portPool.get();
                
        try {
            port.say( new DBMessage( op , buf ) );
            _portPool.done( port );
        }
        catch ( IOException ioe ){
            throw new JSException( "can't say something" );
        }
    }
    
    protected int call( int op , ByteBuffer out , ByteBuffer in ){
        DBPort port = _portPool.get();
        
        try {
            DBMessage a = new DBMessage( op , out );
            DBMessage b = port.call( a , in );
            _portPool.done( port );
            return b.dataLen();
        }
        catch ( IOException ioe ){
            throw new JSException( "can't call something" , ioe );
        }
    }

    public String getConnectPoint(){
	return _host;
    }
    
    private final String _host;
    private final DBPortPool _portPool;
}
