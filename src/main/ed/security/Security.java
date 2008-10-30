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
        "/data/corejs/" ,
        "/data/core-modules/admin/",
        "/data/sites/admin/",
        "/data/sites/www/",
        "/data/sites/grid/",
        "/data/sites/modules/",
        "lastline",
        "src/main/ed/",
        "/home/yellow/code_for_hudson/",
        new File( "src/test/ed/" ).getAbsolutePath(),
        "./src/test/ed/lang/python/", // FIXME?
        Config.get().getProperty("ED_HOME", "/data/ed") + "/src/test/ed"
    };

    public static boolean isCoreJS(){
        if ( OFF )
            return true;

        String topjs = getTopJS();
        if ( topjs == null ) {
            return false;
        }
        
        for ( int i=0; i<SECURE.length; i++ )
            if ( topjs.startsWith( SECURE[i] ) )
                return true;
        
        return false;
    }

    public static String getTopJS(){
        return getTopDynamicStackFrame().getFileName();
    }

    public static StackTraceElement getTopDynamicStackFrame(){
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        StackTraceHolder holder = StackTraceHolder.getInstance();

        for ( int i=0; i<st.length; i++ ){
            StackTraceElement e = st[i];
            StackTraceElement n = holder.fix( e );
            // if n == null, this was removed, which means this was internal.
            // if n is different, e was replaced, which means e was dynamic code
            // that someone knew how to handle.
            if ( n == null || n == e ) continue;

            return n;
        }

        return null;
    }

    public static StackTraceElement getTopUserStackElement(){
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        
        for ( int i=0; i<st.length; i++ ){
            StackTraceElement e = st[i];

            final String name = e.getClassName();

            if ( name.startsWith( Convert.DEFAULT_PACKAGE + "." ) )
                return e;
            
            if ( name.startsWith( "ed." ) || name.startsWith( "java." ) )
                continue;
            
            return e;
        }

        return null;
        
    }
}
