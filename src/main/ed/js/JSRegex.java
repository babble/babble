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
    public static class Cons extends JSFunctionCalls2{

            public JSObject newOne(){
                return new JSRegex();
            }

            public Object call( Scope s , Object a , Object b , Object[] args ){

                String p = a.toString();
                String f = b == null ? "" : b.toString();

                Object o = s.getThis();
                if ( o == null || ! ( o instanceof JSRegex ) )
                    return new JSRegex( p , f );

                JSRegex r = (JSRegex)o;
                r.init( p , f );
                return r;
            }

            protected void init(){
                _prototype.set( "test" , new JSFunctionCalls1(){
                        public Object call( Scope s , Object o , Object foo[] ){
                            if ( o == null )
                                return false;
                            return ((JSRegex)s.getThis()).test( o.toString() );
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
    }

    /** @unexpose */
    static String _jsToJava( String p ){
        StringBuilder buf = new StringBuilder( p.length() + 10 );

        boolean inCharClass = false;

        for( int i=0; i<p.length(); i++ ){
            char c = p.charAt( i );

            if ( c == '\\' &&
                 i + 1 < p.length() &&
                 Character.isDigit( p.charAt( i + 1 ) ) ){

                // this is an escape sequence
                int end = i + 1;
                while ( end < p.length() &&
                        Character.isDigit( p.charAt( end ) ) &&
                        end - i < 3
                        )
                    end++;

                int foo = Integer.parseInt( p.substring( i + 1 , end ) , 8 );
                char myChar = (char)foo;

                buf.append( myChar );
                i = end - 1;
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
     * @param f Options
     */
    void init( String p , String f ){
        _p = _jsToJava( p );
        _f = f == null ? "" : f;

        {
            int compilePatterns = 0;
            if ( f.contains( "i" ) )
                compilePatterns |= Pattern.CASE_INSENSITIVE;
            if ( f.contains( "m" ) )
                compilePatterns |= Pattern.DOTALL;
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
