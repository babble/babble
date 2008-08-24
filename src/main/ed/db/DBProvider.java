// DBProvider.java

package ed.db;

import java.util.*;

import ed.cloud.*;
import ed.util.Config;
import ed.appserver.*;

public class DBProvider {

    public static DBApiLayer get( String root , AppContext ctxt ){
        if ( ctxt == null )
            throw new IllegalArgumentException( "shouldn't call this version of DBProvider.get without an AppContext" );
        
        String env = ctxt.getEnvironmentName();
        if ( env == null )
            return get( root , false );

        Cloud c = ed.cloud.Cloud.getInstanceIfOnGrid();
        return get( root , false , c == null ? null : c.getDBHostForSite( root , env ) );
    }

    public static DBApiLayer get( String root ){
        return get( root , true , null );
    }

    public static DBApiLayer get( String root , boolean useCache ){
        return get( root , useCache , null );
    }
    
    public static DBApiLayer get( String root , String ip ){
        return get( root , true , ip );
    }

    public static DBApiLayer get( String root , boolean useCache , String ip ){
        
        if ( ip == null || ip.trim().length() == 0 ){
            int idx = root.indexOf( "/" );
            if ( idx > 0 ){
                ip = root.substring( 0 , idx );
                root = root.substring( idx + 1 );
            }
            else {
                ip = getDefaultHost();
            }
        }
        
        int port = Integer.parseInt(Config.get().getTryEnvFirst("db_port", Integer.toString(DBPort.PORT)));
        
        return get( root , useCache , ip , port);
    }
    
    public static DBApiLayer get(String root , String ip, int port){
        return get( root , true , ip , port );
    }

    public static DBApiLayer get(String root , boolean useCache , String ip, int port){

        if ( ! useCache )
            return create( root , ip , port );

        final String key = root + ":" + ip + ":" + port;
        
        DBApiLayer db = _roots.get( key );
        
        if ( db != null ) {
            return db;
        }
        
        synchronized ( _roots ){
            db = _roots.get( key );
            if ( db != null ) {
                return db;       
            }
            
            db = create(root , ip, port);
            _roots.put( key , db );
        }

        return db;

    }

    static String getDefaultHost(){
        return Config.get().getTryEnvFirst( "db_ip" , "127.0.0.1" );
    }

    private static DBApiLayer create(String root , String ip, int port){

        final int colon = ip.lastIndexOf( ":" );
        if ( colon > 0 && colon + 1 < ip.length() ){
            boolean allDigits = true;

            for ( int i=colon+1; i<ip.length(); i++ ){
                if ( ! Character.isDigit( ip.charAt(i) ) ){
                    allDigits = false;
                    break;
                }
            }
            
            if ( allDigits ){
                port = Integer.parseInt( ip.substring( colon + 1 ) );
                ip = ip.substring( 0 , colon );
            }
        }

        if ( ip.indexOf( "." ) < 0 && ip.indexOf( "-" ) < 0 ){
            // we're going to assume its not a host or an ip, but a db name.
            Cloud c = Cloud.getInstanceIfOnGrid();
            if ( c != null ){
                try {
                    String temp = c.getDBHost( ip );
                    if ( temp != null )
                        ip = temp;
                }
                catch ( Exception e ){
                    // don't care
                }
            }
        }

    	System.out.println("DBApiLayer : DBTCP : " + ip + ":" + port + "/" + root);
        return new DBTCP( root , ip, port);
    }
    
    static final Map<String,DBApiLayer> _roots = new HashMap<String,DBApiLayer>();
}
