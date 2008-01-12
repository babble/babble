// JSNewFile.java

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

