// PostData.java

package ed.net.httpserver;

import java.io.*;
import java.nio.*;

public abstract class PostData {

    static PostData create( HttpRequest req ){
        final int cl = req.getIntHeader( "Content-Length" , 0 );
        final String ct = req.getHeader( "Content-Type" );
        
        if ( cl <= 0 )
            return null;
        
        boolean multipart = ct != null && ct.toLowerCase().trim().startsWith( "multipart/form-data" );
        
        if ( ! multipart ){
            if ( cl > InMemory.MAX )
                throw new RuntimeException( "can't have regular post greater than 10 mb" );
            return new InMemory( cl , multipart );
        }
        
        if ( cl > InMemory.MAX )
            throw new RuntimeException( "can't handle huge stuff yet" );
        
        return new InMemory( cl , multipart );
    }

    PostData( int len , boolean multipart ){
        _len = len;
        _multipart = multipart;
    }

    abstract int position();
    abstract byte get( int pos );
    abstract void put( byte b );

    abstract String string( int start , int end );


    boolean done(){
        final int pos = position();
        if ( pos > _len )
            throw new RuntimeException( "something is wrong" );
        return pos == _len;
    }

    void go( HttpRequest req ){
        if ( ! _multipart ){
            addRegularParams( req );
            return;
        }

        throw new RuntimeException( "can't do multi part yet" );
    }

    private void addRegularParams( HttpRequest req ){
        for ( int i=0; i<_len; i++ ){
            int start = i;
            for ( ; i<_len; i++ )
                if ( get(i) == '=' ||
                     get(i) == '\n' ||
                     get(i) == '&' )
                    break;

            if ( i == _len ){
                req._addParm( string( start , _len - start ) , null );
                break;
            }
            
            if ( get(i) == '\n' ||
                 get(i) == '&' ){
                req._addParm( string( start , i - start ) , null );
                continue;
            }

            int eq = i;
            
            for ( ; i<_len; i++ )
                if ( get(i) == '\n' ||
                     get(i) == '&' )
                    break;
            
            req._addParm( string( start , eq - start ) ,
                          string( eq + 1 , i - ( eq + 1 ) ) );
                
        }
    }

    final int _len;
    final boolean _multipart;

    static class InMemory extends PostData {
        InMemory( int cl , boolean multipart ){
            super( cl , multipart );
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

        String string( int start , int end ){
            if ( end > _pos )
                throw new RuntimeException( "end > _pos " + end + " > " + _pos );
            return new String( _data , start , end );
        }

        public String toString(){
            return new String( _data );
        }

        int _pos = 0;
        final byte _data[];

        static final int MAX = 1024 * 1024 * 10;
    }
}
