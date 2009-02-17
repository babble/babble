// GridMapping.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.net.lb;

import java.net.*;
import java.util.*;

import ed.js.*;
import ed.db.*;
import ed.log.*;
import ed.net.*;
import ed.net.httpserver.*;
import ed.cloud.*;
import static ed.appserver.AppContextHolder.*;

public abstract class MappingBase implements Mapping {

    protected MappingBase( String name ){
        _name = name;
        _logger = Logger.getLogger( "LB" ).getChild( name );
        _logger.setLevel( Level.DEBUG );
    }

    public String getPool( HttpRequest request ){
	return getPool( getEnvironment( request ) );
    }
    
    public Environment getEnvironment( HttpRequest request ){
        Info info = fixBase( request.getHost() , request.getURI() );
        Info site = info;
        
        if ( ! _sites.containsKey( site.getHost() ) ){
            for ( Info i : getPossibleSiteNames( info ) ){
                if ( _sites.containsKey( i.getHost() ) ){
                    site = i;
                    break;
                }
            }
        }

        final String name = site.getHost(); // this is the site name (alleyinsider,shopwiki.com.au)
        
        String env = site.getEnvironment( info.getHost() );
        String useHost = request.getHost();
        
        final SiteInfo si = _sites.get( name );
        if ( si != null ){
            final String real = si.getAlias( env );
            if ( real != null ){
                if ( useHost.startsWith( env + "." ) ){
                    int idx = useHost.indexOf( "." , env.length() );
                    useHost = real + useHost.substring( idx );
                }
                env = real;
                                
            }
        }

        return new Environment( name , env , useHost );
    }
    
    public String getPool( Environment e ){
        
        SiteInfo si = _sites.get( e.site );
        if ( si == null ){
            _logger.info( "no site for [" + e.site + "] return default pool [" + _defaultPool + "]" );
            return _defaultPool;
        }
        
        final String pool = si.getPool( e.env );
        
        if ( pool == null ){
            _logger.error( "no env [" + e.env + "] for site [" + e.site + "]" );
            return _defaultPool;
        }

        return pool;
    }
    
    public List<InetSocketAddress> getAddressesForPool( String pool ){
        return _pools.get( pool );
    }

    public String toString(){
        return _name;
    }

    protected void addAddressToPool( String pool , String addr ){
        int port = 8080;
        int idx = addr.indexOf( ":" );
        if ( idx > 0 ){
            port = Integer.parseInt( addr.substring( idx + 1 ) );
            addr = addr.substring(0,idx);
        }
        addAddressToPool( pool , new InetSocketAddress( addr , port ) );
    }
    
    protected void addAddressToPool( String pool , InetSocketAddress addr ){
        _logger.debug( "Adding to pool [" + pool + "] address [" + addr + "]" );
        List<InetSocketAddress> lst = _pools.get( pool );
        if ( lst == null ){
            lst = new ArrayList<InetSocketAddress>();
            _pools.put( pool , lst );
        }
        lst.add( addr );
    }
    
    protected void addSiteMapping( String site , String env , String pool ){
        _logger.debug( "Adding mapping for site [" + site + "] [" + env + "] -> [" + pool + "]" );
        getSiteInfo( site ).addEnv( env , pool );
    }

    protected void addSiteAlias( String site , String alias , String real ){
        _logger.debug( "Adding alias for site [" + site + "] [" + alias + "] -> [" + real + "]" );
        getSiteInfo( site ).addAlias( alias , real );
    }

    protected void setDefaultPool( String pool ){
        _defaultPool = pool;
    }

    protected void blockIp( String addr ){
        _blockedIps.add( addr );
    }

    protected void blockUrl( String url ){
        int idx = url.indexOf( "/" );
        if ( idx < 0 )
            blockUrl( url , "" );
        else
            blockUrl( url.substring( 0 , idx ) , url.substring( idx + 1 ) );
    }

    protected void blockUrl( String host , String uri ){
        Set<String> s = _blockedUrls.get( host );
        if ( s == null ){
            s = new TreeSet<String>();
            _blockedUrls.put( host , s );
        }
        s.add( uri );
    }

