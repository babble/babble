// AppContextHolder.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.net.*;
import ed.net.httpserver.*;
import ed.util.*;
import ed.cloud.*;

public class AppContextHolder {

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

    public AppContextHolder( String defaultWebRoot  , String root ){
        _defaultWebRoot = defaultWebRoot;
        _root = root;
        _rootFile = _root == null ? null : new File( _root );
    }

    public AppContext getContext( HttpRequest request , String newUri[] ){
        return getContext( request.getHeader( "Host" ) , request.getURI() , newUri );
    }
    
    public AppContext getContext( String host , String uri , String newUri[] ){
        if ( newUri != null )
            newUri[0] = null;

        if ( host != null )
            host = host.trim();
        
        if ( D ) System.out.println( host + uri );

        if ( host == null || _root == null || host.length() == 0 ){
            if ( D ) System.out.println( "\t using default context for [" + host + "]" );
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
            if ( D ) System.out.println( "\t possible site name [" + i.host + "]" );
            File temp = new File( _root , i.host );
            if ( temp.exists() )
                return getEnvironmentContext( temp , i , host );
            
            JSObject site = getSiteFromCloud( i.host );
            if ( site != null ){
                if ( D ) System.out.println( "\t found site from cloud" );
                temp.mkdirs();
                return getEnvironmentContext( temp , i , host );
            }
        }
        
        return _getDefaultContext();
    }

    AppContext getEnvironmentContext( final File siteRoot , final Info info , final String originalHost ){

        if ( ! siteRoot.exists() )
            throw new RuntimeException( "\t trying to map [" + originalHost + "] to " + siteRoot + " which doesn't exist" );

        AppContext ac = _getContextFromMap( originalHost );
        if ( ac != null )
            return ac;
        
        if ( D ) System.out.println( "\t mapping directory [" + originalHost + "] to " + siteRoot );
        
        if ( hasGit( siteRoot ) ){
            ac = new AppContext( siteRoot );
        }
        else {

            if ( D ) System.out.println( "\t this is a holder for branches" );
            
            final String env = info.getEnvironment( originalHost );
            if ( D ) System.out.println( "\t\t env : " + env );
            
            File envRoot = getBranch( siteRoot , env , info.host );
            if ( D ) System.out.println( "\t using full path : " + envRoot );
            
            ac = new AppContext( envRoot.toString() , envRoot , siteRoot.getName() , env );
        }

        _context.put( info.host , ac );
        _context.put( originalHost , ac );
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
    
    File getBranch( File root , String subdomain , String siteName ){
        File f = _getBranch( root , subdomain , siteName );

        JSObject envConfig = getEnvironmentFromCloud( siteName , subdomain );
        if ( envConfig != null ){
            GitUtils.fetch( f );
            String branch = envConfig.get( "branch" ).toString() ;
            if ( D ) System.out.println( "\t using branch [" + branch + "]" );
            _checkout( f , branch );
        }
        
        return f;
    }

    File _getBranch( File root , String subdomain , String siteName ){
        File test = new File( root , subdomain );
        if ( test.exists() )
            return test;
        
        JSObject site = getSiteFromCloud( siteName );
        if ( site != null ){
            
            Object gitObject = site.get( "giturl" );
            if ( gitObject != null ){
                
                String giturl = gitObject.toString();
                JSObject envConfig = getEnvironmentFromCloud( siteName , subdomain );
                if ( envConfig != null ){

                    if ( D ) System.out.println( "\t found an env in grid" );
                    if ( ! GitUtils.clone( giturl , root , subdomain ) )
                        throw new RuntimeException( "couldn't clone [" + siteName + "] from [" + giturl + "]" );
                    
                    _checkout( test , envConfig.get( "branch" ).toString() );
                    return test;
                }
            }
        }

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
        
        throw new RuntimeException( "can't find environment [" + subdomain + "] in [" + root + "]" );
    }

    private void _checkout( File f , String what ){
        if ( GitUtils.checkout( f , what ) )
            return;

        if ( GitUtils.checkout( f , "origin/" + what ) )
            return;
        
        throw new RuntimeException( "couldn't checkout [" + what + "] for [" + f + "]" );
    }
    

    private synchronized AppContext _getDefaultContext(){
        if ( _defaultWebRoot == null )
            return null;

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

    private static JSObject getEnvironmentFromCloud( String siteName , String envName ){
        Cloud theCloud = Cloud.getInstance();
        if ( theCloud == null || ! theCloud.isRealServer() )
            return null;
        return theCloud.findEnvironment( siteName , envName );
    }

    private static JSObject getSiteFromCloud( String name ){
        Cloud theCloud = Cloud.getInstance();
        if ( theCloud == null || ! theCloud.isRealServer() )
            return null;
        
        return theCloud.findSite( name , false );
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
        
        Info( String host ){
            this( host , "/" );
        }
        
        Info( String host , String uri ){
            this.host = host;
            this.uri = uri;
        }
        
        String getEnvironment( String big ){

            if ( big.equalsIgnoreCase( host ) ||
                 host.startsWith( "www." ) || 
                 big.startsWith( host + "." ) )
                return "www";

            int idx = big.indexOf( "." + host );
            if ( idx < 0 ){
                idx = big.indexOf( host );
                if ( idx < 0 )
                    throw new RuntimeException( "something is wrong host:" + host + " big:" + big );
            }
            
            return big.substring( 0 , idx );
            
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
