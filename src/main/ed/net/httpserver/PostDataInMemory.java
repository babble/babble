// PostDataInMemory.java

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
import java.util.*;
import java.util.regex.*;

class PostDataInMemory extends PostData {

    static final int MAX = 1024 * 1024 * 110;

    PostDataInMemory( int cl , boolean multipart , String ct ){
        super( cl , multipart , ct );
        _data = new byte[cl];
        _pos = 0;
    }
    
    int position(){
        return _pos;
    }
    
    byte get( int pos ){
        if ( pos >= _pos )
            throw new RuntimeException( "pos >= _pos" );
        return _data[pos];
    }

    void put( byte b ){
        _data[_pos++] = b;
    }

    String string( int start , int len ){
        return new String( _data , start , len );
    }

    void fillIn( ByteBuffer buf , int start , int end ){
        buf.put( _data , start , end - start );
    }

    public void writeTo( File f )
        throws IOException {
        FileOutputStream fout = new FileOutputStream( f );
        fout.write( _data );
        fout.close();
    }

    public String toString(){
        return new String( _data );
    }

    int _pos = 0;
    final byte _data[];

}
