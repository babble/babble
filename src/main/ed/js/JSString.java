// JSString.java

package ed.js;

import java.util.regex.*;

import ed.js.func.*;
import ed.js.engine.*;

public class JSString extends JSObjectBase {

    static JSString EMPTY = new JSString("");

    public static JSFunction _cons = new JSStringCons();
    
    static class JSStringCons extends JSFunctionCalls0{

            public Object call( Scope s , Object[] args ){
                throw new RuntimeException( "can't be here" );
            }

            protected void init(){

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
