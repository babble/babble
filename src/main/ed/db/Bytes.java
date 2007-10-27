// Bytes.java

package ed.db;

import java.nio.charset.*;

import ed.js.*;

public class Bytes {
    
    static final byte NUMBER = 1;   // X
    static final byte STRING = 2;   // X
    static final byte ARRAY = 3;    // X
    static final byte OBJECT = 4;  // X
    static final byte OID = 5;     // X
    static final byte EOO = 6;      // X
    static final byte BOOLEAN = 7;   
    static final byte DATE = 8;    // X
    static final byte NULL = 9;    // X

    static protected Charset _utf8 = Charset.forName( "UTF-8" );
    
}
