// DBProvider.java

package ed.db;

import java.net.*;
import java.util.*;

import ed.cloud.*;
import ed.appserver.*;

public class DBProvider {

    public static DBApiLayer get( final AppContext ctxt ){
        return get( ctxt , ctxt.getName() );
    }

    public static DBApiLayer get( final AppContext ctxt , final String name ){
        if ( ctxt == null )
            throw new NullPointerException( "shouldn't call this version of DBProvider.get without an AppContext" );
        
        if ( name == null )
            throw new NullPointerException( "need to specify name" );

        String env = ctxt.getEnvironmentName();
        Cloud c = ed.cloud.Cloud.getInstanceIfOnGrid();
        
        if ( env == null || c == null ){
            try {
                return get( name , false );
            }
            catch ( UnknownHostException un ){
                throw new RuntimeException( "can't connect to default db host" , un );
            }
        }

        return get( c.getDBAddressForSite( name , ctxt.getEnvironmentName() ) , false );
    }

    public static DBApiLayer getSisterDB( DBBase base , String name ){
        AppContext ctxt = AppContext.findThreadLocal();
        if ( ctxt != null )
            return get( ctxt , name );
        
        throw new RuntimeException( "not implemented yet" );
    }
    
    public static DBApiLayer get( String urlFormat )
        throws UnknownHostException {
        return get( urlFormat , true );
    }

    public static DBApiLayer get( String urlFormat , boolean useCache )
        throws UnknownHostException {
        return get( new DBAddress( urlFormat ) , useCache );
    }

    public static DBApiLayer get( DBAddress addr ){
        return get( addr , true );
    }

    public static DBApiLayer get( DBAddress addr , boolean useCache ){

        if ( ! useCache )
            return new DBTCP( addr );

        DBApiLayer db = _connections.get( addr );
        if ( db != null )
            return db;
        
        synchronized ( _connections ){
            db = _connections.get( addr );
            if ( db != null )
                return db;       
            
            db = new DBTCP( addr );
            _connections.put( addr , db );
        }
        
        return db;
    }
    
    static final Map<DBAddress,DBApiLayer> _connections = Collections.synchronizedMap( new HashMap<DBAddress,DBApiLayer>() );
}