    public List<String> getPools(){
        return new ArrayList<String>( _pools.keySet() );
    }

    public String toFileConfig(){

        StringBuilder buf = new StringBuilder( 1024 );

        for ( String site : _sites.keySet() ){
            SiteInfo si = _sites.get( site );
            
            buf.append( "site " ).append( site ).append( "\n" );
            for ( Map.Entry<String,String> e : si._environmentsToPools.entrySet() )
                buf.append( "\t" ).append( e.getKey() ).append( " : " ).append( e.getValue() ).append( "\n" );

            if ( si._aliases.size() > 0 ){
                buf.append( "site-alias " ).append( site ).append( "\n" );
                for ( Map.Entry<String,String> e : si._aliases.entrySet() )
                    buf.append( "\t" ).append( e.getKey() ).append( " : " ).append( e.getValue() ).append( "\n" );
            }

            buf.append( "\n" );

        }

        for ( String pool : _pools.keySet() ){
            buf.append( "pool " ).append( pool ).append( "\n" );
            for ( InetSocketAddress addr : _pools.get( pool ) ){
                buf.append( "\t" ).append( addr.getHostName() );
                if ( addr.getPort() != 8080 )
                    buf.append( ":" ).append( addr.getPort() );
                buf.append( "\n" );
            }
            buf.append( "\n" );
        }
        
        if ( _defaultPool != null )
            buf.append( "default pool " ).append( _defaultPool ).append( "\n\n" );

        for ( String addr : _blockedIps )
            buf.append( "block ip " ).append( addr ).append( "\n\n" );

        for ( String site : _blockedUrls.keySet() ){
            for ( String uri : _blockedUrls.get( site ) )
                buf.append( "block url " ).append( site ).append( "/" ).append( uri ).append( "\n\n" );
        }
        
        return buf.toString();
    }

    public boolean reject( HttpRequest request ){
        return 
            rejectIp( request.getPhysicalRemoteAddr() ) ||
            rejectUrl( request.getHost() , request.getURI() );
    }

    boolean rejectIp( String addr ){
        return _blockedIps.contains( addr );
    }

    boolean rejectUrl( String url ){
        int idx = url.indexOf( "/" );
        if ( idx < 0 )
            return rejectUrl( url , "" );
        return rejectUrl( url.substring( 0 , idx ) , url.substring( idx + 1 ) );
    }

    boolean rejectUrl( String host , String uri ){
        if ( host == null )
            return false;
        
        Set<String> s = _blockedUrls.get( host );
        if ( s == null )
            return false;
        return s.contains( uri );
    }
    
    SiteInfo getSiteInfo( String site ){
        return getSiteInfo( site , true );
    }
    
    SiteInfo getSiteInfo( String site , boolean create ){
        SiteInfo si = _sites.get( site );
        if ( si == null && create ){
            si = new SiteInfo();
            _sites.put( site , si );
        }
        return si;
    }
    
    class SiteInfo {
        
        String getPool( String env ){
            return _environmentsToPools.get( env );
        }
        
        void addEnv( String env , String pool ){
            _environmentsToPools.put( env , pool );
        }
        
        void addAlias( String alias , String real ){
            if ( alias.equals( "*" ) )
                _wildcardEnv = real;
            else 
                _aliases.put( alias , real );
        }

        String getAlias( String alias ){

            String real = _aliases.get( alias );

            if ( real != null )
                return real;

            if ( _environmentsToPools.containsKey( alias ) )
                return null;

            return _wildcardEnv;
        }
        
        final Map<String,String> _environmentsToPools = new TreeMap<String,String>();
        final Map<String,String> _aliases = new TreeMap<String,String>();
        String _wildcardEnv = null;
    }
    
    protected final String _name;
    final Logger _logger;

    private String _defaultPool;
    final private Map<String,SiteInfo> _sites = new TreeMap<String,SiteInfo>();
    final private Map<String,List<InetSocketAddress>> _pools = new TreeMap<String,List<InetSocketAddress>>();

    final private Set<String> _blockedIps = new TreeSet<String>();
    final private Map<String,Set<String>> _blockedUrls = new TreeMap<String,Set<String>>(); // host -> uris
}


