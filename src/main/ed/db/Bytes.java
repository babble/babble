// Bytes.java

package ed.db;

import java.nio.charset.*;

import ed.js.*;

/**
 * <type><name>0<data>
 *   <NUMBER><name>0<double>
 *   <STRING><name>0<len><string>0
 
 */
public class Bytes {

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
    
    static protected Charset _utf8 = Charset.forName( "UTF-8" );
    
}
