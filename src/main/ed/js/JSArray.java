// JSArray.java

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

import java.util.*;

import ed.js.func.*;
import ed.js.engine.*;
import static ed.js.JSInternalFunctions.*;


/** @expose
 * @anonymous name : {toSource}, desc : {Returns this array serialized as json.}, return : {type : (String), desc : (the array in json form)}
 * @anonymous name : {valueOf}, desc : {Returns this array.}, return : {type : (JSArray), desc : (the array)}
 * @anonymous name : {reverse}, desc : {Reverses this array in place.}, return : {type : (JSArray), desc : (the reversed array)}
 * @anonymous name : {pop}, desc : {Removes the last element of this array and returns it.}, return : {type : (JSObject), desc : (the former last element of the array)}
 * @anonymous name : {shift}, desc : {Removes the first element of this array and returns it.}, return : {type : (JSObject), desc : (the former first element of this array)}
 * @anonymous name : {join}, desc : {Creates a string from the array using a given delimeter.} param : {type : (String), name : (d), desc : (delimiter to use)}, return : { type : (String), desc : (delimiter-separated string of elements)}
 * @anonymous name : {splice}, desc : {Inserts and removes elements from this array in place.}, param : {type : (int), name : (idx), desc : (index at which to start insertion and/or deletion)}, param : { type : (int), name : (num), desc : (number of elements to delete)}, param : {type : (any), name : (elem1 ... elemn), desc : (elements to insert into the array)}, return : {desc : (the deleted elements), type : (Array)}
 * @anonymous name : {remove}, desc : {Removes the element at a given index of this array.}, return : { type : (any), desc : (the element removed)}, param : {name : (idx), desc : (index of the element to remove), type : (number)}
 * @anonymous name : {push}, desc : {Adds a given object to the end of this array, returning the length of the array.}, return : {type : (number), desc : (the length of this array)}, param : {type : (any), name : (o), desc : (the element to add to the end of this array)}
 * @anonymous name : {unshift}, desc : {Adds a given object to the beginning of this array.}, return : { type : (number), desc : (the length of this array)}, param : {type : (any), name : (o), desc : (the element to add to the end of this array)}
 * @anonymous name : {concat}, desc : {Adds elements of a given array to this array.}, return : {type : (Array), desc : (the modified array)}, param : {type : (Array), name : (ar), desc : (an array of values to be added)}
 * @anonymous name : {filter}, desc : {Returns the elements for which a given function returned true.}, return : {type : (Array), desc : (the elements for which the function returned true)}, param : { type : (function), name : (f), desc : (function to perform on each element)}
 * @anonymous name : {reduce}, desc : {Apply a function simultaneously against two values of the array (from left-to-right) as to reduce it to a single value.}, return : {type : (any), desc : (single-value result)}, param : { type : (function), name : (f), desc : (function to perform on each pair of elements)}, param : {name : (initialValue), desc : (value to pass to the function with the first element of the array), type : (any) }
 * @anonymous name : {reduceRight}, desc : {Apply a function simultaneously against two values of the array (from right-to-left) as to reduce it to a single value.}, return : {type : (any), desc : (single-value result)}, param : { type : (function), name : (f), desc : (function to perform on each pair of elements)}, param : {name : (initialValue), desc : (value to pass to the function with the last element of the array), type : (any) }
 * @anonymous name : {dup}, desc : {Creates a duplicate of this array.}, return : {type : (Array), desc : (a copy of this array)}
 * @anonymous name : {forEach}, desc : {Executes a function on each element of this array.}, param : {type : (function), name : (f), desc : (function to perform on each element)}
 * @anonymous name : {every}, desc : {Tests whether all elements in the array pass the test implemented by the provided function.}, return : {type : (boolean), desc : (if every element passes)}, param : { type : (function), name : (f), desc : (function to test each element)}
 * @anonymous name : {some}, desc : {Tests whether some element in the array passes the test implemented by the provided function.}, return : {type : (boolean), desc : (if there exists an element that passes)}, param : {type : (function), name : (f), desc : (function to test each element)}
 * @anonymous name : {collect}, desc : {Creates a new array with the results of calling a provided function on every element in this array.}, return : {type : (Array), desc : (result of running a function on each element)}, param : {type : (function), name : (f), desc : (function to call on each element)}
 * @anonymous name : {contains}, desc : {Checks if this array contains a given value.}, return : {type : (boolean), desc : (if this array contains the value)}, param : {type : (any), name : (o), value : (object for which to check)}
 * @anonymous name : {indexOf}, desc : {Finds the first occurence of a given object in this array.}, return : {type : (boolean), desc : (the index of the first occurence of this value, or -1)}, param : {type : (any), name : (o), value : (object for which to check)}
 * @anonymous name : {lastIndexOf}, desc : {Finds the last occurence of a given object in this array.}, return : {type : (boolean), desc : (the index of the last occurence of this value, or -1)}, param : {type : (any), name : (o), value : (object for which to check)}
 * @anonymous name : {sort}, desc : {Sorts the elements of this array, optionally based on a given function.  Sorts this array in place. }, return : {type : (Array), desc : (the sorted array)}, param : {type : (function), name : (f), desc : (sorting function taking two parameters and returning 1, 0, or -1.)}
 * @anonymous name : {compact}, desc : {Returns this array with any null elements removed.  Does not alter this array.}, return : {type : (Array), desc : (array without nulls)}
 * @anonymous name : {each}, desc : {Executes a function on each element of this array, breaking if the function returns a false value.}, param : {type : (function), name : (f), desc : (function to perform on each element)}
 *
 * http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Array#Iteration_methods
 */
