// UploadFile.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
                    throw new RuntimeException( "not implemented" );
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
