// DBPortPool.java

package ed.db;

import ed.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

class DBPortPool extends SimplePool<DBPort> {

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
    

    DBPortPool( InetSocketAddress addr ){
        super( addr.toString() , 10 , Bytes.CONNECTIONS_PER_HOST );
        _addr = addr;
    }

    void gotError(){
        System.out.println( "emptying DBPortPool b/c of error" );
        clear();
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
    
    final InetSocketAddress _addr;
    boolean _everWorked = false;
}
