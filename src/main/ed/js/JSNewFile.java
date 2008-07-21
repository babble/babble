// JSNewFile.java

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

package ed.js;

import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;

import ed.db.*;

public abstract class JSNewFile extends JSFile {

    protected JSNewFile( String filename , String contentType , long length ){
        super( filename , contentType , length );

        final int nc = numChunks();

        for ( int i=0; i<nc; i++ ){
            JSFileChunk c = newChunk( i );
            c.set( "_id" , ObjectId.get() );
            if ( i == 0 )
                set( "next" , c );

            _chunks.add( c );
            if ( i > 0 )
                _chunks.get( i - 1 ).set( "next" , c );

        }

	set( "uploadDate" , new JSDate() );

    }


    protected abstract JSFileChunk newChunk( int num );

    public ObjectId getChunkID( int num ){
        return (ObjectId)(_chunks.get( num ).get( "_id" ));
    }

    public JSFileChunk getChunk( int num ){
        return _chunks.get( num );
    }

    final List<JSFileChunk> _chunks = new ArrayList<JSFileChunk>();

}
