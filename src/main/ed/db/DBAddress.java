// DBAddress.java

package ed.db;

import java.net.*;

import ed.net.*;
import ed.util.*;
import ed.cloud.*;

public class DBAddress {
    
    /**
     * name
     * <host>/name
     * <host>:<port>/name
     */
    public DBAddress( String urlFormat )
        throws UnknownHostException {
        _check( urlFormat , "urlFormat" );
        
        int idx = urlFormat.indexOf( "/" );
        if ( idx < 0 ){
            _host = defaultHost();
            _port = defaultPort();
            _name = urlFormat;
        }
        else {
            _name = urlFormat.substring( idx + 1 ).trim();
            urlFormat = urlFormat.substring( 0 , idx ).trim();
            idx = urlFormat.indexOf( ":" );
            if ( idx < 0 ){
                _host = urlFormat.trim();
                _port = defaultPort();
            }
            else {
                _host = urlFormat.substring( 0 , idx );
                _port = Integer.parseInt( urlFormat.substring( idx + 1 ) );
            }
        }
        
        _check( _host , "host" );
        _check( _name , "name" );
        _addr = _getAddress( _host );

        if ( _name.contains( "." ) )
            throw new RuntimeException( "db names can't have periods in them [" + _name + "]" );
    }

    public DBAddress( DBAddress other , String name )
        throws UnknownHostException {
        this( other._host , other._port , name );
    }

    public DBAddress( String host , String name )
        throws UnknownHostException {
        this( host , DBPort.PORT , name );
    }
    
    public DBAddress( String host , int port , String name )
        throws UnknownHostException {
        _check( host , "host" );
        _check( name , "name" );
        
        _host = host.trim();
        _port = port;
        _name = name.trim();
        _addr = _getAddress( _host );
    }

    static void _check( String thing , String name ){
        if ( thing == null )
            throw new NullPointerException( name + " can't be null " );
        
        thing = thing.trim();
        if ( thing.length() == 0 )
            throw new IllegalArgumentException( name + " can't be empty" );
    }

    public int hashCode(){
        return _host.hashCode() + _port + _name.hashCode();
    }

    public boolean equals( Object other ){
        if ( other instanceof DBAddress ){
            DBAddress a = (DBAddress)other;
            return 
                a._port == _port &&
                a._name.equals( _name ) &&
                a._host.equals( _host );
        }
        return false;
    }

    public String toString(){
        return _host + ":" + _port + "/" + _name;
    }

    public InetSocketAddress getSocketAddress(){
        return new InetSocketAddress( _addr , _port );
    }

    public boolean sameHost( String host ){
        int idx = host.indexOf( ":" );
        int port = defaultPort();
        if ( idx > 0 ){
            port = Integer.parseInt( host.substring( idx + 1 ) );
            host = host.substring( 0 , idx );
        }

        return 
            _port == port &&
            _host.equalsIgnoreCase( host );
    }

    final String _host;
    final int _port;
    final String _name;
    final InetAddress _addr;
    
    private static InetAddress _getAddress( String host )
        throws UnknownHostException {
        return DNSUtil.getByName( _checkCloudHostName( host ) );
    }

    private static String _checkCloudHostName( final String host ){
        if ( host.contains( "." ) )
            return host;

        Cloud c = Cloud.getInstanceIfOnGrid();
        if ( c == null )
            return host;
        
        try {
            String temp = c.getDBHost( host );
            if ( temp != null )
                return temp;
        }
        catch ( Exception e ){} // don't care

        return host;
    }

    static String defaultHost(){
        return Config.get().getTryEnvFirst( "db_ip" , "127.0.0.1" );
    }

    static int defaultPort(){
        return Integer.parseInt(Config.get().getTryEnvFirst("db_port", Integer.toString(DBPort.PORT)));
    }
    
}
