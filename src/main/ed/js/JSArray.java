// JSArray.java

package ed.js;

import java.util.*;

import ed.js.func.*;
import ed.js.engine.*;
import static ed.js.JSInternalFunctions.*;


/**
 * http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Array#Iteration_methods
 * 
 */
public class JSArray extends JSObjectBase implements Iterable , List {
    
    public final static JSFunction _cons = new JSArrayCons();
    static class JSArrayCons extends JSFunctionCalls1{
        
        public JSObject newOne(){
            JSArray a = new JSArray();
            a._new = true;
            return a;
        }
        
        public Object call( Scope scope , Object a , Object[] extra ){        
        
            int len = 0;
            if ( extra == null || extra.length == 0 ){
                if ( a instanceof Number )
                    len = ((Number)a).intValue();
            }
            else {
                len = 1 + extra.length;
            }

            JSArray arr = null;
            
            Object t = scope.getThis();
            if ( t != null && t instanceof JSArray && ((JSArray)t)._new ){
                arr = (JSArray)t;
                if ( len > 0 )
                    arr._initSizeSet( len );
                arr._new = false;
            }
            else {
                arr = new JSArray( len );
            }

            if ( ( a != null && ! ( a instanceof Number ) ) || 
                 ( extra != null && extra.length > 0 ) ){
                arr.setInt( 0 , a );
                if ( extra != null ){
                    for ( int i=0; i<extra.length; i++)
                        arr.setInt( 1 + i , extra[i] );
                }
            }
            
            return arr;
        }

        protected void init(){

            _prototype.set( "toSource" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        return new JSString( JSON.serialize( s.getThis() ) );
                    }
                } );

