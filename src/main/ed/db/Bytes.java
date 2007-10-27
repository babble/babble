// Bytes.java

package ed.db;

import java.nio.charset.*;

import ed.js.*;

public class Bytes {
    
    static final byte NUMBER = 1;
    static final byte STRING = 2;
    static final byte ARRAY = 3;
    static final byte OBJECT = 4;
    static final byte OID = 5;
    static final byte EOO = 6;

    static protected Charset _utf8 = Charset.forName( "UTF-8" );
    
}
