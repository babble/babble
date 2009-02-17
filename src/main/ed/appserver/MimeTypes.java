// MimeTypes.java

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

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.util.*;

public class MimeTypes implements Sizable {

    public static String getExtension( File f ){
        return getExtension( f.toString() );
    }

    public static String getExtension( String s ){
        int idx = s.lastIndexOf( "." );
        if ( idx < 0 )
            return s;
        return s.substring( idx + 1 );
    }

    public static String get( File f ){
        return get( f.toString() );
    }
    
    public static String get( String ext ){
        ext = getExtension( ext );
        return _mimeTypes.getProperty( ext.toLowerCase() );
    }

    public static String getDispositionFromMimeType( String mimeType ){
        if ( mimeType != null && mimeType.startsWith( "image/" ) )
            return "inline";
        return "attachment";
    }

    public long approxSize( SeenPath seen ){
        return _mimeTypes.size() * 20;
    }

    static final Properties _mimeTypes;

    static {
        try {
            _mimeTypes = new Properties();
            _mimeTypes.load( ClassLoader.getSystemClassLoader().getResourceAsStream( "mimetypes.properties" ) );
        }
        catch ( Exception e ){
            throw new RuntimeException( e );
        }
    }


}
