// Bytes.java

package ed.db;

import java.nio.charset.*;

import ed.js.*;

public class Bytes {
    
    static byte NUMBER = 1;
    static byte STRING = 2;
    static byte ARRAY = 3;
    static byte OBJECT = 4;
    static byte OID = 5;
    
    static protected Charset _utf8 = Charset.forName( "UTF-8" );
    
}
