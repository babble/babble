// PostData.java

package ed.net.httpserver;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.regex.*;

import ed.js.*;
import ed.util.*;

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
            return new InMemory( cl , multipart , ct );
        }
        
        if ( cl > InMemory.MAX )
            throw new RuntimeException( "can't handle huge stuff yet" );
        
        return new InMemory( cl , multipart , ct );
    }

    static byte[] _parseBoundary( String ct ){
        if ( ct == null )
            return null;

        final String thing = "boundary=";

        int start = ct.indexOf( thing );
        if ( start <= 0 )
            return null;
        
        int end = ct.indexOf( ";" , start );
        if ( end < 0 )
            end = ct.length();
        
        return ( "--" + ct.substring( start + thing.length() , end ) ).getBytes();
    }

    PostData( int len , boolean multipart , String contentType ){
        _len = len;
        _multipart = multipart;
        _contentType = contentType;
        _boundary = _parseBoundary( _contentType );
    }

    abstract int position();
    abstract byte get( int pos );
    abstract void put( byte b );
    
    abstract String string( int start , int len );
    abstract void fillIn( ByteBuffer buf , int start , int end );

    public void writeTo( JSLocalFile f )
        throws IOException {
        writeTo( f.getRealFile() );
    }

    public abstract void writeTo( File f )
        throws IOException ;

    int indexOf( byte b[] , int start ){
        
        outer:
        for ( int i=start; i<_len-b.length; i++ ){
            for ( int j=0; j<b.length; j++ )
                if ( b[j] != get( i + j ) )
                    continue outer;

            return i;
        }
        return -1;
    }
    
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
        
        parseMultiPart( req );
    }

    private void parseMultiPart( HttpRequest req ){
        
        int start = indexOf( _boundary , 0 );
        if ( start < 0 )
            return;
        
        start += _boundary.length + 1;

        while ( start < _len ){
            
            if ( get( start ) == '\n' )
                start++;
            
            int end = indexOf( _boundary , start );
            if ( end < 0 ){
                if ( get( start ) == '-' )
                    break;
                end = position();
            }

            
            Map<String,String> headers = new StringMap<String>();
            Map<String,String> mainPieces = new StringMap<String>();

            String type = null;
            while ( true ){
                int eol = start;
                for ( ; eol < _len; eol++ )
                    if ( get( eol ) == '\n' )
                        break;

                String line = string( start , eol - start ).trim();
                start = eol + 1;
                if ( line.length() == 0 )
                    break;
                
                int col = line.indexOf( ":" );
                if ( col < 0 )
                    continue;
                
                final String name = line.substring( 0 , col ).trim();
                final String value = line.substring( col + 1 ).trim();

                if ( name.equalsIgnoreCase( "Content-Disposition" ) ){
                    Matcher temp = Pattern.compile( "; (\\w+)=\"(.*?)\"" ).matcher( value );
                    while ( temp.find() )
                        mainPieces.put( temp.group(1) , temp.group(2) );
                }
                
                if ( name.equalsIgnoreCase( "content-type" ) )
                    type = value;
                
                headers.put( name , value );
            }
            
            if ( mainPieces.get( "filename" ) != null && type != null  ){
                String fn = mainPieces.get( "filename" ).trim();
                UploadFile uf = new UploadFile( fn , type , this , start , end - 1 );
                _files.put( mainPieces.get( "name" ) , uf );
            }
            else if ( type == null ){
                req._addParm( mainPieces.get( "name" ) , string( start , end - start ).trim() );
            }
            else {
                throw new RuntimeException( "can't handle : " + type + " " + headers );
            }
            
            start = end + _boundary.length + 1;
        }
        
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

    String status(){
        return "{" + position() + "/" + _len + " mp:" + _multipart + "}";
    }

    final int _len;
    final boolean _multipart;
    final String _contentType;
    final byte[] _boundary;
    final Map<String,UploadFile> _files = new TreeMap<String,UploadFile>();
    
    static class InMemory extends PostData {
        InMemory( int cl , boolean multipart , String ct ){
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
            while ( start < end )
                buf.put( _data[start++] );
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

        static final int MAX = 1024 * 1024 * 10;
    }
}
