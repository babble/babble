// DBPortPool.java

package ed.db;

import ed.util.*;

import java.io.*;
import java.util.*;

class DBPortPool extends SimplePool<DBPort> {

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
        super( "DBPortPool:" + ip , 10 , 20 );
        _ip = ip;
    }
    
    protected DBPort createNew(){
        try {
            return new DBPort( _ip );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "can't create port to:" + _ip , ioe );
        }
    }
    
    final String _ip;
}
