// SysExecCGIGateway.java

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
