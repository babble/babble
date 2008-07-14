// JSBinaryData.java

package ed.js;

import java.io.*;
import java.nio.*;

/** @expose */
public abstract class JSBinaryData {

    /** Length of this binary data. */
    public abstract int length();

    public abstract void put( ByteBuffer buf );

    /** Write this binary data to a given output stream.
     * @param out Output stream to which to write
     */
    public abstract void write( OutputStream out ) throws IOException;
    // this should be newly created
    /** Get this data as as a byte buffer
     * @return The byte buffer with this data in it.
     */
   public abstract ByteBuffer asByteBuffer();

    /** Returns "JSBinaryData".
     * @return Binary data description
     */
    public String toString(){
        return "JSBinaryData";
    }

    // -----------

    public static class ByteArray extends JSBinaryData{
        public ByteArray( byte[] data ){
            this( data , 0 , data.length );
        }

        public ByteArray( byte[] data , int offset , int len ){
            _data = data;
            _offset = offset;
            _len = len;
        }

        public int length(){
            return _len;
        }

        public void put( ByteBuffer buf ){
            buf.put( _data , _offset , _len );
        }

        public void write( OutputStream out )
            throws IOException {
            out.write( _data , _offset , _len );
        }

        public ByteBuffer asByteBuffer(){
            return ByteBuffer.wrap( _data , _offset , _len );
        }

        final byte[] _data;
        final int _offset;
        final int _len;
    }
}
