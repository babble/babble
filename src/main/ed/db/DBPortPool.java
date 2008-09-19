// DBPortPool.java

package ed.db;

import ed.util.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class DBPortPool extends SimplePool<DBPort> {

    public final long _maxWaitTime = 1000 * 60 * 2;

    static DBPortPool get( DBAddress addr ){
        return get( addr.getSocketAddress() );
    }

    static DBPortPool get( InetSocketAddress addr ){
        
        DBPortPool p = _pools.get( addr );
        
        if (p != null) 
            return p;
        
        synchronized (_pools) {
            p = _pools.get( addr );
            if (p != null) {
                return p;
            }
            
            p = new DBPortPool( addr );
            _pools.put( addr , p);
        }

        return p;
    }

    private static final Map<InetSocketAddress,DBPortPool> _pools = Collections.synchronizedMap( new HashMap<InetSocketAddress,DBPortPool>() );

    // ----
    
    public static class NoMoreConnection extends RuntimeException {
	NoMoreConnection(){
	    super( "No more DB Connections" );
	}
    }

    // ----

    DBPortPool( InetSocketAddress addr ){
        super( addr.toString() , 10 , Bytes.CONNECTIONS_PER_HOST );
        _addr = addr;
	_waitingSem = new Semaphore( Math.min( ed.net.httpserver.HttpServer.WORKER_THREAD_QUEUE_MAX / 2 , Bytes.CONNECTIONS_PER_HOST * 5 ) );
    }
    
    public DBPort get(){
	DBPort port = null;
	
	if ( ! _waitingSem.tryAcquire() )
	    throw new NoMoreConnection();

	try {
	    port = get( _maxWaitTime );
	}
	finally {
	    _waitingSem.release();
	}

	if ( port == null )
	    throw new NoMoreConnection();
	
	return port;
    }

    void gotError(){
        System.out.println( "emptying DBPortPool b/c of error" );
        clear();
    }

    public boolean ok( DBPort t ){
        return _addr.equals( t._addr );
    }
    
    protected DBPort createNew(){
        try {
            DBPort p = new DBPort( _addr , this );
            _everWorked = true;
            return p;
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "can't create port to:" + _addr , ioe );
        }
    }

    final private Semaphore _waitingSem;
    final InetSocketAddress _addr;
    boolean _everWorked = false;
}
