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

        AppContext ac = _getContextFromMap( host );
        if ( ac != null )
            return ac;

        {
            int colon = host.indexOf( ":" );
            if ( colon > 0 )
                host = host.substring( 0 , colon );
        }

        // raw {admin.latenightcoders.com}
        File temp = new File( _root , host );
        if ( temp.exists() )
            return getFinalContext( temp , host , host );
        
        // check for virtual hosting under us 
        // foo.latenightcoders.com -> foo.com
        String useHost = host;
        for ( String d : OUR_DOMAINS ){
            if ( useHost.endsWith( d ) ){
                useHost = useHost.substring( 0 , useHost.length() - d.length() ) + ".com";
                break;
            }
        }
        if ( useHost.startsWith( "www." ) )
            useHost = useHost.substring( 4 );

        if ( uri != null && uri.length() > 0 && uri.indexOf( "/" , 1 ) > 0 ){
            for ( String d : CDN_HOST ){
                if ( useHost.startsWith( d ) ){
                    String thing = uri.substring(1);

                    int idx = thing.indexOf( "/" );
                    String newUriNow = thing.substring( idx );                    
                    thing = thing.substring( 0 , thing.indexOf( "/" ) );
                    
                    if ( newUri != null )
                        newUri[0] = newUriNow;
                    return getContext( thing , newUriNow , null );
                }
            }
        }
	
	if ( useHost.equals( "corejs.com" ) ){
	    return _coreContext;
	}

        if ( useHost.equals( "com" ) )
            useHost = "www.com";

        if ( D ) System.out.println( "useHost : " + useHost );
        
        // check for full host
        temp = new File( _root , useHost );
        if ( temp.exists() )
            return getFinalContext( temp , host , useHost );
        
        // domain www.{alleyinsider.com}
        String domain = useHost.indexOf(".") >= 0 ? DNSUtil.getDomain( useHost ) : useHost;
        temp = new File( _rootFile , domain );
        if ( temp.exists() )
            return getFinalContext( temp , host , useHost );

        // just name www.{alleyinsider}.com
        int idx = domain.indexOf( "." );
        if ( idx > 0 ){
            temp = new File( _rootFile , domain.substring( 0 , idx ) );
            if ( temp.exists() )
                return getFinalContext( temp , host , useHost );
        }

        return _getDefaultContext();
    }

    AppContext getFinalContext( final File dir , String host , String useHost ){
        if ( ! dir.exists() )
            throw new RuntimeException( "trying to map [" + host + "] to " + dir + " which doesn't exist" );

        AppContext ac = _getContextFromMap( host );
        if ( ac != null )
            return ac;
        
        ac = _getContextFromMap( dir.toString() );
        if ( ac != null )
            return ac;

        File f = dir;
        
        if ( D ) System.out.println( "mapping directory [" + host + "] to " + f );
        
        if ( hasGit( f ) ){
            ac = new AppContext( f );
        }
        else {
            if ( D ) System.out.println( "\t this is a holder for branches" );
            f = getBranch( f , DNSUtil.getSubdomain( useHost ) );
            if ( D ) System.out.println( "\t using full path : " + f );
            
            ac = new AppContext( f.toString() , dir.getName() , f.getName() );
        }

        _context.put( host , ac );
        _context.put( f.toString() , ac );
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
    
    final String _root;
    final File _rootFile;

    private final String _defaultWebRoot;
    private AppContext _defaultContext;

    private final AppContext _coreContext = new AppContext( CoreJS.get().getRootFile( null ) );

    private final Map<String,AppContext> _context = Collections.synchronizedMap( new StringMap<AppContext>() );
}
