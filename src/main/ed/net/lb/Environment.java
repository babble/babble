// Environment.java

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

package ed.net.lb;

import java.net.*;
import java.util.*;

import ed.net.*;
import ed.net.httpserver.*;

public class Environment {

    public Environment( String site , String env ){    
        this( site , env , null );
    }

    public Environment( String site , String env , String host ){
        this.site = site;
        this.env = env;
        this.host = host;
    }
    
    public int hashCode(){
        int h = 0;
        if ( site != null )
            h += site.hashCode();
        if ( env != null )
            h += env.hashCode();
        return h;
    }
    
    public boolean equals( Object o ){
        if ( ! ( o instanceof Environment ) )
            return false;
        
        Environment e = (Environment)o;
        if ( site == null && e.site != null )
            return false;
        if ( env == null && e.env != null )
            return false;
        
        return site.equals( e.site ) && env.equals( e.env );
    }
    
    public String replaceHeaderValue( String name , String value ){
        if ( name.equalsIgnoreCase( "host" ) && host != null ){
            int idx = value == null ? -1 : value.indexOf( ":" );
            if ( idx > 0 )
                return this.host + value.substring( idx );
            return this.host;
        }
        return value;
    }
    
    public String toString(){
        return "site: [" + site + "] env: [" + env + "]";
    }
    
    public final String site;
    public final String env;
    public final String host;
}
