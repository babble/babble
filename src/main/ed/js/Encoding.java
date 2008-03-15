// Encoding.java

package ed.js;

import bak.pcj.set.*;

import ed.js.func.*;
import ed.js.engine.*;

public class Encoding {

    public static JSFunction escape = new JSFunctionCalls1(){
            public Object call( Scope s , Object o , Object [] extra ){
                return _escape( o.toString() , _noEscaping );
            }
        };

    public static JSFunction encodeURI = new JSFunctionCalls1(){
            public Object call( Scope s , Object o , Object [] extra ){
                return _escape( o.toString() , _noEncoding );
            }
        };

    public static JSFunction encodeURIComponent = new JSFunctionCalls1(){
            public Object call( Scope s , Object o , Object [] extra ){
                return _escape( o.toString() , _noEncodingComonent );
            }
        };
    
    static final String _escape( final String str , final CharSet skip ){
        final StringBuilder buf = new StringBuilder( (int)(str.length() * 1.5) );
        final int len = str.length();
        
        for ( int i=0; i<len; i++ ){
            char c = str.charAt( i );
            int val = (int)c;
            
            if ( Character.isLetterOrDigit( c ) )
                buf.append( c );
            else if ( c == ' ' )
                buf.append( "%20" );
            else if ( skip.contains( c ) )
                buf.append( c );
            else if ( val < 255 )
                buf.append( "%" )
                    .append( _forDigit( ( val >> 4 ) & 0xF ) )
                    .append( _forDigit( val & 0xF ) );
        }
        
        return buf.toString();
    }
    
    final static char _forDigit( int val ){
        char c = Character.forDigit( val , 16 );
        if ( c >= 'a' && c <= 'z' )
            c += _upperDiff;
        return c;
    }

    public static JSFunction unescape = new JSFunctionCalls1(){
            public Object call( Scope s , Object o , Object [] extra ){
                return _unescape( o.toString() );
            }
        };

    static final String _unescape( final String str ){
        final StringBuilder buf = new StringBuilder( str.length() );

        final int len = str.length();
        
        for ( int i=0; i<len; i++ ){
            char c = str.charAt( i );
            
            if ( c != '%' )
                buf.append( c );
            else {
                final String foo = str.substring( i + 1 , i + 3 );
                final int val = Integer.parseInt( foo , 16 );
                buf.append( (char)val );
                i += 2;
            }
            
        }

        return buf.toString();
    }

    static final int _upperDiff = 'A' - 'a';

    static final CharSet _noEscaping = new CharOpenHashSet();
    static final CharSet _noEncoding = new CharOpenHashSet();
    static final CharSet _noEncodingComonent = new CharOpenHashSet();

    static {
        _noEscaping.add( '@' );
        _noEscaping.add( '*' );
        _noEscaping.add( '-' );
        _noEscaping.add( '_' );
        _noEscaping.add( '+' );
        _noEscaping.add( '/' );
        _noEscaping.add( '.' );

        _noEncoding.add( '~' );
        _noEncoding.add( '!' );
        _noEncoding.add( '@' );
        _noEncoding.add( '#' );
        _noEncoding.add( '$' );
        _noEncoding.add( '&' );
        _noEncoding.add( '*' );
        _noEncoding.add( '(' );
        _noEncoding.add( ')' );
        _noEncoding.add( '-' );
        _noEncoding.add( '_' );
        _noEncoding.add( '+' );
        _noEncoding.add( '=' );
        _noEncoding.add( ';' );
        _noEncoding.add( ':' );
        _noEncoding.add( '\'' );
        _noEncoding.add( '/' );
        _noEncoding.add( '?' );
        _noEncoding.add( '.' );
        _noEncoding.add( ',' );


        _noEncodingComonent.add( '~' );
        _noEncodingComonent.add( '!' );
        _noEncodingComonent.add( '*' );
        _noEncodingComonent.add( '(' );
        _noEncodingComonent.add( ')' );
        _noEncodingComonent.add( '-' );
        _noEncodingComonent.add( '_' );
        _noEncodingComonent.add( '\'' );
        _noEncodingComonent.add( '.' );
    }

    public static void install( Scope s ){
        s.set( "escape" , escape );
        s.set( "encodeURI"  , encodeURI );
        s.set( "encodeURIComponent" , encodeURIComponent );
        
        s.set( "unescape" , unescape );
        s.set( "decodeURI" , unescape );
        s.set( "decodeURIComponent" , unescape );
    }
    
}
