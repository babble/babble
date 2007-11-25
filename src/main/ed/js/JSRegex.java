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

    String _p;
    String _f;

    int _compilePatterns;
    Pattern _patt;

    boolean _replaceAll;
}