public class JSArray extends JSObjectBase implements Iterable , List {

    /** @unexpose  */
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

                        return new JSString( buf.toString() );
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
            

            _prototype.dontEnumExisting();
        }
    }

    /** Create this array using a variable number of objects passed as arguments.
     * @param obj Some number of objects.
     * @return Newly created array.
     */
    public static JSArray create( Object ... obj ){
        return new JSArray( obj );
    }


    /** Create an empty array.
     * The array is initialized with space for 16 elements.
     */
    public JSArray(){
        this( 0 );
    }

    /** Create an empty array with a given allocation.
     * @param init The initial allocation for the array.  The minimum possible value is 16.
     */
    public JSArray( int init ){
        super( _cons );

        _array = new ArrayList( Math.max( 16 , init ) );
        _initSizeSet( init );
    }

    private void _initSizeSet( int init ){
        for ( int i=0; i<init; i++ )
            _array.add( null );
    }

    /** Create an array and fill with objects
     * @param obj A variable number of objects
     */
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

    /** Create an array that is the copy of an existing array.
     * @param a A JavaScript array.
     */
    public JSArray( JSArray a ){
        super( _cons );
        _array = a == null ? new ArrayList() : new ArrayList( a._array );
    }

    /** Create an array from an existing Java List object.
     * @param lst A Java List object
     */
    public JSArray( List lst ){
        super( _cons );
        _array = lst == null ? new ArrayList() : lst;
    }

    public JSArray( Collection coll ){
        super( _cons );
        _array = new ArrayList();
        if ( coll != null )
            _array.addAll( coll );
    }

    /** Set an element at a specified index to a given value.
     * If <tt>pos</tt> exceeds the length of this array, this array's size is increased to <tt>pos</tt>+1 and the elements between
     * the original end of this array and the inserted element are set to <tt>null</tt>.
     * @param pos The index of the element.
     * @param v The object to replace the element at position <tt>pos</tt>.
     * @return The inserted object, <tt>v</tt>.
     */
    public Object setInt( int pos , Object v ){
        while ( _array.size() <= pos )
            _array.add( null );
        _array.set( pos , v );
        return v;
    }

    /** Return the element at position <tt>pos</tt>.
     * If <tt>pos</tt> is greater than the length of this array, return null.
     * @param pos The index of the element to return.
     * @return The element at the specified position in this array.
     */
    public Object getInt( int pos ){
        if ( pos >= _array.size() ){
            return null;
        }
        return _array.get( pos );
    }

    /** Returns the number of elements in this list.
     * @return The number of elements in this list.
     */
    public int size(){
        return _array.size();
    }

    /** Return the element at position <tt>n</tt>.
     * If <tt>n</tt> is greater than the length of this array, return null.
     * @param n The index of the element to return.
     * @return The element at the specified position in this array.
     */
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

    /** Set an element at a specified index to a given value.
     * If <tt>n.toString()</tt> is equal to "", <tt>v</tt> is pushed onto the end of this array.
     * If <tt>n</tt> exceeds the length of this array, this array's size is increased to <tt>n</tt>+1 and the elements between
     * the original end of this array and the inserted element are set to <tt>null</tt>.
     * @param n The index of the element.
     * @param v The object to replace the element at position <tt>n</tt>.
     * @return The inserted object, <tt>v</tt>.
     */
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

    /** Returns an array of the indices for this array.
     * @return The indices for this array.
     */
    public Collection<String> keySet( boolean includePrototype ){
        Collection<String> p = super.keySet( includePrototype );

        List<String> keys = new ArrayList<String>( p.size() + _array.size() );

        for ( int i=0; i<_array.size(); i++ )
            keys.add( String.valueOf( i ) );

        keys.addAll( p );
        keys.remove( "_dbCons" );

        return keys;
    }

    /** If this array contains a certain key.
     * @param s The key to check.
     * @return If this array contains the key <tt>s</tt>.
     */
    @Override
    public boolean containsKey(String s) {
        return "length".equals(s) || super.containsKey(s);
    }

    /** Return a comma-separated list of array elements converted to strings.
     * @return A comma-separated list of array elements.
     */
    public String toString(){
        StringBuilder buf = new StringBuilder();
        for ( int i=0; i<_array.size(); i++ ){
            if ( i > 0 )
                buf.append( "," );
            Object val = _array.get( i );
            buf.append( val == null ? "" : JSInternalFunctions.JS_toString( val ) );
        }
        return buf.toString();
    }

    /** Add an object to the end of this array.
     * @param o Object to be added.
     * @return true
     */
    public boolean add( Object o ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        return _array.add( o );
    }

    /** Inserts the specified element at the specified position in this array. Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     * @param o Object to be added.
     * @return true
     */
    public void add( int index , Object o ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        _array.add( index , o );
    }

    /** Replaces the element at the specified position in this array with the specified element.
     * @param index The index of the element.
     * @param o The object to replace the element at position <tt>index</tt>.
     * @return The element previously at the specified position.
     */
    public Object set( int index , Object o ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        return _array.set( index , o );
    }

    /** Returns the element at the specified position in this array.
     * @param i Index of element to return.
     * @return The element at the specified position in this array.
     */
    public Object get( int i ){
        if ( i >= _array.size() )
            return null;
        return _array.get( i );
    }

    /** Append all of the elements of a collection to this array.
     * @param c Collection to be appended.
     * @return If the array changed.
     */
    public boolean addAll( Collection c ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        return _array.addAll( c );
    }

    /** Add all of the elements of a collection starting at a specified index of this array, shifting subsequent elements (if any) to the right.
     * @param idx Index to begin insertion.
     * @param c Collection to be added.
     * @return If the array changed.
     */
    public boolean addAll( int idx , Collection c ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        return _array.addAll( idx , c );
    }

    /** Returns true if this array contains all of the elements of the specified collection.
     * @param c Collection to be tested.
     * @return true if this array contains all of the elements of the specified collection.
     */
    public boolean containsAll( Collection c ){
        return _array.containsAll( c );
    }

    /** Returns a view of the portion of this array between fromIndex, inclusive, and toIndex, exclusive.
     * @param start Starting index.
     * @param end Ending index.
     * @return A view of the specified range within this array.
     */
    public List subList( int start , int end ){
        return _array.subList( start , end );
    }

    /** Returns an iterator of the elements in this array.
     * @return An iterator of the elements in this array.
     */
    public ListIterator listIterator(){
        return _array.listIterator();
    }

    /** Returns a array iterator of the elements in this array, starting at the specified position in the array.
     * @param index The index at which to start the iterator.
     * @return An iterator of the elements in this array, starting at <tt>index</tt>.
     */
    public ListIterator listIterator( int index ){
        return _array.listIterator( index );
    }

    /** Returns the index of the last occurrence of the specified object in this array.
     * @param foo Object for which to search.
     * @return Last index at which <tt>foo</tt> was found or -1.
     */
    public int lastIndexOf( Object foo ){
        return _array.lastIndexOf( foo );
    }

    /**  Returns the index of the first occurrence of the specified object in this array.
     * @param foo Object for which to search.
     * @return First index at which <tt>foo</tt> was found or -1.
     */
    public int indexOf( Object foo ){
        return _array.indexOf( foo );
    }

    /** Returns true if this array contains the specified element.
     * @param foo Object for which to search.
     * @return If the object was found in this array.
     */
    public boolean contains( Object foo ){
        for ( int i=0; i<_array.size(); i++ ){
            Object o = _array.get(i);
            if ( o != null && o.equals( foo ) )
                return true;
        }
        return false;
    }

    /** Tests if this array has no elements.
     * @return If the array has no elements.
     */
    public boolean isEmpty(){
        return _array.isEmpty();
    }

    /** Remove an element at a specified position. Shifts any subsequent elements left.
     * @param i Index at which element should be removed.
     * @return The element that was removed from the array.
     */
    public Object remove( int i ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        return _array.remove( i );
    }

    /** Removes a single instance of the specified element from this array. Shifts any subsequent elements left.
     * @param o Object to be removed.
     * @return If the array contained the given element.
     */
    public boolean remove( Object o ){
        if ( _locked )
            throw new RuntimeException( "array locked" );
        return _array.remove( o );
    }

    /** Returns an iterator over the elements in this list in proper sequence.
     * @return An iterator over the elements in this list in proper sequence.
     */
    public Iterator iterator(){
        return _array.iterator();
    }

    /** Retains only the elements in this collection that are contained in the specified collection. In other words, returns the intersection of this array and the given collection.
     * @param c Collection with which to intersect the array.
     * @return true if this collection changed as a result of the call.
     * @throws RuntimeException All the time... this method is not yet implemented.
     */
    public boolean retainAll( Collection c ){
        throw new RuntimeException( "not implemented" );
    }

    /** Removes from this list all the elements that are contained in the specified collection.
     * @param c Collection of elements to be removed.
     * @return true if this list changed as a result of the call.
     */
    public boolean removeAll( Collection c ){
        boolean removedAny = false;
        for ( Object o : c ){
            removedAny = remove( o ) || removedAny;
        }
        return removedAny;
    }

    /** Converts this array to a Java array of objects.  No effect from JavaScript.
     * @return Java Object array.
     */
    public Object[] toArray(){
        return _array.toArray();
    }

    /** Returns an array containing all of the elements in this list in the correct order; the runtime type of the returned array is that of the specified array. If the list fits in the specified array, it is returned therein. Otherwise, a new array is allocated with the runtime type of the specified array and the size of this list.
     *      If the list fits in the specified array with room to spare (i.e., the array has more elements than the list), the element in the array immediately following the end of the collection is set to null. This is useful in determining the length of the list only if the caller knows that the list does not contain any null elements.
     * @return Java Object array.
     */
    public Object[] toArray( Object[] o ){
        return _array.toArray( o );
    }

    /** @unexpose */
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

    /** Randomly permutes this array using a default source of randomness. All permutations occur with approximately equal likelihood.
     * @return This array.
     */
    public JSArray shuffle(){
        Collections.shuffle( _array );
        return this;
    }

    /** Make this array mostly immutable. */
    public void lock(){
        _locked = true;
    }

    /** Remove all array elements. */
    public void clear(){
        _array.clear();
    }

    /** Return if the array is locked.
     * @return If the array is locked.
     */
    public boolean isLocked(){
        return _locked;
    }

    /** Return array getter of a given name
     * @param name Getter name.
     * @return null
     */
    JSFunction getGetter( String name ){
        return null;
    }

    /** Return array setter of a given name
     * @param name Setter name.
     * @return null
     */
    JSFunction getSetter( String name ){
        return null;
    }

    /** Call a function on an array in a specified scope.
     * @param s Scope to use.
     * @param f Function to call.
     * @param o Array to pass to function.
     * @return The return value of the function.
     */
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

    /** The hash code value of this array.
     * @return The hash code value of this array.
     */
    public int hashCode(){
        int hash = super.hashCode();
        if ( _array != null ){
            hash += _array.hashCode();
        }
        return hash;
    }

    private boolean _locked = false;
    private boolean _new = false;
    /** @unexpose */
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
