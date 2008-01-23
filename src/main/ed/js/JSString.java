// JSString.java

package ed.js;

import java.util.regex.*;

import com.twmacinta.util.*;

import ed.js.func.*;
import ed.js.engine.*;

public class JSString extends JSObjectBase {

    public static JSFunction _cons = new JSStringCons();
    
    static class JSStringCons extends JSFunctionCalls1{

        public JSObject newOne(){
            return new JSString("");
        }
        
        public Object call( Scope s , Object foo , Object[] args ){

            Object o = s.getThis();
            if ( o == null ){
                if ( foo == null )
                    return new JSString( "" );
                return new JSString( foo.toString() );
            }
            
            if ( ! ( o instanceof JSString ) )
                throw new RuntimeException( "something is very broken" );
            
            JSString str = (JSString)o;
            if ( foo != null )
                str._s = foo.toString();
            
            return o;
        }
        
        protected void init(){
            
            _prototype.set( "trim" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        return new JSString( s.getThis().toString().trim() );
                    }
                } );


            _prototype.set( "md5" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object extra[] ){
                        synchronized ( _myMd5 ){
                            _myMd5.Init();
                            _myMd5.Update( s.getThis().toString() );
                            return new JSString( _myMd5.asHex() );
                        }
                    }
                    
                    private final MD5 _myMd5 = new MD5();
                } );


            _prototype.set( "toLowerCase" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        return new JSString( s.getThis().toString().toLowerCase() );
                    }
                } );

            _prototype.set( "toUpperCase" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        return new JSString( s.getThis().toString().toUpperCase() );
                    }
                } );


            _prototype.set( "charCodeAt" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        String str = s.getThis().toString();
                        int idx = ((Number)o).intValue();
                        return Integer.valueOf( str.charAt( idx ) );
                    }
                } );
            
                
            _prototype.set( "charAt" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        String str = s.getThis().toString();
                        int idx = ((Number)o).intValue();
                        if ( idx >= str.length() || idx < 0 )
                            return EMPTY;
                        return new JSString( str.substring( idx , idx + 1 ) );
                    }
                } );

            _prototype.set( "indexOf" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        String str = s.getThis().toString();
                        String thing = o.toString();
                        
                        int start = 0;
                        if ( foo != null && foo.length > 0 )
                            start = ((Number)foo[0]).intValue();

                        return str.indexOf( thing , start );
                    }
                } );

            _prototype.set( "startsWith" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        String str = s.getThis().toString();
                        String thing = o.toString();
                        
                        return str.startsWith( thing );
                    }
                } );

            _prototype.set( "endsWith" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        String str = s.getThis().toString();
                        String thing = o.toString();
                        
                        return str.endsWith( thing );
                    }
                } );

            _prototype.set( "substring" , new JSFunctionCalls2() {
                    public Object call( Scope s , Object startO , Object endO , Object foo[] ){
                        String str = s.getThis().toString();

                        int start = ((Number)startO).intValue();
                        if ( start < 0 )
                            start = 0;
                        if ( start >= str.length() || start < 0 )
                            return EMPTY;
                        
                        int end = -1;
                        if ( endO != null && endO instanceof Number )
                            end = ((Number)endO).intValue();
                        
                        if ( end > str.length() )
                            end = str.length();

                        if ( end < 0 )
                            return new JSString( str.substring( start) );
                        return new JSString( str.substring( start , end ) );
                    }
                } );

            _prototype.set( "substr" , new JSFunctionCalls2() {
                    public Object call( Scope s , Object startO , Object lengthO , Object foo[] ){
                        String str = s.getThis().toString();

                        int start = ((Number)startO).intValue();
                        if ( start < 0 )
                            start = 0;
                        if ( start >= str.length() || start < 0 )
                            return EMPTY;
                        
                        int length = -1;
                        if ( lengthO != null && lengthO instanceof Number )
                            length = ((Number)lengthO).intValue();
                        
                        if ( start + length > str.length() )
                            length = str.length() - start;

                        if ( length < 0 )
                            return new JSString( str.substring( start) );
                        return new JSString( str.substring( start , start + length ) );
                    }
                } );


            _prototype.set( "match" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        String str = s.getThis().toString();

                        if ( o instanceof String || o instanceof JSString )
                            o = new JSRegex( o.toString() , "" );
                            
                        if ( ! ( o instanceof JSRegex ) )
                            throw new RuntimeException( "not a regex : " + o.getClass() );
                            
                        JSRegex r = (JSRegex)o;
                        Matcher m = r._patt.matcher( str );
                        if ( ! m.find() )
                            return null;
                        return m.group(0);
                    }
                } );
                

            _prototype.set( "split" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object o , Object crap[] ){                        
                            
                        String str = s.getThis().toString();

                        if ( o instanceof String || o instanceof JSString )
                            o = new JSRegex( o.toString() , "" );
                            
                        if ( ! ( o instanceof JSRegex ) )
                            throw new RuntimeException( "not a regex : " + o.getClass() );
                            
                        JSRegex r = (JSRegex)o;

                        JSArray a = new JSArray();
                        for ( String pc : r._patt.split( str ) )
                            a.add( new JSString( pc ) );
                            
                        return a;
                    }
                }
                );
                
            _prototype.set( "replace" , new JSFunctionCalls2() {
                    public Object call( Scope s , Object o , Object repl , Object crap[] ){
                        String str = s.getThis().toString();

                        if ( o instanceof String || o instanceof JSString )
                            o = new JSRegex( o.toString() , "" );
                            
                        if ( ! ( o instanceof JSRegex ) )
                            throw new RuntimeException( "not a regex : " + o.getClass() );
                            
                        JSRegex r = (JSRegex)o;
                        Matcher m = r._patt.matcher( str );
                          
                        StringBuffer buf = null;
                        int start = 0;

                        Object replArgs[] = null;
                                        
                        while ( m.find() ){
                            if ( buf == null )
                                buf = new StringBuffer( str.length() );
                                
                            buf.append( str.substring( start , m.start() ) );
                                
                            if ( repl instanceof JSString ){
                                String foo = repl.toString();
                                for ( int i=0; i<foo.length(); i++ ){
                                    char c = foo.charAt( i );

                                    if ( c != '$' ){
                                        buf.append( c );
                                        continue;
                                    }
                                        
                                    if ( i + 1 >= foo.length() ||
                                         ! Character.isDigit( foo.charAt( i + 1 ) ) ){
                                        buf.append( c );
                                        continue;
                                    }
                                        
                                    i++;
                                    int end = i;
                                    while ( end < foo.length() && Character.isDigit( foo.charAt( end ) ) )
                                        end++;
                                        
                                    int num = Integer.parseInt( foo.substring( i , end ) );
                                    buf.append( m.group( num ) );
                                        
                                    i = end - 1;
                                } 
                            }
                            else if ( repl instanceof JSFunction ){
                                if ( replArgs == null )
                                    replArgs = new Object[ m.groupCount() + 1 ];
                                for ( int i=0; i<replArgs.length; i++ )
                                    replArgs[i] = new JSString( m.group( i ) );
                                buf.append( ((JSFunction)repl).call( s , replArgs ) );
                            }
                            else {
                                throw new RuntimeException( "can't use replace with : " + repl.getClass() );
                            }
                                
                            start = m.end();

                            if ( ! r._replaceAll )
                                break;
                        }
                            
                        if ( buf == null )
                            return new JSString( str );
                            
                        buf.append( str.substring( start ) );
                        return new JSString( buf.toString() );
                    }
                } );
        }
    };
    
    static JSString EMPTY = new JSString("");

    public JSString( String s ){
        super( _cons );
        _s = s;
    }
    
    public Object get( Object name ){
        
        if ( name instanceof JSString )
            name = name.toString();
        
        if ( name instanceof String && name.toString().equals( "length" ) )
            return Integer.valueOf( _s.length() );

        return super.get( name );
    }
    
    public String toString(){
        return _s;
    }
    
    public int length(){
        return _s.length();
    }
    
    public int hashCode(){
        return _s.hashCode();
    }

    public boolean equals( Object o ){

        if ( o == null )
            return _s == null;
        
        if ( _s == null )
            return false;
        
        return _s.equals( o.toString() );
    }

    String _s;
}
