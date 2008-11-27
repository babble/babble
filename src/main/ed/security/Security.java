// Security.java

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

package ed.security;

import java.util.*;
import java.io.*;

import ed.appserver.*;
import ed.js.engine.*;
import ed.util.Config;
import ed.lang.StackTraceHolder;

public class Security {

    public final static boolean OFF = ed.util.Config.get().getBoolean( "NO-SECURITY" );
    public final static String _baseClass = Convert.cleanName( Module.getBase());

    public final static Set<String> allowedSites;
    static {
	Set<String> s = new HashSet<String>();
	s.add( "admin" );
	s.add( "www" );
	s.add( "grid" );
	s.add( "mongo" );
	allowedSites = Collections.unmodifiableSet( s );
    }

    public final static boolean isAllowedSite( String siteName ){
	return allowedSites.contains( siteName );
    }

    final static String SECURE[] = new String[]{
        Config.getDataRoot() + "corejs/" ,
        Config.getDataRoot() + "core-modules/admin/",
        Config.getDataRoot() + "core-modules/py-google/",
        Config.getDataRoot() + "core-modules/cloudsignup/",
        Config.getDataRoot() + "sites/admin/",
        Config.getDataRoot() + "sites/www/",
        Config.getDataRoot() + "sites/grid/",
        Config.getDataRoot() + "sites/modules/",
        "lastline",
        "src/main/ed/",
        "src/test/ed/",
        "/home/yellow/code_for_hudson/",
        new File( "src/test/ed/" ).getAbsolutePath(),
        new File( "include/jython/Lib" ).getAbsolutePath(),
        "./src/test/ed/lang/python/", // FIXME?
        Config.get().getProperty("ED_HOME", "/data/ed") + "/src/test/ed",
        Config.get().getProperty("ED_HOME", "/data/ed") + "/include/jython/Lib",
        "./appserver/libraries/corejs/core" // TODO - fix this - hack to deal with SDK test failures
    };

    public static boolean inTrustedCode(){
        if ( OFF )
            return true;

        String topjs = getTopDynamicClassName();
        if ( topjs == null )
            return true;
        
        for ( int i=0; i<SECURE.length; i++ )
            if ( topjs.startsWith( SECURE[i] ) )
                return true;
        
        return false;
    }

    public static String getTopDynamicClassName(){
        StackTraceElement e = getTopDynamicStackFrame();
        if ( e == null )
            return null;
        return e.getClassName();
    }

    public static StackTraceElement getTopDynamicStackFrame(){
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        StackTraceHolder holder = StackTraceHolder.getInstance();

        for ( int i=0; i<st.length; i++ ){
            
            final StackTraceElement cur = st[i];
            final String name = cur.getClassName();
            
            if ( _standardJavaClass( name ) )
                continue;
            
            {
                // if fixed == null, this was removed, which means this was internal.
                // if fixed is different, e was replaced, which means e was dynamic code
                // that someone knew how to handle.
                
                final StackTraceElement fixed = holder.fix( cur );
                if ( fixed != null && fixed != cur )
                    return fixed;
            }

            if ( _dynamicClasses.contains( name ) )
                return cur;

            if ( name.startsWith( Convert.DEFAULT_PACKAGE + "." ) )
                return cur;
            
            if ( name.startsWith( "ed." ) )
                continue;
                    
        }

        return null;
    }

    public static boolean canAccessClass( final String c ){
        if ( inTrustedCode() )
            return true;
        
        return nonSecureCanAccessClass( c );
    }

    public static boolean nonSecureCanAccess( Class c ){
        return nonSecureCanAccessClass( c.getName() );
    }

    public static boolean nonSecureCanAccessClass( Class c ){
        return nonSecureCanAccessClass( c.getName() );
    }
    
    public static boolean nonSecureCanAccessClass( final String c ){
        if ( _standardJavaClass( c ) )
            return true;

        if ( c.startsWith( "ed.js." ) && c.indexOf( "." , 7 ) < 0 )
            return true;
        
        return false;
    }
    
    private static boolean _standardJavaClass( final String c ){
        return 
            c.startsWith( "com.sun." ) ||
            c.startsWith( "sun." ) ||
            c.startsWith( "javax." ) ||
            c.startsWith( "java." );
    }

    /**
     * need to be very careful with this
     */
    static void addDynamicClass( String c ){
        _dynamicClasses.add( c );
    }
    static Set<String> _dynamicClasses = Collections.synchronizedSet( new HashSet<String>() );
}
