// Environment.java

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
    
    public String getExtraHeaderString(){
        StringBuilder buf = new StringBuilder();
        getExtraHeaderString( buf );
        return buf.toString();
    }
    
    public void getExtraHeaderString( StringBuilder buf ){
        buf.append( "X-Host: " ).append( fullInternalHost() ).append( "\r\n" );
    }
    
    public String toString(){
        return "site: [" + site + "] env: [" + env + "] host: [" + this.host + "]";
    }
    
    String fullInternalHost(){
        return this.env + "." + this.site + _internalDomain;
    }

    public final String site;
    public final String env;
    public final String host;

    static String _internalDomain = "." + ed.util.Config.getExternalDomain();
}
