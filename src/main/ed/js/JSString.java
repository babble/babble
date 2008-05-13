// JSString.java

package ed.js;

import java.util.regex.*;

import com.twmacinta.util.*;

import ed.util.*;
import ed.js.func.*;
import ed.js.engine.*;

public class JSString extends JSObjectBase {

    static { JS._debugSIStart( "JSString" ); }

    static { JS._debugSI( "JSString" , "0" ); }
    public static JSFunction _cons = new JSStringCons();
    static { JS._debugSI( "JSString" , "1" ); }
    
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
            
            JS._debugSI( "JSString" , "JSStringCons init 0" );
            
            final JSObject myPrototype = _prototype;

            if ( ! JS.JNI ){
                final StringEncrypter encrypter = new StringEncrypter( "knsd8712@!98sad" );
                
                _prototype.set( "encrypt" , new JSFunctionCalls0(){
                        public Object call( Scope s , Object foo[] ){
                            synchronized ( encrypter ){
                                return new JSString( encrypter.encrypt( s.getThis().toString() ) );
                            }
                        }        
                    } );
                
                _prototype.set( "decrypt" , new JSFunctionCalls0(){
                        public Object call( Scope s , Object foo[] ){
                            synchronized ( encrypter ){
                                return new JSString( encrypter.decrypt( s.getThis().toString() ) );
                            }
                        }        
                    } );
            }


            JS._debugSI( "JSString" , "JSStringCons init 1" );
                
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

            _prototype.set( "to_sym" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object extra[] ){
                        return s.getThis().toString();
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

            _prototype.set( "contains" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        String str = s.getThis().toString();
                        String thing = o.toString();
                        
                        return str.contains( thing );
                    }
                } );

            _prototype.set( "lastIndexOf" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        String str = s.getThis().toString();
                        String thing = o.toString();
                        
                        int end = str.length();
                        if ( foo != null && foo.length > 0 )
                            end = ((Number)foo[0]).intValue();

                        return str.lastIndexOf( thing , end );
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
                        if ( r.getFlags().contains( "g" )){
                            JSArray a = new JSArray();
                            do {
                                a.add(new JSString(m.group()));
                            } while(m.find());
                            return a;
                        }
                        else{
                            return r.exec(str);
                        }
                    }
                } );
                

            _prototype.set( "split" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object o , Object extra[] ){                        
                            
                        String str = s.getThis().toString();

                        if ( o instanceof String || o instanceof JSString )
                            o = new JSRegex( o.toString() , "" );
                            
                        if ( ! ( o instanceof JSRegex ) )
                            throw new RuntimeException( "not a regex : " + o.getClass() );
                        
                        int limit = Integer.MAX_VALUE;
                        if ( extra != null && extra.length > 0 && extra[0] instanceof Number )
                            limit = ((Number)extra[0]).intValue();
    
                        JSRegex r = (JSRegex)o;
                        
                        String spacer = null;
                        if ( r.getPattern().contains( "(" ) )
                            spacer = r.getPattern().replaceAll( "[()]" , "" );

                        JSArray a = new JSArray();
                        for ( String pc : r._patt.split( str , -1 ) ){
                            if ( a.size() > 0 && spacer != null )
                                a.add( spacer );
                            a.add( new JSString( pc ) );
                            if ( a.size() >= limit )
                                break;
                        }
                            
                        return a;
                    }
                }
                );

            _prototype.set( "reverse" , new JSFunctionCalls0(){
                    public Object call(Scope s, Object [] args){
                        String str = s.getThis().toString();
                        StringBuffer buf = new StringBuffer( str.length() );
                        for ( int i=str.length()-1; i>=0; i--)
                            buf.append( str.charAt( i ) );
                        return new JSString( buf.toString() );
                    }
                } );

            _prototype.set( "pluralize" , new JSFunctionCalls0(){
                    public Object call(Scope s, Object [] args){
                        String str = s.getThis().toString();
                        return str + "s";
                    }
                } );
            

            
            _prototype.set( "each_byte" , new JSFunctionCalls1(){
                    public Object call(Scope s, Object funcObject , Object [] args){

                        if ( funcObject == null )
                            throw new NullPointerException( "each_byte needs a function" );
                        
                        JSFunction func = (JSFunction)funcObject;

                        String str = s.getThis().toString();
                        for ( int i=0; i<str.length(); i++ ){
                            func.call( s , (int)str.charAt( i ) );
                        }
                        return null;
                    }
                } );
            

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
                        
                        final JSObject options = ( crap != null && crap.length > 0  && crap[0] instanceof JSObject ) ? (JSObject)crap[0] : null;
                        final boolean replaceAll = r._replaceAll || ( options != null && options.get( "all" ) != null );

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

                            if ( ! replaceAll )
                                break;
                        }
                            
                        if ( buf == null )
                            return new JSString( str );
                            
                        buf.append( str.substring( start ) );
                        return new JSString( buf.toString() );
                    }
                } );

            _prototype.set( "sub" , _prototype.get( "replace" ) );
            final JSObjectBase gsubOptions = new JSObjectBase();
            gsubOptions.set( "all" , "asd" );
            final Object gsubOptionsArray[] = new Object[]{ gsubOptions };

            _prototype.set( "gsub" , new JSFunctionCalls2() {
                    public Object call( Scope s , Object o , Object repl , Object crap[] ){
                        return ((JSFunction)myPrototype.get( "replace" )).call( s , o , repl , gsubOptionsArray );
                    }
                } );


            set("fromCharCode", new JSFunctionCalls0() {
                    public Object call(Scope s, Object [] args){
                        if(args == null) return new JSString("");
                        StringBuffer buf = new StringBuffer();
                        for(int i = 0; i < args.length; i++){
                            Object o = args[i];
                            if(! (o instanceof Number) )
                                throw new RuntimeException( "fromCharCode only takes numbers" );
                            Number n = (Number)o;
                            char c = (char)(n.intValue());
                            buf.append(c);
                        }
                        return new JSString( buf.toString() );
                    }
                } );


        }
    };

    static { JS._debugSI( "JSString" , "2" ); }    

    static JSString EMPTY = new JSString("");

    public JSString( String s ){
        super( _cons );
        _s = s;
    }
    
    public JSString( char [] c ){
        this(new String(c));
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
    
    public int compareTo( Object o ){
	if ( o == null ) o = "";
	return _s.compareTo( o.toString() );
    }

    public int length(){
        return _s.length();
    }

    public Object getInt( int n ){
        if ( n >= _s.length() )
            return null;
        // Eliot said that we should map characters to objects in scope.java
        return new JSString(new char[]{_s.charAt( n )});
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

    public byte[] getBytes(){
        return _s.getBytes();
    }

    String _s;

    static { JS._debugSIDone( "JSString" ); }
}
