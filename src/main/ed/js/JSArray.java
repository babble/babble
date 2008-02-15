// JSArray.java

package ed.js;

import java.util.*;

import ed.js.func.*;
import ed.js.engine.*;
import static ed.js.JSInternalFunctions.*;


/**
 * http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Array#Iteration_methods
 * 
 * TODO: 
 * reduce
 * redeceRight

 * slice
 * toSource
 * valueOf 

 */
public class JSArray extends JSObjectBase implements Iterable {
    
    public final static JSFunction _cons = new JSArrayCons();
    static class JSArrayCons extends JSFunctionCalls0{
        
        public Object call( Scope s , Object[] args ){
            throw new RuntimeException( "can't be here" );
        }

        protected void init(){

            _prototype.set( "reverse" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        Collections.reverse( a._array );
                        return a;
                    }
                } );

            _prototype.set( "pop" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        return a._array.remove( a._array.size() - 1 );
                    }
                } );

            _prototype.set( "shift" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        return a._array.remove( 0 );
                    }
                } );

            _prototype.set( "join" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object strJS , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        String str = ",";
                        if ( strJS != null )
                            str = strJS.toString();
                        
                        StringBuilder buf = new StringBuilder();

                        for ( int i=0; i<a._array.size(); i++ ){
                            if ( i > 0 )
                                buf.append( str );
                            buf.append( a._array.get( i ).toString() );
                        }
                        
                        return buf.toString();
                    }
                } );

            
            _prototype.set( "splice" , new JSFunctionCalls2() {
                    public Object call( Scope s , Object startObj , Object numObj , Object foo[] ){
                        
                        JSArray a = (JSArray)(s.getThis());
                        JSArray n = new JSArray();

                        int start = ((Number)startObj).intValue();
                        int num = numObj == null ? 0 : ((Number)numObj).intValue();
                        
                        for ( int i=start; i<a._array.size() && ( num == 0 || i < start + num ); i++ ){
                            n.add( a._array.get( i ) );
                        }
                        return n;
                    }
                } );
            
            _prototype.set( "remove" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object idxObj , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        int idx = ((Number)idxObj).intValue();
                        if ( idx >= a._array.size() )
                            return null;
                        return a._array.remove( idx );
                    }
                } );


            _prototype.set( "push" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        if ( a == null )
                            throw new RuntimeException( "this shouldn't be possible.  scope id = " + s._id );
                        a.add( o );
                        return a.size();
                    }
                } );

            _prototype.set( "unshift" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        if ( a == null )
                            throw new RuntimeException( "this shouldn't be possible.  scope id = " + s._id );
                        a._array.add( 0 , o );
                        if ( foo != null )
                            for ( int i=0; i<foo.length; i++ )
                                a._array.add( 1 + i , foo[i] );
                        return a.size();
                    }
                } );

            _prototype.set( "concat" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        if ( a == null )
                            throw new RuntimeException( "this shouldn't be possible.  scope id = " + s._id );
                        if ( o == null )
                            return a;
                        
                        if ( ! ( o instanceof JSArray ) )
                            throw new RuntimeException( "trying to concat a non-array");
                        
                        a = new JSArray( a );

                        JSArray tempArray = (JSArray)o;
                        for ( Object temp : tempArray._array )
                            a.add( temp );
                        return a;
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


            _prototype.set( "unique" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSArray n = new JSArray();
    
                        Set seen = new HashSet();
                        for ( Object o : a._array ){
                            if ( seen.contains( o ) )
                                continue;
                            seen.add( o );
                            n.add( o ); 
                        }
                        
                        return n;
                    }
                } );

            _prototype.set( "forEach" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSFunction f = (JSFunction)fo;
                        
			if ( f == null )
			    throw new NullPointerException( "forEach needs a function" );
			
                        for ( int i=0; i<a._array.size(); i++ )
                            f.call( s , a._array.get( i ) , i , a._array.size() );
                        
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


            _prototype.set( "contains" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object test , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());

                        for ( Object o : a._array )
                            if ( JSInternalFunctions.JS_eq( o , test ) )
                                return true;
                        
                        return false;
                    }
                } );

            _prototype.set( "indexOf" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object test , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        
                        int start = 0;
                        if ( foo != null && foo.length > 0 && foo[0] instanceof Number )
                            start = ((Number)foo[0]).intValue();

                        for ( int i=start; i<a._array.size(); i++ )
                            if ( JSInternalFunctions.JS_sheq( test , a._array.get( i )  ) )
                                return i;
                        
                        return -1;
                    }
                } );

            _prototype.set( "lastIndexOf" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object test , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        
                        int start = a._array.size() - 1 ;
                        if ( foo != null && foo.length > 0 && foo[0] instanceof Number )
                            start = ((Number)foo[0]).intValue();

                        if ( start >= a._array.size() )
                            start = a._array.size() - 1;

                        for ( int i=start; i>=0; i-- )
                            if ( JSInternalFunctions.JS_sheq( test , a._array.get( i )  ) )
                                return i;
                        
                        return -1;
                    }
                } );

            _prototype.set( "sort" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object func , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        
                        if ( func == null )
                            Collections.sort( a._array , _normalComparator );
                        else
                            Collections.sort( a._array , new MyComparator( s , (JSFunction)func ) );
                        
                        return a;
                    }
                } );
            


        }
    }
    
    public static JSArray create( Object ... obj ){
        return new JSArray( obj );
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

    public JSArray( JSArray a ){
        super( _cons );
        _array = a == null ? new ArrayList() : new ArrayList( a._array );
    }
    
    public JSArray( List lst ){
        super( _cons );
        _array = lst == null ? new ArrayList() : lst;
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
        if ( _locked )
            throw new RuntimeException( "array locked" );
        _array.add( o );
    }

    public void addAll( Collection c ){
        _array.addAll( c );
    }

    public Iterator iterator(){
        return _array.iterator();
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

    public void lock(){
        _locked = true;
    }

    public void clear(){
        _array.clear();
    }

    public boolean isLocked(){
        return _locked;
    }

    private boolean _locked = false;
    final List<Object> _array;

    static class MyComparator implements Comparator {
        MyComparator( Scope s , JSFunction func ){
            _scope = s;
            _func = func;
        }
        
        public int compare( Object l , Object r ){
            if ( _func == null ){
                if ( l == null && r == null )
                    return 0;
                if ( l == null )
                    return 1;
                if ( r == null )
                    return -1;
                return l.toString().compareTo( r.toString() );
            }
            
            return ((Number)(_func.call( _scope , l , r , null ))).intValue();
        }

        private final Scope _scope;
        private final JSFunction _func;
    }
    static final MyComparator _normalComparator = new MyComparator( null , null );
}
