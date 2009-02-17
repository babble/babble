// Encoding.java

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

import java.net.*;

import bak.pcj.map.*;
import bak.pcj.set.*;

import ed.js.func.*;
import ed.js.engine.*;

/** Functionality to encode and escape text.
 * @expose
 */
public class Encoding {

    /** Function to replace nonalphanumeric characters with their %hex equivalents.  Does not replace '@', '*', '-', '_', '+', '/', or '.'. This is an implementation of the JavaScript builtin method.  */
    public static JSFunction escape = new JSFunctionCalls1(){
            public Object call( Scope s , Object o , Object [] extra ){
                return new JSString( _escape( o.toString() , _noEscaping ) );
            }
        };

    /** Function to replace nonalphanumeric characters with their %hex equivalents.  Does not replace '~', '!', '@', '#', '$', '&', '*', '(', ')', '-', '_', '+', '=', ';', ':', '/', ''', '?', ',', or '.'.  This is an implementation of the JavaScript builtin method. */
    public static JSFunction encodeURI = new JSFunctionCalls1(){
            public Object call( Scope s , Object o , Object [] extra ){
                return new JSString( _escape( o.toString() , _noEncoding ) );
            }
        };

    /** Function to replace nonalphanumeric characters with their %hex equivalents.  Does not replace '~', '!', '*', '(', ')', '-', '_', ''', or '.'.  This is an implementation of the JavaScript builtin method. */
    public static JSFunction encodeURIComponent = new JSFunctionCalls1(){
            public Object call( Scope s , Object o , Object [] extra ){
                return new JSString( _escape( o.toString() , _noEncodingComonent ) );
            }
        };

    /** @unexpose */
    static final String _escape( final String str , final CharSet skip ){
        final StringBuilder buf = new StringBuilder( (int)(str.length() * 1.5) );
        final int len = str.length();

        for ( int i=0; i<len; i++ ){
            char c = str.charAt( i );
            int val = (int)c;
            // a lot of strange unicode characters count as letters, so 
            // Character.isLetterOrDigit doesn't work
            if ( ( c + "" ).matches( "\\w" ) ||
                 skip.contains( c ) )  {
                buf.append( c );
            }
            else if ( val >= 256 ) {
                buf.append( "%u" );
                toHex( c , 4 ,buf );
            }
            else {
                buf.append( "%" );
                toHex( c , 2 , buf );
            }
        }

        return buf.toString();
    }

    /** @unexpose */
    final static char _forDigit( int val ){
        char c = Character.forDigit( val , 16 );
        if ( c >= 'a' && c <= 'z' )
            c += _upperDiff;
        return c;
    }

    /** Function to unescape an escaped string. */
    public static JSFunction unescape = new JSFunctionCalls1(){
            public Object call( Scope s , Object o , Object [] extra ){
                return new JSString( _unescape( o.toString() ) );
            }
        };

    /** @unexpose */
    public static final String _unescape( final String str ){
        final StringBuilder buf = new StringBuilder( str.length() );

        final int len = str.length();

        int i=0;
        while ( i<len ){
            char c = str.charAt( i );

            if ( c == '%' ){ 
                // unicode
                if( i <= len - 6 && str.charAt( i+1 ) == 'u' ) {
                    final String toDecode = str.substring( i + 2 , i + 6 );
                    if ( _isHex( toDecode ) ){
                        buf.append( Character.toChars( Integer.parseInt( toDecode, 16 ) )[0] );
                        i += 6;
                        continue;
                    }
                }

                // low unicode ( %uxx == \\u00xx )
                if( i <= len - 3 ) {
                    final String toDecode = str.substring( i + 1 , i + 3 );
                    if( _isHex( toDecode ) ) {
                        buf.append( Character.toChars( Integer.parseInt( toDecode, 16 ) )[0] );
                        i += 3;
                        continue;
                    }
                }
            }

            buf.append( c );
            i++;
            continue;            
        }

        return buf.toString();
    }

    /** @unexpose */
    static final boolean _isHex( final String s ){
        final int len = s.length();
        for ( int i=0; i<len; i++ )
            if ( ! _isHex( s.charAt( i ) ) )
                return false;
        return true;
    }

    /** @unexpose */
    static final boolean _isHex( final char c ){
        if ( c >= '0' && c <= '9' )
            return true;

        if ( c >= 'a' && c <= 'f' )
            return true;

        if ( c >= 'A' && c <= 'F' )
            return true;

        return false;
    }

    static void toHex( char c, int size, StringBuilder buf ) {
        int s = (int)c;
        size--;
        while( size >= 0 ) {
            int num = s / (int)Math.pow( 16, size );
            switch( num ) {
            case 15:
                buf.append( 'F' );
                break;
            case 14:
                buf.append( 'E' );
                break;
            case 13:
                buf.append( 'D' );
                break;
            case 12:
                buf.append( 'C' );
                break;
            case 11:
                buf.append( 'B' );
                break;
            case 10:
                buf.append( 'A' );
                break;
            default:
                buf.append( num );
            }
            s = s % (int)Math.pow( 16, size );
            size--;
        }
    }

    /** @unexpose */
    static final int _upperDiff = 'A' - 'a';

    /** @unexpose */
    static final CharSet _noEscaping = new CharOpenHashSet();
    /** @unexpose */
    static final CharSet _noEncoding = new CharOpenHashSet();
    /** @unexpose */
    static final CharSet _noEncodingComonent = new CharOpenHashSet();

    static {
        _noEscaping.add( '@' );
        _noEscaping.add( '*' );
        _noEscaping.add( '-' );
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
        _noEncodingComonent.add( '\'' );
        _noEncodingComonent.add( '.' );
    }

    /** Converts special HTML characters into their entity equivalents: &lt;, &gt;, &amp;, &quot; &#39;. */
    public static JSFunction escapeHTML = new JSFunctionCalls1(){
            public Object call( Scope s , Object o , Object [] extra ){
                if ( o == null )
                    return null;
                return new JSString( _escapeHTML( o.toString() ) );
            }
        };

    /** @unexpose */
    public static String _escapeHTML( String html ){
        if ( html == null )
            return null;
        final StringBuilder buf = new StringBuilder( (int)(html.length() * 1.2) );
        final int max = html.length();
        for ( int i=0; i<max; i++ ){
            char c = html.charAt( i );

            switch( c ){
            case '<': buf.append( "&lt;" ); break;
            case '>': buf.append( "&gt;" ); break;
            case '&': buf.append( "&amp;" ); break;
            case '"': buf.append( "&quot;" ); break;
            case '\'': buf.append( "&#39;" ); break;
            default: buf.append( c ); break;
            }
        }
        return buf.toString();
    }


    /** Loads the functions from this class into the given scope.
     * @param s Scope to use
     */
    public static void install( Scope s ){
        s.set( "escape" , escape );
        s.set( "encodeURI"  , encodeURI );
        s.set( "encodeURIComponent" , encodeURIComponent );

        s.set( "unescape" , unescape );
        s.set( "decodeURI" , unescape );
        s.set( "decodeURIComponent" , unescape );


        s.set( "escapeHTML" , escapeHTML );
    }

}
