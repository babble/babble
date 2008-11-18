// DBProvider.java

package ed.db;

import java.net.*;
import java.util.*;

import ed.util.*;
import ed.cloud.*;
import ed.appserver.*;

public class DBProvider {

    public static DBBase get( final AppContext ctxt ){
        return get( ctxt , ctxt.getName() );
    }

    public static DBBase get( final AppContext ctxt , final String name ){
        if ( ctxt == null )
            throw new NullPointerException( "shouldn't call this version of DBProvider.get without an AppContext" );
        
        if ( name == null )
            throw new NullPointerException( "need to specify name" );

        String env = ctxt.getEnvironmentName();
        Cloud c = ed.cloud.Cloud.getInstanceIfOnGrid();
        
        if ( env == null || c == null ){
            try {
                String fullName = name;
                if ( fullName.indexOf( ":" ) < 0 ){
                    env = Config.get().getTryEnvFirst( "db_env" , null );
                    if ( env != null && env.trim().length() > 0 ){
                        fullName += ":" + env.trim();
                    }
                }
                return get( fullName , false );
            }
            catch ( UnknownHostException un ){
                throw new RuntimeException( "can't connect to default db host" , un );
            }
        }

        final DBAddress addr = c.getDBAddressForSite( name , env , true );
        if ( addr == null )
            throw new RuntimeException( "can't find db address for site [" + ctxt.getName() + "] environment [" + env + "]" );
        return get( addr , false );
    }

    public static DBBase getSisterDB( DBBase base , String name ){
        AppContext ctxt = AppContext.findThreadLocal();
        if ( ctxt != null )
            return get( ctxt , name );
        
        try {
            DBAddress addr = base.getAddress();
            if ( addr == null )
                return get( name );
        
            return get( new DBAddress( addr , name ) );    
        }
        catch ( UnknownHostException un ){
            throw new RuntimeException( "can't get db connection" , un );
        }
        
    }
    
    public static DBBase get( String urlFormat )
        throws UnknownHostException {
        return get( urlFormat , true );
    }

    public static DBBase get( String urlFormat , boolean useCache )
        throws UnknownHostException {
        return get( new DBAddress( urlFormat ) , useCache );
    }

    public static DBBase get( DBAddress addr ){
        return get( addr , true );
    }

    public static DBBase get( DBAddress addr , boolean useCache ){

        if ( ! useCache )
            return new DBTCP( addr );

        DBBase db = _connections.get( addr );
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
    
    static final Map<DBAddress,DBBase> _connections = Collections.synchronizedMap( new HashMap<DBAddress,DBBase>() );
}
