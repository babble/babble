// DBProvider.java

package ed.db;

import java.util.*;

public class DBProvider {

    public static DBApiLayer get( String root ){
        return get( root , null );
    }
    
    public static DBApiLayer get( String root , String ip ){
        if ( ip == null || ip.trim().length() == 0 ){
            ip = System.getenv( "db_ip" );
            if ( ip == null || ip.trim().length() == 0 )
                ip = "127.0.0.1";
        }

        final String key = root + ip;
        
        DBApiLayer db = _roots.get( key );
        if ( db != null )
            return db;
        
        synchronized ( _roots ){
            db = _roots.get( key );
            if ( db != null )
                return db;       
            
            db = create( root , ip );
            _roots.put( key , db );
        }

        return db;

    }

    private static DBApiLayer create( String root , String ip ){
        return new DBTCP( root , ip );
    }
    
    static final Map<String,DBApiLayer> _roots = new HashMap<String,DBApiLayer>();

}
