// AppContextHolder.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.net.*;
import ed.net.httpserver.*;
import ed.util.*;

class AppContextHolder {

    static boolean D = Boolean.getBoolean( "DEBUG.APP" );

    static String OUR_DOMAINS[] = new String[]{ ".latenightcoders.com" , ".local.10gen.com" , ".10gen.com" };
    static String CDN_HOST[] = new String[]{ "origin." , "origin-local." , "static." , "static-local." , "secure." };

    static final Set<String> CDN_HOSTNAMES;
    static {
        Set<String> s = new HashSet<String>();
        
        for ( String d : OUR_DOMAINS )
            for ( String h : CDN_HOST )
                s.add( (h + d).replaceAll( "\\.+" , "." ) );
        
        CDN_HOSTNAMES = Collections.unmodifiableSet( s );
    }

    private static final String LOCAL_BRANCH_LIST[] = new String[]{ "master" , "test" , "www" };
    private static final String WWW_BRANCH_LIST[] = new String[]{ "test" , "master" };

    AppContextHolder( String defaultWebRoot  , String root ){
        _defaultWebRoot = defaultWebRoot;
        _root = root;
        _rootFile = _root == null ? null : new File( _root );
    }

    AppContext getContext( HttpRequest request , String newUri[] ){
        return getContext( request.getHeader( "Host" ) , request.getURI() , newUri );
    }

    AppContext getContext( String host , String uri , String newUri[] ){
        if ( newUri != null )
            newUri[0] = null;

        if ( host != null )
            host = host.trim();
        
        if ( host == null || _root == null || host.length() == 0 ){
            if ( D ) System.out.println( " using default context for [" + host + "]" );
            return _getDefaultContext();
        }
        
        Info info = fixBase( host , uri );
        host = info.host;
        uri = info.uri;
        if ( newUri != null )
            newUri[0] = info.uri;

	if ( host.equals( "corejs.com" ) )
	    return _getCoreContext();

        AppContext ac = _getContextFromMap( host );
        if ( ac != null )
            return ac;

        for ( Info i : getPossibleSiteNames( info ) ){
            File temp = new File( _root , i.host );
            if ( temp.exists() )
                return getFinalContext( temp , i , host );
        }
        
        return _getDefaultContext();
    }

    AppContext getFinalContext( final File dir , final Info info , String host ){
        if ( ! dir.exists() )
            throw new RuntimeException( "trying to map [" + host + "] to " + dir + " which doesn't exist" );

        AppContext ac = _getContextFromMap( host );
        if ( ac != null )
            return ac;
        
        File f = dir;
        
        if ( D ) System.out.println( "mapping directory [" + host + "] to " + f );
        
        if ( hasGit( f ) ){
            ac = new AppContext( f );
        }
        else {
            if ( D ) System.out.println( "\t this is a holder for branches" );
            f = getBranch( f , DNSUtil.getSubdomain( host ) );
            if ( D ) System.out.println( "\t using full path : " + f );
            
            ac = new AppContext( f.toString() , dir.getName() , f.getName() );
        }

        _context.put( info.host , ac );
        _context.put( host , ac );
        return ac;
    }

    private AppContext _getContextFromMap( String host ){

        AppContext ac = _context.get( host );
        
        if (ac != null && ac.isReset()) {
            _context.put( host , null );
            ac = null;
        }
        
        return ac;
    }

    File getBranch( File root , String subdomain ){
        File test = new File( root , subdomain );
        if ( test.exists() )
            return test;
        
        if ( subdomain.equals( "dev" ) ){
            test = new File( root , "master" );
            if ( test.exists() )
                return test;
        }
        
        String searchList[] = null;
        
        if ( subdomain.equals( "local" ) )
            searchList = LOCAL_BRANCH_LIST;
        else if ( subdomain.equals( "www" ) )
            searchList = WWW_BRANCH_LIST;

        if ( searchList != null ){
            for ( int i=0; i<searchList.length; i++ ){
                test = new File( root , searchList[i] );
                if ( test.exists() )
                    return test;
            }
        }
        
        throw new RuntimeException( "can't find branch for subdomain : " + subdomain );
    }

    private synchronized AppContext _getDefaultContext(){
        if ( _defaultContext != null && _defaultContext._reset )
            _defaultContext = null;
        
        if ( _defaultContext != null )
            return _defaultContext;
        
        _defaultContext = new AppContext( _defaultWebRoot );
        return _defaultContext;
    }

    private boolean hasGit( File test ){
        File f = new File( test , ".git" );
        if ( f.exists() )
            return true;

        f = new File( test , "dot-git" );
        if ( f.exists() )
            return true;

        return false;
        
    }

    static List<Info> getPossibleSiteNames( String host , String uri ){
        return getPossibleSiteNames( fixBase( host , uri ) );
    }
    
    static List<Info> getPossibleSiteNames( Info base ){

        List<Info> all = new ArrayList<Info>( 6 );
        all.add( base );
        
        final String host = base.host;
        final String uri = base.uri;
        
        String domain = DNSUtil.getDomain( host );
        if ( ! domain.equals( host ) )
            all.add( new Info( domain , uri ) );
        
        int idx = domain.indexOf( "." );
        if ( idx > 0 )
            all.add( new Info( domain.substring( 0 , idx ) , uri ) );

        return all;
    }
    
    static Info fixBase( String host , String uri ){
        
        {
            int idx = host.indexOf( ":" );
            if ( idx >= 0 )
                host = host.substring( 0 , idx );
        }

        if ( uri == null ){
            uri = "/";
        }
        else {
            if ( ! uri.startsWith( "/" ) )
                uri = "/" + uri;
        }

        if ( CDN_HOSTNAMES.contains( host ) ){

            if ( uri.indexOf( "/" , 1 ) < 0 )
                throw new RuntimeException( "static host without valid  host:[" + host + "] uri:[" + uri + "]" );
            
            final int idx = uri.indexOf( "/" , 1 );
            host = uri.substring( 1 , idx );
            uri = uri.substring( idx  );
        }
        
        for ( String d : OUR_DOMAINS ){
            if ( host.endsWith( d ) ){
                host = host.substring( 0 , host.length() - d.length() );
                if ( host.indexOf( "." ) < 0 )
                    host += ".com";
                break;
            }
        }
        
        if ( host.startsWith( "www." ) )
            host = host.substring( 4 );

        if ( host.equals( "com" ) )
            host = "www.com";

        return new Info( host , uri );
    }
    
    static class Info {
        
        Info( String host , String uri ){
            this.host = host;
            this.uri = uri;
        }

        public String toString(){
            return host + uri;
        }

        final String host;
        final String uri;
    }

    private synchronized AppContext _getCoreContext(){
        if ( _coreContext == null )
            _coreContext = new AppContext( CoreJS.get().getRootFile( null ) );
        return _coreContext;
    }

    final String _root;
    final File _rootFile;

    private final String _defaultWebRoot;
    private AppContext _defaultContext;

    private AppContext _coreContext;

    private final Map<String,AppContext> _context = Collections.synchronizedMap( new StringMap<AppContext>() );
}
