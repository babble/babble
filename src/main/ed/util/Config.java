// Config.java

package ed.util;

import java.io.*;
import java.util.*;

public class Config extends Properties {
    
    public static Config get(){
        return INSTANCE;
    }
    
    static String _placesToLook[] = new String[]{
        "" , 
        "conf" , 
        "/etc/" , 
        System.getenv( "ED_HOME" ) + "/" ,
        System.getenv( "ED_HOME" ) + "/conf"  ,
        System.getenv( "ED_HOME" ) + "/etc" ,
        ed.db.JSHook.whereIsEd + "/" ,
        ed.db.JSHook.whereIsEd + "/conf" ,
        ed.db.JSHook.whereIsEd + "/etc" ,
    };

    private static String _configFile = "10gen.properties";
    
    private final static Config INSTANCE = new Config();
    
    private Config(){
        super( System.getProperties() );
        
        ClassLoader cl = ClassLoader.getSystemClassLoader();
    
        for ( String place : _placesToLook ){
            File f = new File( place , _configFile );
            
            if ( f.exists() ){
                System.out.println( "loading config file from [" + f + "]" );
                _load( f );
                break;
            }

            InputStream in = cl.getResourceAsStream( place + _configFile );
            if ( in != null ){
                System.out.println( "loading config from [" + place + _configFile + "]" );
                _load( in );
                break;
            }
        }
    }

    private void _load( File f ){
        try {
            _load( new FileInputStream( f ) );
        }
        catch ( FileNotFoundException fnf ){
            System.err.println( "can't find : " + f + " but should be impossible b/c only called if it exists.  exiting" );
            System.exit(-4);
        }
    }

    private void _load( InputStream in ){
        try {
            load( in );
        }
        catch ( IOException ioe ){
            System.err.println( "error reading config file from stream : " + ioe + "  exiting..." );
            System.exit(-4);
        }
    }
    
    public boolean getBoolean( String key ){
        return getBoolean( key , false );
    }
    
    public boolean getBoolean( String key , boolean def ){
        String s = getProperty( key );
        if ( s == null )
            return def;

        if ( s.startsWith( "t" ) || 
             s.startsWith( "T" ) ||
             s.startsWith( "y" ) || 
             s.startsWith( "Y" ) || 
             s.startsWith( "1" ) )
            return true;
        
        if ( s.startsWith( "n" ) || 
             s.startsWith( "N" ) ||
             s.startsWith( "f" ) || 
             s.startsWith( "F" ) || 
             s.startsWith( "0" ) )
            return false;
        
        return def;
    }

    public String getTryEnvFirst( String name , String def ){
        String s = System.getenv( name );
        if ( s != null )
            return s;
        
        return getProperty( name , def );
    }

    public Object setProperty(String key, String value){
        throw new RuntimeException( "can't set something on config" );
    }

    public Object put(String key, String value){
        throw new RuntimeException( "can't set something on config" );
    }

}
