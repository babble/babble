// WritableByteChannelConnector.java

package ed.io;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class WritableByteChannelConnector implements WritableByteChannel {
    public WritableByteChannelConnector( OutputStream out ){
        _out = out;
    }

    public int write( ByteBuffer src )
        throws IOException {
        byte b[] = new byte[src.remaining()];
        src.get( b );
        _out.write( b );
        return b.length;
    }

    public void close(){
    }
    
    public boolean isOpen(){
        return true;
    }

    
    private final OutputStream _out;

}
