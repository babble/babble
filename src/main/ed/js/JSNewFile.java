// JSNewFile.java

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
