// JSArray.java

package ed.js;

import java.util.*;

import ed.js.func.*;
import ed.js.engine.*;
import static ed.js.JSInternalFunctions.*;

public class JSArray extends JSObjectBase {
    
    public final static JSFunction _cons = new JSArrayCons();
    static class JSArrayCons extends JSFunctionCalls0{
        
        public Object call( Scope s , Object[] args ){
            throw new RuntimeException( "can't be here" );
        }

        protected void init(){
            _prototype.set( "push" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        a.add( o );
                        return a.size();
                    }
                } );

            _prototype.set( "filter" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSFunction f = (JSFunction)fo;

                        JSArray n = new JSArray();
                        for ( Object o : a._array )
                            if ( JS_evalToBool( f.call( s , o ) ) )
                                n.add( o );
                        return n;
                    }
                } );

            _prototype.set( "forEach" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSFunction f = (JSFunction)fo;
                        
                        for ( Object o : a._array )
                            f.call( s , o );
                        
                        return null;
                    }
                } );

            _prototype.set( "every" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSFunction f = (JSFunction)fo;

                        for ( Object o : a._array )
                            if ( ! JS_evalToBool( f.call( s , o ) ) )
                                return false;
                        return true;
                    }
                } );

            _prototype.set( "some" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSFunction f = (JSFunction)fo;

                        for ( Object o : a._array )
                            if ( JS_evalToBool( f.call( s , o ) ) )
                                return true;
                        return false;
                    }
                } );

            _prototype.set( "map" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSFunction f = (JSFunction)fo;

                        JSArray n = new JSArray();
                        for ( Object o : a._array )
                            n.add( f.call( s , o ) );
                        return n;
                    }
                } );
            
        }
    }
    
    public JSArray(){
        this( 0 );
    }

    public JSArray( int init ){
        super( _cons );

        _array = new ArrayList( Math.max( 16 , init ) );
        
        for ( int i=0; i<init; i++ )
            _array.add( null );
    }

    public JSArray( Object ... obj ){
        super( _cons );
        _array = new ArrayList( obj.length );
        for ( Object o : obj )
            _array.add( o );
    }

    public JSArray( List lst ){
        super( _cons );
        _array = lst;
    }

    public Object setInt( int pos , Object v ){
        while ( _array.size() <= pos )
            _array.add( null );
        _array.set( pos , v );
        return v;
    }

    public Object getInt( int pos ){
        if ( pos >= _array.size() ){
            return null;
        }
        return _array.get( pos );
    }

    public int size(){
        return _array.size();
    }

    public Object get( Object n ){
        if ( n != null )
            if ( n instanceof JSString || n instanceof String )
                if ( n.toString().equals( "length" ) )
                    return _array.size();
        
        int idx = _getInt( n );
        if ( idx >=0 )
            return getInt( idx );
        
        return super.get( n );
    }

    public Object set( Object n , Object v ){
        
        if ( n.toString().equals( "" ) ){
            _array.add( v );
            return v;
        }

        int idx = _getInt( n );
        if ( idx < 0 )
            return super.set( n , v );
        
        return setInt( idx , v );
    }
    
    public Collection<String> keySet(){
        Collection<String> p = super.keySet();
        
        List<String> keys = new ArrayList<String>( p.size() + _array.size() );
        
        for ( int i=0; i<_array.size(); i++ )
            keys.add( String.valueOf( i ) );

        keys.addAll( p );
        
        return keys;
    }

    public String toString(){
        StringBuilder buf = new StringBuilder();
        for ( int i=0; i<_array.size(); i++ ){
            if ( i > 0 )
                buf.append( "," );
            buf.append( _array.get( i ) );
        }
        return buf.toString();
    }

    public void add( Object o ){
        _array.add( o );
    }

    int _getInt( Object o ){
        if ( o == null )
            return -1;

        if ( o instanceof JSString )
            o = o.toString();
        
        if ( ! ( o instanceof String ) )
            return -1;
        
        String str = o.toString();
        for ( int i=0; i<str.length(); i++ )
            if ( ! Character.isDigit( str.charAt( i ) ) )
                return -1;
        
        return Integer.parseInt( str );
    }

    public void shuffle(){
        Collections.shuffle( _array );
    }

    final List<Object> _array;

}
