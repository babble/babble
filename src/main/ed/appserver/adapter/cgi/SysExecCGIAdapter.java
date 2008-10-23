// SysExecCGIGateway.java

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

package ed.appserver.adapter.cgi;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.security.*;
import ed.net.httpserver.*;
import ed.appserver.*;
import ed.util.*;

public class SysExecCGIAdapter extends CGIAdapter {
    
    public SysExecCGIAdapter( File f ){
        _file = f;
    }
    
    public void handleCGI( EnvMap env , InputStream stdin , OutputStream stdout , AppRequest ar ){
        
        if ( ! Security.isAllowedSite( ar.getContext().getName() ) ){
            HttpResponse response = ar.getResponse();
            response.setResponseCode( 501 );
            response.getJxpWriter().print( "you are not allowed to run cgi programs" );
            return;
        }

        String envarr[] = new String[env.size()];
        int pos=0;
        for ( String key : env.keySet() )
            envarr[pos++] = key + "=" + env.get( key );

        try {
            Process p = Runtime.getRuntime().exec( new String[]{ _file.getAbsolutePath() } , envarr , _file.getParentFile() );
            StreamUtil.pipe( p.getInputStream() , stdout );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "can't read from cgi pipe for [" + _file + "]" , ioe );
        }
    }

    public File getFile(){
        return _file;
    }

    public String getName(){
        return _file.toString();
    }
    
    public long lastUpdated( Set<Dependency> s ){
        return _file.lastModified();
    }

    final File _file;
    
}
