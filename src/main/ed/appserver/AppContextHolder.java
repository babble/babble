// AppContextHolder.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.net.*;
import ed.net.httpserver.*;
import ed.log.*;
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

    /**
     * @param defaultWebRoot default web site
     * @param root where all your sites live
     */
    public AppContextHolder( String defaultWebRoot  , String root ){
        _defaultWebRoot = defaultWebRoot;
        _root = root;
        _rootFile = _root == null ? null : new File( _root );
    }
    
    public Result getContext( HttpRequest request ){
        String host = request.getHeader( "X-Host" );
	String uri = request.getURI();
	
        if ( host != null ){
	    // if there is an X-Host, lets see if this is a cdn thing
	    if ( CDN_HOSTNAMES.contains( request.getHost() ) && 
		 ! CDN_HOSTNAMES.contains( host ) &&
		 ! host.equals( request.getHost() )  // this should never happen, but is a weird case.
		 ){
		// X-Host was cleaned by someone else
		// so we need strip cdn thing from uri.
		int idx = uri.indexOf( "/" , 1 );
		if ( idx > 0 ){
		    uri = uri.substring( idx );
		}
		
	    }
	}
	else { 
	    // no X-Host
            host = request.getHeader( "Host" );
	}

        if ( host != null ){
            int idx = host.indexOf( ":" );
            if ( idx > 0 )
                host = host.substring( 0 , idx );
        }
	
        return getContext( host , uri );
    }

    public Result getContext( String host , String uri ){
        if ( host != null )
            host = host.trim();

        if ( D ) System.out.println( host + uri );

        if ( host == null || _root == null || host.length() == 0 ){
            if ( D ) System.out.println( "\t using default context for [" + host + "]" );
            return new Result( _getDefaultContext() , host , uri );
        }

        Info info = fixBase( host , uri );
        host = info.host;
        uri = info.uri;

        AppContext ac = _getContextFromMap( host );
        if ( ac != null ){
            if ( D ) System.out.println( "\t found in cache [" + host + "]" );
            return new Result( ac , host , uri );
        }
        
        synchronized ( _contextCreationLock ){
            
            ac = _getContextFromMap( host );
            if ( ac != null ){
                if ( D ) System.out.println( "\t found in cache [" + host + "]"  );
                return _finish( ac , host, uri , host );
            }

            for ( Info i : getPossibleSiteNames( info ) ){
                if ( D ) System.out.println( "\t possible site name [" + i.host + "]" );
                File temp = new File( _root , i.host );
                if ( temp.exists() )
                    return _finish( getEnvironmentContext( temp , i , host ) , host , info.uri , host );

                JSObject site = getSiteFromCloud( i.host );
                if ( site != null ){
                    if ( D ) System.out.println( "\t found site from cloud" );
                    temp.mkdirs();
                    return _finish( getEnvironmentContext( temp , i , host ) , host , info.uri , host );
                }
            }
        }
        
        return _finish( _getDefaultContext() , info.host , info.uri , host );
    }
    
    private Result _finish( AppContext context , String host , String uri , String origHost ){
        _contextCache.put( origHost , context );
        return new Result( context , host , uri );
    }

    private AppContext getEnvironmentContext( final File siteRoot , final Info info , final String originalHost ){

        if ( ! siteRoot.exists() )
            throw new RuntimeException( "\t trying to map [" + originalHost + "] to " + siteRoot + " which doesn't exist" );

        AppContext ac = _getContextFromMap( originalHost );
        if ( ac != null )
            return ac;

        if ( D ) System.out.println( "\t mapping directory [" + originalHost + "] to " + siteRoot );

        if ( isCodeDir( siteRoot ) ){
            ac = new AppContext( siteRoot );
        }
        else {

            if ( D ) System.out.println( "\t this is a holder for branches" );

            final String env = info.getEnvironment( originalHost );
            if ( D ) System.out.println( "\t\t env : " + env );

            final File envRoot = getBranch( siteRoot , env , info.host );
            if ( D ) System.out.println( "\t using full path : " + envRoot );
            
            final String envRootString = envRoot.toString();

            ac = _getContextFromMap( envRootString );
            if ( ac == null ){
                ac = new AppContext( envRootString , envRoot , siteRoot.getName() , env );
                _contextCache.put( envRootString , ac );
            }
            
        }

        _contextCache.put( originalHost , ac );
        return ac;
    }

    void replace( AppContext oldOne , AppContext newOne ){
        synchronized ( _contextCreationLock ){
            List<String> names = new ArrayList<String>( _contextCache.keySet() );
            for ( String s : names ){
                AppContext temp = _contextCache.get( s );
                if ( _same( temp , oldOne ) ){
                    _contextCache.put( s , newOne );
                }
            }

            _contextCache.put( newOne._root , newOne );
            
            if ( _same( _defaultContext , oldOne ) )
                _defaultContext = newOne;
            
        }
    }
    
    private boolean _same( AppContext a , AppContext b ){
        if ( a == b )
            return true;

        if ( a == null || b == null )
            return false;

        if ( a._rootFile == null )
            throw new NullPointerException( "how could a's _rootFile be null" );

        if ( b._rootFile == null )
            throw new NullPointerException( "how could b's _rootFile be null" );

        if ( a._rootFile.equals( b._rootFile ) )
            return true;

        return false;
    }

    private AppContext _getContextFromMap( String host ){

        AppContext ac = _contextCache.get( host );

        if (ac != null && ac.isReset()) {
            _contextCache.put( host , null );
            ac = null;
        }

        return ac;
    }

    File getBranch( File root , String subdomain , String siteName ){
        File f = _getBranch( root , subdomain , siteName );

        JSObject envConfig = getEnvironmentFromCloud( siteName , subdomain );
        if ( envConfig != null ){
            GitUtils.fullUpdate( f );
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

        throw new RuntimeException( "can't find environment [" + subdomain + "] in [" + root + "]  siteName [" + siteName + "] found site:" + ( site != null )  );
    }

    static void _checkout( File f , String what ){
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

    private boolean isCodeDir( final File test ){
        File f = new File( test , ".git" );
        if ( f.exists() )
            return true;

        f = new File( test , "dot-git" );
        if ( f.exists() )
            return true;
	
	if ( ! test.exists() )
	    return false;
	
	File lst[] = test.listFiles();
	for ( int j=0; j<lst.length; j++ ){
	    f = lst[j];

	    if ( f.isDirectory() )
		continue;
	    
	    final String name = f.getName();

	    for ( int i=0; i<JSFileLibrary._srcExtensions.length; i++ )
		if ( name.endsWith( JSFileLibrary._srcExtensions[i] ) )
		    return true;

	}

        return false;

    }

    static JSObject getEnvironmentFromCloud( String siteName , String envName ){
        Cloud theCloud = Cloud.getInstanceIfOnGrid();
        if ( theCloud == null )
            return null;
        return theCloud.findEnvironment( siteName , envName );
    }

    private static JSObject getSiteFromCloud( String name ){
        Cloud theCloud = Cloud.getInstanceIfOnGrid();
        if ( theCloud == null )
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


            final int idx = uri.indexOf( "/" , 1 );
	    
            if ( idx < 0 )
                throw new RuntimeException( "static host without valid  host:[" + host + "] uri:[" + uri + "]" );

            host = uri.substring( 1 , idx );
            uri = uri.substring( idx  );
        }

        for ( String d : OUR_DOMAINS ){
            if ( host.endsWith( d ) ){
                host = host.substring( 0 , host.length() - d.length() );
                if ( host.equals( "www" ) || host.equals( "www.www" ) )
                    host = "www";
                else if ( host.indexOf( "." ) < 0 )
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

    class Result {
        
        Result( AppContext context , String host , String uri ){
            this.context = context;
            this.host = host;
            this.uri = uri;
            if ( this.host.equalsIgnoreCase( "www" ) && getRoot().contains( "stage" ) )
                throw new RuntimeException( "blah" );
        }
        
        String getRoot(){
            return context.getRoot();
        }
        
        public String toString(){
            return getRoot() + "||" + host + "||" + uri;
        }

        final AppContext context;
        final String uri;
        final String host;
    }

    final String _root;
    final File _rootFile;

    private final String _defaultWebRoot;
    private AppContext _defaultContext;

    private final Map<String,AppContext> _contextCache = Collections.synchronizedMap( new StringMap<AppContext>(){
            public AppContext put( String name , AppContext c ){
                if ( D ) System.out.println( "adding to cache [" + name + "] -> [" + c + "]" );

                if ( name.equalsIgnoreCase( "www" ) && c.toString().contains( "stage" ) )
                    throw new RuntimeException( "here" );


                return super.put( name , c );
            }
        } );
    private final String _contextCreationLock = ( "AppContextHolder-Lock-" + Math.random() ).intern();
}
