// JSRegex.java

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

import java.util.regex.*;

import ed.util.*;
import ed.js.func.*;
import ed.js.engine.*;

/** @expose */
public class JSRegex extends JSObjectBase {

    /** @unexpose */
    private final static JSFunction _cons = new Cons();
    public static class Cons extends JSFunctionCalls1{

        public JSObject newOne(){
            return new JSRegex();
        }
        
        public Object call( Scope s, Object[] args ) {
            return new JSRegex( "", "" );
        }

        public Object call( Scope s , Object a , Object[] args ){
            String p = a.toString();
            String f = "";
            if( args != null && args.length > 0 )
                f = args[0].toString();

            Object o = s.getThis();
            if ( o == null || ! ( o instanceof JSRegex ) )
                return new JSRegex( p , f );
            
            JSRegex r = (JSRegex)o;
            r.init( p , f );
            return r;
        }

        public Object get( Object o ) {
            if( o == null )
                return null;
            
            String s = o.toString();
            if( s.startsWith( "$" ) ) {
                int m = Integer.parseInt( s.substring( 1 ) );
                Object obj = matchArray.get( m );
                return obj == null ? "" : obj.toString();
            }
            else if( o.equals( "input" ) ) {
                return JSRegex.input;
            }

            return super.get( o );
        }

        public Object set( Object n, Object v ) {
            if( n == null ) 
                return false;
            if( v == null )
                v = "";

            if( n.toString().equals( "input" ) ) {
                JSRegex.input = v.toString();
                return v;
            }
            return super.set( n,v );
        }