            _prototype.set( "valueOf" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        return s.getThis();
                    }
                } );

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
                        int num = numObj == null ? Integer.MAX_VALUE : ((Number)numObj).intValue();
                        
                        for ( int i=0; i<num && start < a._array.size(); i++ )
                            n._array.add( a._array.remove( start ) );

                        if ( foo != null )
                            for ( int i=0; i<foo.length; i++ )
                                a._array.add( i + start , foo[i] );

                        return n;
                    }
                } );

            _prototype.set( "slice" , new JSFunctionCalls2() {
                    public Object call( Scope s , Object startObj , Object numObj , Object foo[] ){
                        
                        JSArray a = (JSArray)(s.getThis());
                        JSArray n = new JSArray();

                        int start = startObj == null ? 0 : ((Number)startObj).intValue();
                        int end = numObj == null ? Integer.MAX_VALUE : ((Number)numObj).intValue();
                        if ( end < 0 )
                            end = a._array.size() + end;
                        
                        for ( int i=start; i<end && i < a._array.size(); i++ )
                            n._array.add( a._array.get( i ) );

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
            
            _prototype.set( ed.lang.ruby.Ruby.RUBY_SHIFT , _prototype.get( "push" ) );

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

            _prototype.set( "__rshift" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        if ( a == null )
                            throw new RuntimeException( "this shouldn't be possible.  scope id = " + s._id );
                        a._array.add( o );
                        if ( foo != null )
                            for ( int i=0; i<foo.length; i++ )
                                a._array.add( foo[i] );
                        return a;
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

            _prototype.set( "reduce" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSFunction f = (JSFunction)fo;
                        
                        Object val = null;
                        if ( foo != null && foo.length > 0 )
                            val = foo[0];
                        
                        Integer l = a._array.size();
                        
                        for ( int i=0; i<a._array.size(); i++ ){
                            val = f.call( s , val , a._array.get(i) , i , l );
                        }
                        
                        return val;
                    }
                } );

            _prototype.set( "reduceRight" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSFunction f = (JSFunction)fo;
                        
                        Object val = null;
                        if ( foo != null && foo.length > 0 )
                            val = foo[0];
                        
                        Integer l = a._array.size();
                        
                        for ( int i=a._array.size() -1 ; i >= 0; i-- ){
                            val = f.call( s , val , a._array.get(i) , i , l );
                        }
                        
                        return val;
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

            _prototype.set( "dup" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        return new JSArray( a );
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
                            n.add( fixAndCall( s , f , o ) );
                        return n;
                    }
                } );
            _prototype.set( "collect" , _prototype.get( "map" ) );

            _prototype.set( "collect_ex_" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSFunction f = (JSFunction)fo;

                        for ( int i=0; i<a._array.size(); i++ ){
                            a.set( i , fixAndCall( s , f , a._array.get( i ) ) );
                        }
                        
                        return a;
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
            
            _prototype.set( "include_q_" , _prototype.get( "contains" ) );

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

	    /* 
	       ex:
	       foo = {}
	       [ 1 , 2, 3 ].__multiAssignment( scope , "a" , foo , "b", q, 3 );
	       assert( scope.a == 1 )
	       assert( foo.b == 2 );
	       assert( q[3] == 3) ;
	     */
            _prototype.set( "__multiAssignment" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());

                        if ( foo != null ){
                            for ( int i=0; i<foo.length-1; i+=2 ){
                                JSObject obj = (JSObject)foo[i];
                                obj.set( foo[i+1] , a.get( i / 2 ) );
                            }
                        }
                        return a;
                    }
                } );


            _prototype.set( "empty_q_" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        return a.isEmpty();
                    }
                }
                );

            _prototype.set( "compact" , new JSFunctionCalls0() {
                    public Object call( Scope s , Object foo[] ){
                        JSArray a = (JSArray)(s.getThis());
                        JSArray n = new JSArray();
                        for ( Object o : a )
                            if ( o != null )
                                n.add( o );
                        return n;
                    }
                }
                );
            
            _prototype.set( "each" , new JSFunctionCalls1(){
                    public Object call(Scope s, Object funcObject , Object [] args){

                        if ( funcObject == null )
                            throw new NullPointerException( "each needs a function" );
                        
                        JSFunction func = (JSFunction)funcObject;
                        JSArray a = (JSArray)(s.getThis());
                        
                        Object blah = s.getParent().getThis();
                        s.setThis( blah );
                        
                        Boolean old = func.setUsePassedInScopeTL( true );

                        for ( int i=0; i<a._array.size(); i++ ){
                            Object o = a._array.get( i );
                            Object ret = func.call( s , o );
                            
                            if ( ret == null )
                                continue;
                            
                            if(ret instanceof Number && ((Number)ret).longValue() == -111) {
                                i--;
                                continue;
                            }
                                

                            if ( JSInternalFunctions.JS_evalToBool( ret ) )
                                continue;
                            
                            break;
                        }
                        
                        func.setUsePassedInScopeTL( old );

                        s.clearThisNormal( null );
                        return null;
                    }
                } );

            this.set( "createLinkedList", new JSFunctionCalls0(){
                    public Object call(Scope s, Object [] extra){
                        return new JSArray(new LinkedList());
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
        _initSizeSet( init );
    }

    private void _initSizeSet( int init ){
        for ( int i=0; i<init; i++ )
            _array.add( null );
    }

    public JSArray( Object ... obj ){
        super( _cons );
        if ( obj == null ){
            _array = new ArrayList();
        }
        else {
            _array = new ArrayList( obj.length );
            for ( Object o : obj )
                _array.add( o );
        }

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
        keys.remove( "_dbCons" );

        return keys;
    }

    @Override
    public boolean containsKey(String s) {
        return "length".equals(s) || super.containsKey(s);
    }

    public String toString(){
        StringBuilder buf = new StringBuilder();
        for ( int i=0; i<_array.size(); i++ ){
            if ( i > 0 )
                buf.append( "," );
            Object val = _array.get( i );
            buf.append( val == null ? "" : val );
        }
        return buf.toString();
    }
    
    public boolean add( Object o ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        return _array.add( o );
    }

    public void add( int index , Object o ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        _array.add( index , o );
    }

    public Object set( int index , Object o ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        return _array.set( index , o );
    }

    public Object get( int i ){
        return _array.get( i );
    }

    public boolean addAll( Collection c ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        return _array.addAll( c );
    }

    public boolean addAll( int idx , Collection c ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        return _array.addAll( idx , c );
    }

    public boolean containsAll( Collection c ){
        return _array.containsAll( c );
    }
    
    public List subList( int start , int end ){
        return _array.subList( start , end );
    }

    public ListIterator listIterator(){
        return _array.listIterator();
    }

    public ListIterator listIterator( int index ){
        return _array.listIterator( index );
    }

    public int lastIndexOf( Object foo ){
        return _array.lastIndexOf( foo );
    }

    public int indexOf( Object foo ){
        return _array.indexOf( foo );
    }

    public boolean contains( Object foo ){
        return _array.contains( foo );
    }

    public boolean isEmpty(){
        return _array.isEmpty();
    }

    public Object remove( int i ){
        if ( _locked )
            throw new RuntimeException( "array locked" );        
        return _array.remove( i );
    }

    public boolean remove( Object o ){
        if ( _locked )
            throw new RuntimeException( "array locked" );        
        return _array.remove( o );
    }

    public Iterator iterator(){
        return _array.iterator();
    }

    public boolean retainAll( Collection c ){
        throw new RuntimeException( "not implemented" );
    }

    public boolean removeAll( Collection c ){
        boolean removedAny = false;
        for ( Object o : c ){
            removedAny = remove( o ) || removedAny;
        }
        return removedAny;
    }

    public Object[] toArray(){
        return _array.toArray();
    }

    public Object[] toArray( Object[] o ){
        return _array.toArray( o );
    }

    int _getInt( Object o ){
        if ( o == null )
            return -1;
        
        if ( o instanceof Number )
            return ((Number)o).intValue();

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

    JSFunction getGetter( String name ){
        return null;
    }

    JSFunction getSetter( String name ){
        return null;
    }

    public static Object fixAndCall( Scope s , JSFunction f , Object o ){
        if ( false ){
            System.out.println( "ruby:" + s.isRuby() );
            System.out.println( "params: " + f.getNumParameters() );
            System.out.println( "thing:" + o.getClass() );
        }
        if ( s.isRuby() && f.getNumParameters() > 1 && o instanceof JSArray )
            return f.call( s , ((JSArray)o).toArray() );
        return f.call( s , o );
    }

    private boolean _locked = false;
    private boolean _new = false;
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
