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

public class Security {

    public final static boolean OFF = ed.util.Config.get().getBoolean( "NO-SECURITY" );
    public final static String _baseClass = Convert.cleanName( Module.getBase());

    public final static Set<String> allowedSites;
    static {
	Set<String> s = new HashSet<String>();
	s.add( "admin" );
	s.add( "www" );
	s.add( "grid" );
	allowedSites = Collections.synchronizedSet( s );
    }

    public final static boolean isAllowedSite( String siteName ){
	return allowedSites.contains( siteName );
    }

    final static String SECURE[] = new String[]{ 
        Convert.DEFAULT_PACKAGE + "." + _baseClass + "corejs_" , 
        Convert.DEFAULT_PACKAGE + "." + _baseClass + "core_modules_admin_" , 
        Convert.DEFAULT_PACKAGE + "." + _baseClass + "sites_admin_" , 
        Convert.DEFAULT_PACKAGE + "." + _baseClass + "sites_www_" , 
        Convert.DEFAULT_PACKAGE + "." + _baseClass + "sites_grid_" , 
        Convert.DEFAULT_PACKAGE + ".lastline" ,
        Convert.DEFAULT_PACKAGE + ".src_main_ed_" ,
        Convert.DEFAULT_PACKAGE + "._home_yellow_code_for_hudson" ,
        Convert.DEFAULT_PACKAGE + "." + new File( "src/test/ed" ).getAbsolutePath().replace( '/' , '_' )
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

        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        
        for ( int i=0; i<st.length; i++ ){
            StackTraceElement e = st[i];
            if ( e.getClassName().startsWith( Convert.DEFAULT_PACKAGE + "." ) )
                return e.getClassName();
        }

        return null;
    }
}
