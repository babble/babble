// ByteBufferHolder.java

package ed.io;

import java.util.*;
import java.nio.*;
import java.nio.channels.*;

public class ByteBufferHolder {

    public ByteBufferHolder(){

    }
    
    public byte get( int i ){
        if ( i >= _pos )
            throw new RuntimeException( "out of bounds" );
        
        final int num = i / _bufSize;
        final int pos = i % _bufSize;

        return _buffers.get( num ).get( pos );
    }

    public void put( int i , byte val ){
        if ( i >= _pos )
            throw new RuntimeException( "out of bounds" );
        
        final int num = i / _bufSize;
        final int pos = i % _bufSize;

        _buffers.get( num ).put( pos , val );
    }
    
    public int position(){
        return _pos;
    }

    public void position( int p ){
        _pos = p;
        int num = _pos / _bufSize;
        int pos = _pos % _bufSize;
        ByteBuffer bb = _buffers.get( num );
        bb.position( pos );
        for ( int i=num+1; i<_buffers.size(); i++ )
            _buffers.get( i ).position( 0 );
    }

    public int remaining(){
        return Integer.MAX_VALUE;
    }

    public void put( ByteBuffer in ){
        while ( in.hasRemaining() ){
            int num = _pos / _bufSize;
            if ( num >= _buffers.size() )
                _addBucket();

            ByteBuffer bb = _buffers.get( num );
            
            final int canRead = Math.min( bb.remaining() , in.remaining() );
            
            final int oldLimit = in.limit();
            in.limit( canRead );
            
            bb.put( in );
            
            in.limit( oldLimit );
            
            _pos += canRead;
        }

    }

    private void _addBucket(){
        _buffers.add( ByteBuffer.allocateDirect( _bufSize ) );
    }
    
    public String toString(){
        StringBuilder buf = new StringBuilder();
        buf.append( "{ ByteBufferHolder pos:" + _pos + " " );
        for ( ByteBuffer bb : _buffers )
            buf.append( bb ).append( " " );
        return buf.append( "}" ).toString();
    }

    List<ByteBuffer> _buffers = new ArrayList<ByteBuffer>();
    int _pos = 0;

    final int _bufSize = 4096;
}
