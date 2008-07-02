// MimeTypes.java

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
        if ( mimeType.startsWith( "image/" ) )
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
