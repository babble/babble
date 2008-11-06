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
            Object o = s.getThis();
            if( a instanceof JSRegex ) {
                // can't pass flags to an existing regex
                if( args != null && 
                    args.length > 0 && 
                    args[0] != null &&
                    args[0] != VOID ) {
                    throw new JSException( "can't supply flags when constructing one RegExp from another " );
                }

                // calling RegExp( regex ) returns regex
                if( !(o instanceof JSRegex ) ) {
                    return a;
                }
                // create a new regexp from the parameter
                else {
                    JSRegex r = (JSRegex)a;
                    String flags = ( r.global ? "g" : "" ) + 
                        ( r.ignoreCase ? "i" : "" ) + 
                        ( r.multiline ? "m" : "" );
                    ((JSRegex)o).init( ((JSRegex)a).source, flags );
                    return o;
                }
            }

            // null turns into "null"
            String p = a + "";
            String f = "";
            if( args != null && args.length > 0 )
                f = getFlags( args[0] );

            if ( o == null || ! ( o instanceof JSRegex ) )
                return new JSRegex( p , f );
            
            JSRegex r = (JSRegex)o;
            r.init( p , f );
            return r;
        }

        private String getFlags( Object o ) {
            if( o == null ) {
                return "";
            }
            if( !(o instanceof String || o instanceof JSString) || 
                !Pattern.matches( "[gim]*", o.toString() ) ) {
                throw new JSException( "Syntax Error: illegal flags " + o );
            }
            return o.toString();
        }

        public Object get( Object o ) {
            if( o == null )
                return null;
            
            String s = o.toString();
            if( s.equals( "$_" ) || s.equals( "input" ) ) {
                return JSRegex.input;
            }
            else if( s.equals( "$&" ) ) {
                return JSRegex.lastMatch;
            }
            else if( s.startsWith( "$" ) ) {
                int m = Integer.parseInt( s.substring( 1 ) );
                Object obj = matchArray.get( m );
                return obj == null ? "" : obj.toString();
            }
            else if( s.equals( "lastMatch" ) ) {
                return JSRegex.lastMatch;
            }
            else if( s.equals( "lastParen" ) ) {
                return JSRegex.lastParen;
            }

            return super.get( o );
        }

        public Object set( Object n, Object v ) {
            if( n == null ) 
                return false;
            if( v == null )
                v = "";

            String s = n.toString();
            if( s.equals( "$_" ) || s.equals( "input" ) ) {
                JSRegex.input = v.toString();
                return v;
            }
            return super.set( n,v );
        }

        protected void init(){
            final JSObject proto = _prototype;

            _prototype.set( "toString" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        Object o = s.getThis();
                        if( !(o instanceof JSRegex ) ){
                            return null;
                        }
                        JSRegex r = (JSRegex)o;
                        String source = r.source;
                        // default source
                        if( source.equals( "" ) )
                            source = "(?:)";
                        String g = r.global ? "g" : "";
                        String i = r.ignoreCase ? "i" : "";
                        String m = r.multiline ? "m" : "";
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
    private JSRegex(){
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
    }

    public void setProps( String p, String f) {
        source = p;
        global = f.indexOf( "g" ) >= 0;
        ignoreCase = f.indexOf( "i" ) >= 0;
        multiline = f.indexOf( "m" ) >= 0;
    }


    private static final boolean isHex( char c ) {
        return (c >= '0' && c <= '9') || 
            (c >= 'a' && c <= 'f') || 
            (c >= 'A' && c <= 'F');
    }

    /** @unexpose */
    static String _jsToJava( String p ){
        StringBuilder buf = new StringBuilder( p.length() + 10 );
        int parenCount = 0;

        boolean inCharClass = false;
        for( int i=0; i<p.length(); i++ ){
            char c = p.charAt( i );

            if ( c == '(' ) {
                parenCount++;
            }

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
                if( end - (i+1) >= 1 && ( inCharClass || isOctal ) ) {
                    char ch = (char)Integer.parseInt( p.substring( i+1, end ) , 8 );
                    // if this is a "reserved" character and we aren't in a char class, escape it
                    if( !inCharClass && Pattern.matches( "[-^$*+()\\[\\]{}?!<>,\\.]", ch+"" ) )
                        buf.append( "\\" + ch );
                    else { // octal 
                        buf.append( ch );
                    }
                    i = end - 1;
                }
                // back ref
                else {
                    // ignore back references greater than the number of refs
                    if( i+1 < p.length() &&
                        Character.isDigit( p.charAt( i+1 ) ) &&
                        p.charAt( i+1 ) - '0' > parenCount ) {
                        i++;
                        continue;
                    }
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
                 ( i == 0 || p.charAt( i - 1 ) != '\\' ) ) {
                // HACK
                // []
                if( p.charAt( i+1 ) == ']' ) {
                    buf.append( "[\uFFFF]" );
                    i++;
                    continue;
                }
                // [^]
                if( p.charAt( i + 1 ) == '^' && p.charAt( i + 2 ) == ']' ) {
                    buf.append( "[^\uFFFF]" );
                    i += 2;
                    continue;
                }

                inCharClass = true;
            }
            buf.append( c );
        }

        return buf.toString();
    }

    /** Initialize a regular expression from the given string with options.  Valid option strings can be any combination of "i", for "case insensitive", "g", for "global", and "m" for "multiline input".
     * Using the "m" option causes ^ and $ to match the beginnning and end of a line, respectively, versus the beginning and end of the input they would match normally.
     * @param p Regular expression
     * @param f Flags
     */
    private void init( String p , String f ){
        setProps( p, f );
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

    public Object set( Object n, Object v ) {
        String s = n.toString();
        if( s.equals( "lastIndex" ) ) {
            lastIndex = Integer.parseInt( v.toString() );
        }
        return v;
    }

    public Object get( Object n ) {
        String s = n.toString();
        if( s.equals( "lastIndex" ) ) {
            return lastIndex;
        }
        return null;
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
        String source = this.source;
        if( this.source == null )
            source = "(?:)";
        if( _f == null )
            _f = "";
        return "/" + source + "/" + _f;
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

        if ( lastIndex >= s.length() || !m.find( lastIndex ) ) {
            lastIndex = 0;
            lastMatch = null;
            lastParen = "";
            return null;
        }

        a = new JSArray();
        for ( int i=0; i<=m.groupCount(); i++ ){
            String temp = m.group(i);
            if ( temp == null )
                a.add( null );
            else
                a.add( new JSString( temp ) );
        }
        lastMatch = m.group(0);
        lastParen = m.groupCount() > 0 ? m.group( m.groupCount() ) : "";

        a.set( "_matcher" , m );
        a.set( "input" , new JSString( s ) );
        a.set( "index" , m.start() );
        lastIndex = m.end();

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
    static String lastMatch = null;
    static String lastParen = "";

    /** @unexpose */
    String _p;
    /** @unexpose */
    String _f;

    private String source = null;
    private boolean global = false;
    private boolean multiline = false;
    private boolean ignoreCase = false;
    private int lastIndex = 0;

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
