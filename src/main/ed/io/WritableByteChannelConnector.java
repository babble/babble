// WritableByteChannelConnector.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

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
