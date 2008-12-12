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
import ed.git.*;
import ed.util.*;
import ed.lang.*;
import ed.cloud.*;

public class AppContextHolder {

    static boolean D = Boolean.getBoolean( "DEBUG.APP" );

    static final String CDN_HOST[] = new String[]{ "origin." , "origin-local." , "static." , "static-local." , "secure." };
    static final String OUR_DOMAINS[];
    static {
        List<String> ourDomains = new ArrayList<String>();
        
        ourDomains.add( ".local." + Config.getExternalDomain().toLowerCase() );
        ourDomains.add( "." + Config.getExternalDomain().toLowerCase() );
        
        String externalDomainAliases = Config.get().getProperty( "externalDomainAliases" );
        if ( externalDomainAliases != null ){
            for ( String s : externalDomainAliases.split( "," ) ){
                s = s.trim().toLowerCase();
                if ( s.length() == 0 )
                    continue;
                
                ourDomains.add( "." + s );
            }
        }

        OUR_DOMAINS = ourDomains.toArray( new String[ ourDomains.size() ] );
        if ( D ) System.out.println( "OUR_DOMAINS: " + ourDomains );
    }
    
    static final Set<String> CDN_HOSTNAMES;
    static {
        Set<String> s = new HashSet<String>();

        for ( String d : OUR_DOMAINS )
            for ( String h : CDN_HOST )
                s.add( (h + d).replaceAll( "\\.+" , "." ).toLowerCase() );

        CDN_HOSTNAMES = Collections.unmodifiableSet( s );
    }

    private static final String LOCAL_BRANCH_LIST[] = new String[]{ "master" , "test" , "www" };
    private static final String WWW_BRANCH_LIST[] = new String[]{ "test" , "master" };

    public static boolean isCDNHost( String host ){
        return CDN_HOSTNAMES.contains( host );
    }

    /**
     * @param defaultWebRoot default web site
     * @param root where all your sites live
     */
    public AppContextHolder( String defaultWebRoot  , String root ){
        _defaultWebRoot = defaultWebRoot;
        _root = root;
        _rootFile = _root == null ? null : new File( _root );
    }

    public void addToServer(){
        HttpServer.addGlobalHandler( new HttpMonitor( "appcontextholder" ){
                
                public void handle( MonitorRequest mr ){
                    
                    if ( mr.getRequest().getBoolean( "gc" , false ) )
                        System.gc();

                    IdentitySet<AppContext> all = new IdentitySet<AppContext>();
                
                    synchronized ( _contextCreationLock ){
                        all.addAll( _contextCache.values() );
                    }
                    
                    SeenPath seen = new SeenPath();
		    
                    long totalSize = 0;

                    for ( AppContext ac : all ){
                        
                        if ( ac == null )
                            continue;
                        
                        mr.startData( ac.getName() + ":" + ac.getEnvironmentName() );
                        mr.addData( "Num Requests" , ac._numRequests );
                        mr.addData( "Created" , ac._created );
                        mr.addData( "git branch" , ac.getGitBranch() );
                        try {
                            int before = seen.size();
                            long size = ac.approxSize( seen );
                            totalSize += size;
                            mr.addData( "Memory (kb)" , size / 1024 );
                            mr.addData( "Number Objects" , seen.size() - before );
                            
                            if ( mr.getRequest().getBoolean( "reflect" , false ) ){
                                ReflectionVisitor.Reachable r = new AppContext.AppContextReachable();
                                ReflectionWalker walker = new ReflectionWalker( r );
                                walker.walk( ac );
                                mr.addData( "Reflection Object Count" , r.seenSize() );
                            }
                            
                        }
                        catch ( Exception e ){
                            e.printStackTrace();
                            mr.addData( "Memory" , "error getting size : " + e );
                        }
                        mr.endData();
                    }

                    
                    mr.startData( "TOTOAL" );
                    mr.addData( "Total Memory (kb)" , totalSize / 1024 );
                    mr.endData();
                    
                }
            } );
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

        if ( D ) System.out.println( "\t fixed host [" + host + "]" );

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
            ac = _getContextFromMap( siteRoot.getAbsolutePath() );
            if ( ac == null ){
                ac = new AppContext( siteRoot );
                _contextCache.put( siteRoot.getAbsolutePath() , ac );    
            }
        }
        else {

            if ( D ) System.out.println( "\t this is a holder for branches" );

            final String env = info.getEnvironment( originalHost );
            if ( D ) System.out.println( "\t\t env : " + env );

            final File envRoot = getBranch( siteRoot , env , info.host );
            if ( D ) System.out.println( "\t using full path : " + envRoot );
            
            final String fileKey = envRoot.getAbsolutePath();
            
            ac = _getContextFromMap( fileKey );
            if ( ac == null ){
                ac = new AppContext( envRoot.toString() , envRoot , siteRoot.getName() , env );
                _contextCache.put( fileKey , ac );
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
        GitDir git = _getBranch( root , subdomain , siteName );

        JSObject envConfig = getEnvironmentFromCloud( siteName , subdomain );
        if ( envConfig != null ){
            String branch = envConfig.get( "branch" ).toString() ;
            if ( D ) System.out.println( "\t using branch [" + branch + "]" );
            
            git.findAndSwitchTo( branch );
        }
        
        return git.getRoot();
    }

    GitDir _getBranch( File root , String subdomain , String siteName ){
        GitDir test = new GitDir( root , subdomain );
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
                    test.clone( giturl );
                    
                    String branch = envConfig.get( "branch" ).toString();
                    if ( ! test.checkout( branch ) )
                        throw new RuntimeException( "couldn't checkout [" + branch + "] for [" + test + "]" );
                         
                    return test;
                }
            }
        }

        if ( subdomain.equals( "dev" ) ){
            test = new GitDir( root , "master" );
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
                test = new GitDir( root , searchList[i] );
                if ( test.exists() )
                    return test;
            }
        }

        throw new RuntimeException( "can't find environment [" + subdomain + "] in [" + root + "]  siteName [" + siteName + "] found site:" + ( site != null )  );
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

    static JSObject getSiteFromCloud( String name ){
        Cloud theCloud = Cloud.getInstanceIfOnGrid();
        if ( theCloud == null )
            return null;

        return theCloud.findSite( name , false );
    }

    static List<Info> getPossibleSiteNames( String host , String uri ){
        return getPossibleSiteNames( fixBase( host , uri ) );
    }

    public static List<Info> getPossibleSiteNames( Info base ){
        
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

    public static Info fixBase( String host , String uri ){

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
                throw new HttpExceptions.BadRequest( 410 , "static host without valid  host:[" + host + "] uri:[" + uri + "]" );
            
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

    public static class Info {

        Info( String host ){
            this( host , "/" );
        }

        Info( String host , String uri ){
            this.host = host;
            this.uri = uri;
        }
        
        public String getHost(){
            return host;
        }

        public String getURI(){
            return uri;
        }

        public String getEnvironment( String big ){

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
            if ( this.host != null && this.host.equalsIgnoreCase( "www" ) && getRoot().contains( "stage" ) )
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
