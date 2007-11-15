// JSBinaryData.java

package ed.js;

import java.io.*;
import java.nio.*;

public abstract class JSBinaryData {

    public abstract int length();

    public abstract void put( ByteBuffer buf );
    public abstract void write( OutputStream out ) throws IOException;
    // this should be newly created
    public abstract ByteBuffer asByteBuffer();


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

        public void write( OutputStream out )
            throws IOException {
            out.write( _data );
        }

        public ByteBuffer asByteBuffer(){
            return ByteBuffer.wrap( _data );
        }
        
        final byte[] _data;
    }
}
