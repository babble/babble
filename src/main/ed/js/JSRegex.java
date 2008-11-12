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
import ed.appserver.AppRequest;

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
                    String g = r.global ? "g" : "";
                    String i = r.ignoreCase ? "i" : "";
                    String m = r.multiline ? "m" : "";
                    ((JSRegex)o).init( r.source, g+i+m );
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
            if( s.equals( "$_" ) ) {
                return get( "input" );
            }
            else if( s.equals( "$&" ) ) {
                return get( "lastMatch" );
            }
            else if( s.equals( "$+" ) ) {
                return get( "lastParen" );
            }
            else if( s.equals( "$`" ) ) {
                return get( "leftContext" );
            }
            else if( s.equals( "$'" ) ) {
                return get( "rightContext" );
            }
            else if( s.equals( "$*" ) ) {
                return get( "multiline" );
            }
            else if( s.startsWith( "$" ) ) {
                int m = Integer.parseInt( s.substring( 1 ) );
                Object obj = ((JSArray)get( "matchArray" )).get( m );
                return obj == null ? "" : obj.toString();
            }

            return super.get( o );
        }

        public Object set( Object n, Object v ) {
            if( n == null ) 
                return false;
            if( v == null )
                v = "";

            String s = n.toString();
            if( s.equals( "$_" ) ) {
                set( "input", v.toString() );
                return v;
            }
            else if( s.equals( "$*" ) ) {
                set( "multiline", Boolean.parseBoolean( v.toString() ) );
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
            _prototype.set( "compile", new JSFunctionCalls1() {
                    public Object call( Scope s, Object o , Object foo[] ) {
                        if( foo == null || foo.length == 0 ) 
                            ((JSRegex)s.getThis()).init( o.toString(), (String)null );
                        else
                            ((JSRegex)s.getThis()).init( o.toString(), foo[0].toString());
                        return null;
                    }
                } );
            _prototype.set( "__rmatch" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object o , Object foo[] ){
                        
                        if ( o == null )
                            return -1;
                        
                        String str = o.toString();
                        
                        JSRegex r = (JSRegex)s.getThis();
                        JSArray a = r.exec( str );
                        r._setLast( a );
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
                            r._setLast( a );
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
                set( "matchArray", new JSArray() );
                set( "input", "" );
                set( "lastMatch", null );
                set( "lastParen", "" );
                set( "leftContext", "" );
                set( "rightContext", "" );
                set( "multiline", false );
                
                dontEnumExisting();
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
        CachedResult cr = _last.get();
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
            if ( f.contains( "m" ) || (Boolean)_cons.get( "multiline" ) ){
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
            _last.get().lastIndex = Integer.parseInt( v.toString() );
        }
        return v;
    }

    public Object get( Object n ) {
        String s = n.toString();
        if( s.equals( "lastIndex" ) ) {
            return _last.get().lastIndex;
        }
        if( s.equals( "source" ) ) {
            return this.source;
        }
        if( s.equals( "global" ) ) {
            return this.global;
        }
        if( s.equals( "ignoreCase" ) ) {
            return this.ignoreCase;
        }
        if( s.equals( "multiline" ) ) {
            return this.multiline;
        }
        return super.get( n );
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
        if( source == null )
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
        Matcher m = _patt.matcher( s );
        if( s == null )
            s = getConstructor().get( "input" ) + "";

        CachedResult cr = _last.get();
        
        int idx = cr.lastIndex;
        if ( idx >= s.length() )
            idx = 0;
        boolean b = m.find( idx );
        
        if( global ) {
            _cons.set( "lastMatch", b ? m.group(0) : null );
            _cons.set( "lastParen", b && m.groupCount() > 0 ? m.group( m.groupCount() ) : "" );
            _cons.set( "leftContext", b ? s.substring( 0, m.start(0) ) : "" );
            _cons.set( "rightContext", b ? s.substring( m.end(0) ) : "" );

            cr.lastIndex = b ? m.end() : 0;
        }
        return b;
    }

    /** Applies this regular expression to the given string and returns an array of all matching substrings.
     * @param s String to match.
     * @return Array of matches.
     */
    public JSArray exec( String s ){
        return exec( s == null ? (getConstructor().get( "input" )+"") : s , true );
    }

    /** Applies this regular expression to the given string and returns an array of all matching substrings, with the option of using the last matcher if the string is the same as last time.
     * @param s String to match.
     * @param canUseOld If the old matcher can be used, or a new one must be created.
     * @return Array of matches.
     */
    public JSArray exec( final String s , final boolean canUseOld ){
        
        CachedResult cr = _last.get();
        if( cr == null ){
            _last.set( new CachedResult( s, null, AppRequest.getThreadLocal(), null ) );
            cr = _last.get();
        }

        final Matcher m;
        if ( canUseOld && cr._matcher != null && s == cr._input && s.equals( cr._input ) && cr._request == AppRequest.getThreadLocal() ){
            m = cr._matcher;
        }
        else {
            m = _patt.matcher( s );
        }

        if ( cr.lastIndex > s.length() || !m.find( cr.lastIndex ) ) {
            if( global ) {
                cr.lastIndex = 0;
            }
            _cons.set( "lastMatch", null );
            _cons.set( "lastParen", "" );
            _cons.set( "leftContext", "" );
            _cons.set( "rightContext", "" );
            return null;
        }

        JSArray a = new JSArray();
        for ( int i=0; i<=m.groupCount(); i++ ){
            String temp = m.group(i);
            if ( temp == null )
                a.add( null );
            else
                a.add( new JSString( temp ) );
        }
        _cons.set( "lastMatch", m.group(0) );
        _cons.set( "lastParen", m.groupCount() > 0 ? m.group( m.groupCount() ) : "" );
        _cons.set( "leftContext", s.substring( 0, m.start(0) ) );
        _cons.set( "rightContext", s.substring( m.end(0) ) );

        a.set( "_matcher" , m );
        a.set( "input" , new JSString( s ) );
        a.set( "index" , m.start() );
        if( global ) {
            cr.lastIndex = m.end();
        }

        if ( _replaceAll ){
            if ( cr == null || cr._request != AppRequest.getThreadLocal() ){
                _last.set( new CachedResult( s, m, AppRequest.getThreadLocal(), a ) );
            }
            else {
                cr._array = a;
            }
        }
        else {
            _last.set( null );
        }
        
        _lastRegex.set( this );
        _cons.set("matchArray", new JSArray( a ) );

        return a;
    }

    void _setLast( JSArray arr ){
        CachedResult cr = _last.get();
        if( cr == null || cr._request != AppRequest.getThreadLocal() ) {
            _last.set( new CachedResult( null, null, null, null ) );
            cr = _last.get();
        }
        cr._request = AppRequest.getThreadLocal();
        cr._array = arr;
        if( arr == null )
            return;
        cr._input = arr.get( "input" )+"";
        cr._matcher = (Matcher)arr.get( "_matcher" );
    }

    /** Get the array of matches generated last.
     * @return An array of matching strings.
     */
    public JSArray getLast(){
        CachedResult cr = _last.get();
        if ( cr == null )
            return null;
        return cr._array;
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

    String source = null;
    boolean global = false;
    boolean multiline = false;
    boolean ignoreCase = false;

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

    class CachedResult {        
        CachedResult( String input , Matcher m , AppRequest req , JSArray arr ){
            _input = input;
            _matcher = m;
            _request = req;
            _array = arr;
        }
        
        int lastIndex = 0;

        String _input;
        Matcher _matcher;
        AppRequest _request;
        
        JSArray _array;
    }

    /** @unexpose 
     * Not only thread local, but CachedResult should be re-created on every HTTP request
     */
    class CRLast extends ThreadLocal<CachedResult>{

        public CachedResult get(){

            CachedResult cr = super.get();
            if ( cr == null )
                return null;
            
            AppRequest ar = AppRequest.getThreadLocal();
            if ( ar == cr._request )
                return cr;
            
            cr = initialValue();
            set( cr );
            return cr;
        }
        
        @Override protected CachedResult initialValue() {
            return new CachedResult( null, null, AppRequest.getThreadLocal(), null );
        }
    };

    private CRLast _last = new CRLast();
    
    /** @unexpose */
    static ThreadLocal<JSRegex> _lastRegex = new ThreadLocal<JSRegex>();
    static int num =0;
}
