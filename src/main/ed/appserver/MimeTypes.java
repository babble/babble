// MimeTypes.java

package ed.appserver;

import java.io.*;
import java.util.*;

public class MimeTypes {

    public static String get( File f ){
        return get( f.toString() );
    }
    
    public static String get( String ext ){
        int idx = ext.lastIndexOf( "." );
        if ( idx >= 0 )
            ext = ext.substring( idx + 1 );
        
        return _mimeTypes.getProperty( ext );
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
