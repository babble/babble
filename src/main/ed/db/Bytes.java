// Bytes.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.js.*;

/**
 * <type><name>0<data>
 *   <NUMBER><name>0<double>
 *   <STRING><name>0<len><string>0
 
 */
public class Bytes {

    static final boolean D = Boolean.getBoolean( "DEBUG.DB" );

    public static final ByteOrder ORDER = ByteOrder.LITTLE_ENDIAN;

    static final int BUF_SIZE = 1024 * 1024 * 5;
    
    static final int CONNECTIONS_PER_HOST = 20;
    static final int BUFS_PER_50M = ( 1024 * 1024 * 50 ) / BUF_SIZE;

    static final byte EOO = 0;    
    static final byte NUMBER = 1;
    static final byte STRING = 2;
    static final byte OBJECT = 3;    
    static final byte ARRAY = 4;
    static final byte BINARY = 5;
    static final byte UNDEFINED = 6;
    static final byte OID = 7;
    static final byte BOOLEAN = 8;
    static final byte DATE = 9;
    static final byte NULL = 10;
    static final byte REGEX = 11;
    static final byte REF = 12;
    static final byte CODE = 13;

    static final byte SYMBOL = 14;

    /* 
       these are binary types
       so the format would look like
       <BINARY><name><BINARY_TYPE><...>
    */

    static final byte B_FUNC = 1;
    static final byte B_BINARY = 2;

    
    static protected Charset _utf8 = Charset.forName( "UTF-8" );
    static protected final int MAX_STRING = 1024 * 512;
    
    public static byte getType( Object o ){
        if ( o == null )
            return NULL;

        if ( o instanceof DBRef )
            return REF;

        if ( o instanceof JSFunction )
            return CODE;

        if ( o instanceof Number )
            return NUMBER;
        
        if ( o instanceof String || o instanceof JSString )
            return STRING;
        
        if ( o instanceof JSArray )
            return ARRAY;

        if ( o instanceof JSBinaryData )
            return BINARY;

        if ( o instanceof ObjectId )
            return OID;
        
        if ( o instanceof Boolean )
            return BOOLEAN;
        
        if ( o instanceof JSDate )
            return DATE;

        if ( o instanceof JSRegex )
            return REGEX;
        
        if ( o instanceof JSObject )
            return OBJECT;

        return 0;
    }


    static final String NO_REF_HACK = "_____nodbref_____";
}
