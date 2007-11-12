// JSBinaryData.java

package ed.js;

import java.io.*;
import java.nio.*;

public abstract class JSBinaryData {

    public abstract int length();

    // just the raw data
    public abstract void put( ByteBuffer buf )
        throws IOException ;

    public String toString(){
        return "JSBinaryData";
    }
}
