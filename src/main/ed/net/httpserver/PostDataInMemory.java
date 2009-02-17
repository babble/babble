// PostDataInMemory.java

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

package ed.net.httpserver;

import java.io.*;
import java.nio.*;

import ed.js.*;

class PostDataInMemory extends PostData {

    static final int MAX = 1024 * 1024 * 110;

    PostDataInMemory( int contentLength , boolean multipart , String contentType ){
        super( contentLength , multipart , contentType );

        if (contentLength < 0 || contentLength > MAX) {
            throw new RuntimeException("Error : specified content length [" + contentLength + "] invalid. < 0 or > MAX (Max = " + MAX + "");
        }
        
        _data = new byte[contentLength];
        _pos = 0;
    }
    
    int position(){
        return _pos;
    }

    static int getMax() {
        return MAX;
    }
    
    byte get( int pos ){
        if ( pos >= _pos ) {
            throw new RuntimeException("Error: attempt to read past end of data. " + pos + " >= " + _pos);
        }
        return _data[pos];
    }

    void put( byte b ){
        if ( _pos == _data.length ) {
            throw new RuntimeException("Error: attempt to write past end of buffer.");
        }
        _data[_pos++] = b;
    }

    String string( int start , int len ){
        return new String( _data , start , len );
    }

    void fillIn( ByteBuffer buf , int start , int end ){
        buf.put( _data , start , end - start );
    }

    public void write( OutputStream out , int start , int end )
        throws IOException {
        out.write( _data , start , end - start );
    }

    public void writeTo( File f )
        throws IOException {
        FileOutputStream fout = new FileOutputStream( f );
        fout.write( _data );
        fout.close();
    }

    public JSFile getAsFile(){
        return new JSInputFile( "not named" , "none" , _data );
    }

    public String toString(){
        return new String( _data );
    }

    int _pos = 0;
    final byte _data[];

}
