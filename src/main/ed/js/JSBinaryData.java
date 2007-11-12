// JSBinaryData.java

package ed.js;

import java.io.*;
import java.nio.*;

public abstract class JSBinaryData {

    public abstract int length();

    // just the raw data
    public abstract void put( ByteBuffer buf );

    public String toString(){
        return "JSBinaryData";
    }

    // -----------

    public static class ByteArray extends JSBinaryData{
        public ByteArray( byte[] data ){
            _data = data;
        }
        
        public int length(){
            return _data.length;
        }
        
        public void put( ByteBuffer buf ){
            buf.put( _data );
        }
        
        final byte[] _data;
    }
}
