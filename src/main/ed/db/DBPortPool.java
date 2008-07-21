// DBPortPool.java

package ed.db;

import ed.util.*;

import java.io.*;
import java.util.*;

class DBPortPool extends SimplePool<DBPort> {

    static DBPortPool get(String ip, int port){
        
        String poolID = ip + ":" + port;
        
        DBPortPool p = _pools.get(poolID);
        
        if (p != null) {
            return p;
        }
        
        synchronized (_pools) {
            p = _pools.get(poolID);
            if (p != null) {
                return p;
            }
            
            p = new DBPortPool(ip, port);
            _pools.put(poolID, p);
        }

        return p;
    }

    static DBPortPool get( String ip){
        DBPortPool p = _pools.get( ip );
        if ( p != null )
            return p;

        synchronized ( _pools ){
            p = _pools.get( ip );
            if ( p != null )
                return p;
            
            p = new DBPortPool( ip );
            _pools.put( ip , p );
        }

        return p;
    }
    
    private static final Map<String,DBPortPool> _pools = Collections.synchronizedMap( new HashMap<String,DBPortPool>() );

    // ----
    

    DBPortPool( String ip ){
        this( ip , DBPort.PORT);
    }

    DBPortPool( String ip, int port){
        super( "DBPortPool:" + ip + ":" + port, 10 , Bytes.CONNECTIONS_PER_HOST );
        _ip = ip;
        _port = port;
    }

    void gotError(){
        System.out.println( "emptying DBPortPool b/c of error" );
        clear();
    }
    
    protected DBPort createNew(){
        try {
            DBPort p = new DBPort( _ip, _port , this );
            _everWorked = true;
            return p;
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "can't create port to:" + _ip + ":" + _port, ioe );
        }
    }
    
    final String _ip;
    final int _port;
    boolean _everWorked = false;
}
