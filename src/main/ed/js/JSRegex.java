// JSRegex.java

package ed.js;

import java.util.regex.*;

import ed.js.func.*;
import ed.js.engine.*;

public class JSRegex extends JSObjectBase {

    public final static JSFunction _cons = new JSFunctionCalls2(){

            public JSObject newOne(){
                return new JSRegex();
            }

            public Object call( Scope s , Object a , Object b , Object[] args ){

                String p = a.toString();
                String f = b == null ? "" : b.toString();

                JSObject o = s.getThis();
                if ( o == null )
                    return new JSRegex( p , f );
                
                JSRegex r = (JSRegex)o;
                r.init( p , f );
                return r;
            }

            protected void init(){

            }
        };
    
    public JSRegex(){
        super( _cons );
    }

    public JSRegex( String p , String f ){
        super( _cons );
        init( p , f );
    }
    
    void init( String p , String f ){
        _p = p;
        _f = f;
        
        {
            int compilePatterns = 0;
            if ( f.contains( "i" ) )
                compilePatterns |= Pattern.CASE_INSENSITIVE;
            if ( f.contains( "m" ) )
                compilePatterns |= Pattern.DOTALL;
            _compilePatterns = compilePatterns;
        }
        
        _replaceAll = f.contains( "g" );
        
        _patt = Pattern.compile( p , _compilePatterns );
    }

    public String getPattern(){
        return _p;
    }
    
    public String getFlags(){
        return _f;
    }
    
    public String toString(){
        return "/" + _p + "/" + _f;
    }
    
    public int hashCode(){
        return _p.hashCode() + _f.hashCode();
    }

    public boolean equals( Object o ){
        return toString().equals( o.toString() );
    }

    public Pattern getCompiled(){
        return _patt;
    }

    public boolean test( String s ){
        Matcher m = _patt.matcher( s );
        return m.matches();
    }
    
    public JSArray exec( String s ){
        JSArray a = _last.get();
        String oldString = a == null ? null : a.get( "input" ).toString();
        Matcher m = null;

        if ( a != null && s.equals( oldString ) ){
            m = (Matcher)a.get( "_matcher" );
        }
        else {
            m = _patt.matcher( s );
        }

        if ( ! m.find() )
            return null;
        
        a = new JSArray();
        for ( int i=0; i<=m.groupCount(); i++ )
            a.add( m.group(i) );

        a.set( "_matcher" , m );
        a.set( "input" , new JSString( s ) );
        a.set( "index" , m.start() );
        
        if ( _replaceAll )
            _last.set( a );
        else
            _last.set( null );
        
        return a;
    }
    
    String _p;
    String _f;

    int _compilePatterns;
    Pattern _patt;

    boolean _replaceAll;

    ThreadLocal<JSArray> _last = new ThreadLocal<JSArray>();
}
