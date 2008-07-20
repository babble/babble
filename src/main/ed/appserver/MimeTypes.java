// MimeTypes.java

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

public class MimeTypes {

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
