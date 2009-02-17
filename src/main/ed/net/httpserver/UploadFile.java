// UploadFile.java

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

public class UploadFile extends JSNewFile  {

    UploadFile( String filename , String contentType , PostData pd , int start , int end ){
        super( filename , contentType , end - start );
        _data = pd;
        _start = start;
        _end = end;
    }

    public String string(){
        return _data.string( _start , _end - _start );
    }

    protected JSFileChunk newChunk( int i ){
        return new MyChunk( i );
    }

    class MyChunk extends JSFileChunk {
        MyChunk( int num ){
            super( UploadFile.this , num );
            _num = num;
        }

        public JSBinaryData getData(){

            final int start = _start + ( _num * getChunkSize() );
            final int end = Math.min( _end , start + getChunkSize() );

            if ( start > _end )
                throw new RuntimeException( "this chunk shouldn't exists" );
            
            return new JSBinaryData(){

                public int length(){
                    return end - start;
                }
                
                public void put( ByteBuffer buf ){
                    _data.fillIn( buf , start , end );
                }
                
                public void write( OutputStream out ) 
                    throws IOException {
                    _data.write( out , start , end );
                }
                
                public ByteBuffer asByteBuffer(){
                    throw new RuntimeException( "not implemented" );
                }
                
            };
        }

        final int _num;
    }


    final PostData _data;
    final int _start;
    final int _end;
        
}