        protected void init(){
            final JSObject proto = _prototype;

            _prototype.set( "lastIndex" , 0 );

            _prototype.set( "toString" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        Object o = s.getThis();
                        if( !(o instanceof JSRegex ) ){
                            return null;
                        }
                        String source = proto.get( "source" ).toString();
                        // default source
                        if( source.equals( "" ) )
                            source = "(?:)";
                        String g = Boolean.valueOf( proto.get( "global" ).toString() ) ? "g" : "";
                        String i = Boolean.valueOf( proto.get( "ignoreCase" ).toString() ) ? "i" : "";
                        String m = Boolean.valueOf( proto.get( "multiline" ).toString() ) ? "m" : "";
                        return new JSString( "/" + source + "/" + g + i + m );
                    }
                } );
            _prototype.set( "test" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object o , Object foo[] ){
                        if ( o == null )
                            return ((JSRegex)s.getThis()).test( (String)o );
                        return ((JSRegex)s.getThis()).test( o.toString() );
                    }
                } );

            _prototype.set( "exec" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object o , Object foo[] ){
                        if( o == null ) 
                            return ((JSRegex)s.getThis()).exec( (String)o );
                        return ((JSRegex)s.getThis()).exec( o.toString() );
                    }
                } );

            _prototype.set( "__rmatch" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object o , Object foo[] ){
                        
                        if ( o == null )
                            return -1;
                        
                        String str = o.toString();
                        
                        JSRegex r = (JSRegex)s.getThis();
                        JSArray a = r.exec( str );
                        r._last.set( a );
                        if ( a == null )
                            return null;
                        return a.get( "index" );
                    }
                } );

                _prototype.set( "match" , new JSFunctionCalls1(){
                        public Object call( Scope s , Object o , Object foo[] ){

                            if ( o == null )
                                return -1;

                            String str = o.toString();

                            JSRegex r = (JSRegex)s.getThis();
                            JSArray a = r.exec( str , false );
                            r._last.set( a );
                            if ( a == null )
                                return null;

                            final String res = a.getInt(0).toString();
                            return res;
                        }
                    } );

                set( "quote" , new JSFunctionCalls1(){
                        public Object call( Scope s , Object o , Object foo[] ){
                            return quote( o.toString() );
                        }
                    }
                    );


                set( "last" , new JSFunctionCalls0(){
                        public Object call( Scope s , Object foo[] ){
                            return _lastRegex.get();
                        }
                    } );
                
                _prototype.dontEnumExisting();
            }
        };

    /** Create a new regular expression. */
    public JSRegex(){
        super( Scope.getThreadLocalFunction( "RegExp" , _cons ) );
    }

    /** Create a new regular expression from the given string.
     * @param p Regular expression
     */
    public JSRegex( String p ){
        this( p , "" );
    }

    /** Create a new regular expression from the given string with options.  Valid option strings can be any combination of "i", for "case insensitive", "g", for "global", and "m" for "multiline input".
     * Using the "m" option causes ^ and $ to match the beginnning and end of a line, respectively, versus the beginning and end of the input they would match normally.
     * @param p Regular expression
     * @param f Options
     */
    public JSRegex( String p , String f ){
        super( Scope.getThreadLocalFunction( "RegExp" , _cons ) );
        init( p , f );

        getConstructor()._prototype.set( "source" , p );
        getConstructor()._prototype.set( "global" , _f.indexOf( "g" ) >= 0 );
        getConstructor()._prototype.set( "ignoreCase" , _f.indexOf( "i" ) >= 0 );
        getConstructor()._prototype.set( "multiline" , _f.indexOf( "m" ) >= 0 );
        getConstructor()._prototype.dontEnumExisting();
    }

    private static final boolean isHex( char c ) {
        return (c >= '0' && c <= '9') || 
            (c >= 'a' && c <= 'f') || 
            (c >= 'A' && c <= 'F');
    }

    /** @unexpose */
    static String _jsToJava( String p ){
        StringBuilder buf = new StringBuilder( p.length() + 10 );

        boolean inCharClass = false;
        for( int i=0; i<p.length(); i++ ){
            char c = p.charAt( i );

            if ( c == '\\' ) {
                int end = i+1;

                // unicode
                if ( i+1 < p.length() && p.charAt( i+1 ) == 'u' ) {
                    end = end + 1;
                    while( end < p.length() && isHex( p.charAt( end ) ) && end < i+6 )
                        end++;

                    // only escape unicode if it's valid
                    if( end - (i+2) == 4 ) {
                        buf.append( "\\" );
                    }

                    continue;
                }
                boolean isOctal = ( end < p.length() ) ? ( p.charAt( end ) == '0' ) : false;
                while( end < p.length() && 
                       ( Character.isDigit( p.charAt( end ) ) && 
                         ( !isOctal || ( isOctal && p.charAt( end ) < '8' ) ) ) ) {
                    end++;
                }
                // to octal
                // always escape in char classes
                if( end - (i+1) > 1 || 
                    ( end - (i+1) == 1 && ( inCharClass || isOctal ) ) ) {
                    buf.append( (char)Integer.parseInt( p.substring( i+1, end ) , 8 ) );
                    i = end - 1;
                }
                // back ref
                else {
                    buf.append( "\\" );
                }
                continue;
            }

            if ( inCharClass ){
                if ( c == '[' &&
                     p.charAt( i - 1 ) != '\\'  )
                    buf.append( "\\" );

                if ( c == ']' &&
                     p.charAt( i - 1 ) != '\\'  )
                    inCharClass = false;

                buf.append( c );
                continue;
            }

            if ( p.charAt( i ) == '[' &&
                 ( i == 0 || p.charAt( i - 1 ) != '\\' ) )
                inCharClass = true;

            buf.append( c );
        }

        return buf.toString();
    }

    /** Initialize a regular expression from the given string with options.  Valid option strings can be any combination of "i", for "case insensitive", "g", for "global", and "m" for "multiline input".
     * Using the "m" option causes ^ and $ to match the beginnning and end of a line, respectively, versus the beginning and end of the input they would match normally.
     * @param p Regular expression
     * @param f Flags
     */
    void init( String p , String f ){
        _p = _jsToJava( p );
        _f = f == null ? "" : f;

        {
            int compilePatterns = 0;
            if ( f.contains( "i" ) )
                compilePatterns |= Pattern.CASE_INSENSITIVE;
            if ( f.contains( "m" ) ){
                compilePatterns |= Pattern.MULTILINE;
	    }
            _compilePatterns = compilePatterns;
        }

        _replaceAll = f.contains( "g" );

        try {
            _patt = Pattern.compile( _p , _compilePatterns );
        }
        catch ( PatternSyntaxException pe ){
            throw new RuntimeException( "bad pattern \"" + _p + "\" : " + pe.getMessage() );
        }
    }

    /** Returns this regular expression.
     * @return This regular expression.
     */
    public String getPattern(){
        return _p;
    }

    /** Returns this regular expression's flags.
     * @return Flags.
     */
    public String getFlags(){
        return _f;
    }

    /** Returns this regular expression and flags in /expression/flags form.
     * @return /expression/flags
     */
    public String toString(){
        if( _p == null )
            _p = "(?:)";
        if( _f == null )
            _f = "";
        return "/" + _p + "/" + _f;
    }

    /** The hash code value of this regular expression.
     * @return The hash code value of this regular expression.
     */
    public int hashCode( IdentitySet seen ){
        return _p.hashCode() + _f.hashCode();
    }

    /** Tests if the string equivalents of this and the given object are equal.
     * @param o Object to test equality against.
     * @return Whether this and the given object are equal.
     */
    public boolean equals( Object o ){
        return toString().equals( o.toString() );
    }

    /** Get this regular expression's compiled pattern.
     * @return The compiled pattern.
     */
    public Pattern getCompiled(){
        return _patt;
    }

    /** Attempts to match an entire region against the pattern.
     * @param String to match against pattern
     * @return true if, and only if, a subsequence of the input sequence matches this matcher's pattern
     */
    public boolean matches( String s ){
        Matcher m = _patt.matcher( s );
        return m.matches();
    }

    /** Returns if the given string contained a match for this pattern.
     * @param s String to attempt to match.
     * @return If the given string contained a match for this pattern.
     */
    public boolean test( String s ){
        if( s == null )
            s = JSRegex.input;
        Matcher m = _patt.matcher( s );
        return m.find();
    }

    /** Applies this regular expression to the given string and returns an array of all matching substrings.
     * @param s String to match.
     * @return Array of matches.
     */
    public JSArray exec( String s ){
        return exec( s , true );
    }

    /** Applies this regular expression to the given string and returns an array of all matching substrings, with the option of using the last matcher if the string is the same as last time.
     * @param s String to match.
     * @param canUseOld If the old matcher can be used, or a new one must be created.
     * @return Array of matches.
     */
    public JSArray exec( String s , boolean canUseOld ){
        if( s == null ) {
            s = input;
        }
        JSArray a = _last.get();
        String oldString = a == null ? null : a.get( "input" ).toString();
        Matcher m = null;

        if ( canUseOld && a != null && s == oldString && s.equals( oldString ) ){
            m = (Matcher)a.get( "_matcher" );
        }
        else {
            m = _patt.matcher( s );
        }

        if ( ! m.find() )
            return null;

        a = new JSArray();
        for ( int i=0; i<=m.groupCount(); i++ ){
            String temp = m.group(i);
            if ( temp == null )
                a.add( null );
            else
                a.add( new JSString( temp ) );
        }

        a.set( "_matcher" , m );
        a.set( "input" , new JSString( s ) );
        a.set( "index" , m.start() );

        if ( _replaceAll )
            _last.set( a );
        else
            _last.set( null );
        _lastRegex.set( this );
        matchArray = new JSArray( a );

        return a;
    }

    /** Get the array of matches generated last.
     * @return An array of matching strings.
     */
    public JSArray getLast(){
        return _last.get();
    }

    /** Escape special RegExp characters in a string. Escapes \, ^, $, *, +, {, }, [, ], (, ), -, ?, and .
     * @param s String to escape.
     * @return Escaped string.
     */
    public static String quote( String s ){
        StringBuilder buf = new StringBuilder( s.length() + 10 );
        for ( int i=0; i<s.length(); i++ ){
            char c = s.charAt( i );
            switch ( c ){
            case '\\':
            case '^':
            case '$':
            case '*':
            case '+':
            case '{':
            case '}':
            case '[':
            case ']':
            case '(':
            case ')':
            case '-':
            case '?':
            case '.':
                buf.append( "\\" );
            default:
                buf.append( c );
            }
        }
        return buf.toString();
    }

    static JSArray matchArray = new JSArray();
    static String input = "";

    /** @unexpose */
    String _p;
    /** @unexpose */
    String _f;

    /** @unexpose */
    int _compilePatterns;
    /** @unexpose */
    Pattern _patt;

    /** @unexpose */
    boolean _replaceAll;

    /** @unexpose */
    ThreadLocal<JSArray> _last = new ThreadLocal<JSArray>();
    /** @unexpose */
    static ThreadLocal<JSRegex> _lastRegex = new ThreadLocal<JSRegex>();
}
